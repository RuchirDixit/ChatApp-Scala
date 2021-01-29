
package com.bridgelabz.routes

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.bridgelabz.actors.{ActorSystemFactory, EmailNotificationActor, SaveToDatabaseActor}
import com.bridgelabz.caseClasses._
import com.bridgelabz.database.MongoDatabase
import com.bridgelabz.jwt.TokenAuthorization
import com.bridgelabz.marshallers.{MyJsonProtocol, MyJsonResponse}
import com.bridgelabz.services.{DatabaseService, UserManagementService}
import com.nimbusds.jose.JWSObject
import com.typesafe.scalalogging.LazyLogging
import courier.{Envelope, Mailer, Text}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import javax.mail.internet.InternetAddress
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, TimeoutException}
import scala.util.{Failure, Success}

class UserManagementRoutes(service: UserManagementService) extends PlayJsonSupport with LazyLogging
  with MyJsonProtocol with MyJsonResponse {
  implicit val system = ActorSystemFactory.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val saveMessageActor = system.actorOf(Props[SaveToDatabaseActor], "saveActor")
  val routes: Route =
    pathPrefix("user") {
        /**
         * for login using post request,
         * @input : It accepts id, name and password as body and generates jwt token based on name
         * @Return :  success on successful login or else returns unauthorized
         */
        path("login") {
          (post & entity(as[User])) { loginRequest =>
            logger.info("Login response: " + service.userLogin(loginRequest))
            if (service.userLogin(loginRequest) == "Login Successful") {
              val id = service.returnId(loginRequest.name)
              val token: String = TokenAuthorization.generateToken(loginRequest.name, id)
              respondWithHeaders(RawHeader("token",token)) {
                complete(StatusCodes.OK)
              }
            }
            else if (service.userLogin(loginRequest) == "User Not verified") {
              logger.error("User's email not verified")
              complete(StatusCodes.UnavailableForLegalReasons,JsonResponse("User's Email Id is not verified!"))
            } else {
              logger.error("Invalid credentials")
              complete(StatusCodes.Unauthorized, JsonResponse("Invalid credentials. User not found! Try again with correct details."))
            }
          }
        } ~
        /**
         * to verify user using post request,
         * @input : It accepts parameters token and name from the link
         * It fetches user by decoding the token and updates the isVerified field to true
         * @return :  success on successful verification of user and error if verification failed
         */
        path("verify") {
          get {
            parameters('token.as[String], 'name.as[String]) {
              (token, name) =>
                val jwsObject = JWSObject.parse(token)
                val updateUserAsVerified = MongoDatabase.collectionForUserRegistration.updateOne(equal("name", name), set("isVerified", true)).toFuture()
                Await.result(updateUserAsVerified, 60.seconds)
                if (jwsObject.getPayload.toJSONObject.get("name").equals(name)) {
                  logger.info("Successfully verified user!")
                  complete(StatusCodes.OK, JsonResponse("User successfully verified and registered!"))
                }
                else {
                  logger.error("user not verified.")
                  complete(StatusCodes.BadRequest -> JsonResponse("User could not be verified! Please verify using the mail sent."))
                }
            }
          }
        } ~
        /**
         * to get all messages using get request,
         * @input : It accepts group name as parameter
         * It fetches all the messages inside group chat name mentioned in as query parameter
         * @return : All the messages on success and error if group chat name not found
         */
        path("messages") {
          (post & entity(as[Messages])) { messageRequest =>
            try {
              val senderId = service.returnId(messageRequest.sender)
              val receiverId = service.returnId(messageRequest.receiver)
              val roomname = service.generateGroupChatName(messageRequest.sender, messageRequest.receiver, senderId.toString, receiverId)
              val messagesByGroupName = MongoDatabase.collectionForChat.find(equal("groupChatName", roomname)).toFuture()
              val messages = Await.result(messagesByGroupName, 60.seconds)
              logger.info("Messages:" + messages)
              complete(messages)
            }
            catch {
              case _: TimeoutException =>
                logger.error("Timeout exception for getting messages on roomname")
                complete(JsonResponse("Reading file timeout."))
            }
          }
        } ~
          /**
           * to register user using post request,
           * @input : It accepts id,name,password
           * It generates jwt token on successful registration of user and sends email to user for verification
           * @return : success on successful registration of user or else error message if user already exists
           */
          path("register") {
            (post & entity(as[User])) { createUserRequest =>
              val userCreatedResponse = service.createUser(createUserRequest)
              logger.info("Create response::" + userCreatedResponse)
              if (userCreatedResponse == "User created") {
                logger.info("User object in register:" + createUserRequest)
                val a:Option[Any] = Some(createUserRequest.id)
                val id = (a match {
                  case Some(x:Int) => x
                  case _ => Int.MinValue
                })
                val token: String = TokenAuthorization.generateToken(createUserRequest.name, id)
                val mailer = Mailer(sys.env("mailer"), sys.env("smtp_port").toInt)
                  .auth(true)
                  .as(sys.env("sender_email"), sys.env("sender_password"))
                  .startTls(true)()
                mailer(Envelope.from(new InternetAddress(sys.env("sender_email")))
                  .to(new InternetAddress(createUserRequest.name))
                  .subject("Token")
                  .content(Text("Thanks for registering! Click on this link to verify your email address: http://"
                    + sys.env("Host") + ":" + sys.env("Port_number") + "/user/verify?token="
                    + token + "&name=" + createUserRequest.name)))
                  .onComplete {
                    case Success(_) =>
                      val schedulerActor = system.actorOf(Props[EmailNotificationActor], "schedulerActor")
                      system.scheduler.schedule(30.seconds, 120.seconds, schedulerActor, createUserRequest.name)
                      logger.info("Message delivered. Email verified!")
                    case Failure(_) =>
                      logger.error("Failed to deliver mail.")
                      complete(StatusCodes.NotFound, JsonResponse("Failed To Deliver Mail. Please check the email again."))
                  }
                complete((StatusCodes.OK,JsonResponse("User Registered! Now you can Login using these details.")))
              } else if (userCreatedResponse == "User Validation Failed") {
                logger.error("Email address not correct according to pattern")
                complete(StatusCodes.BadRequest -> JsonResponse("Error! Please enter correct email address."))
              }
              else {
                logger.error("User already exists")
                complete(StatusCodes.Unauthorized, JsonResponse("Error! User already exists!"))
              }
            }
          } ~
          /**
           * For getting token and decoding username of sender from token
           * @input: It accepts token as authorization parameter, and sender, receiver and message to save
           * It checks if receiver is a registered user if not throws message to register, if yes Saved message to Database
           * @return:  success message if message delivered or error message if not delivered.
           */
          path("authorize") {
            (post & entity(as[ChatCase])) { getDataToSaveChats =>
              TokenAuthorization.authenticated { token =>
                logger.info("Token:" + token.values.toList.last.toString())
                logger.info("receiver: " + getDataToSaveChats.receiver)
                if (DatabaseService.checkIfExists(getDataToSaveChats.receiver) == true) {
                  val receiverId = service.returnId(getDataToSaveChats.receiver)
                  val senderId = token.values.toList.head.toString
                  val userService = new UserManagementService
                  val groupChatName = userService.generateGroupChatName(token.values.toList.last.toString(), getDataToSaveChats.receiver, senderId, receiverId)
                  val saveToChat = getDataToSaveChats.copy(sender = Some(token.values.toList.last.toString()),groupChatName = Some(groupChatName))
                  implicit val timeout = Timeout(10.seconds)
                  val futureResponse = saveMessageActor ? saveToChat
                  val result = Await.result(futureResponse, 60.seconds)
                  if (result.equals("Message added")) {
                    complete(StatusCodes.OK, JsonResponse("Message sent to receiver!"))
                  }
                  else {
                    logger.error("Message could not be delivered")
                    complete(StatusCodes.BadRequest -> JsonResponse("Message could not be delivered! Try sending all the data correctly again."))
                  }
                }
                else {
                  logger.error("Receiver need to be a registered user")
                  complete(StatusCodes.Unauthorized -> JsonResponse("Receiver needs to be a registered user. Please register to continue communication."))
                }
              }
            }
          } ~
          /**
           * For getting token and decoding username of sender from token
           * @input: It accepts token as authorization parameter, receiver (groupname) and message to save
           * It saves messages from sender to a particular group mentioned by user
           * @return:  success message if message delivered or error message if not delivered.
           */
          path("groupChat") {
            (post & entity(as[GroupChat])) { groupData =>
              TokenAuthorization.authenticated { token =>
                logger.info("Token with sender:" + token.values.toList.last.toString())
                val groupChatInformation = groupData.copy(sender = Some(token.values.toList.last.toString()))
                val response = service.saveGroupChat(groupChatInformation)
                if(response.equals("Message added")){
                  logger.info("Message send to group successfully.")
                  complete(StatusCodes.OK,JsonResponse("Message sent in Group!"))
                }
                else {
                  logger.error("Message could not be sent to group.")
                  complete(StatusCodes.BadRequest,JsonResponse("Message could not be sent."))
                }
              }
            }
          } ~
        /**
           * It displays all the messages with their senders in the group
           * @input : It accepts group name for which all messages are to be displayed
           *  @returns: All the messages with sender, group name and messages in Json format
           */
          path("groupMessages") {
            (post & entity(as[GroupMessages])) { messageRequest =>
              try {
                val groupname = messageRequest.groupName
                val messagesByGroupName = MongoDatabase.collectionForGroup.find(equal("receiver", groupname)).toFuture()
                val groupmessages = Await.result(messagesByGroupName, 60.seconds)
                logger.info("Group Messages:" + groupmessages)
                complete(groupmessages)
              }
              catch {
                case _: TimeoutException =>
                  logger.error("Timeout exception while reading data")
                  complete(JsonResponse("Reading file timeout."))
              }
            }
          } ~
          /**
           * It displays all the group names that user can send messages to
           * @input : It accepts sender name decoded from token
           *  @returns: All the messages with sender, group name and messages
           */
          path("viewGroups") {
            post {
              TokenAuthorization.authenticated { token =>
                val sender = token.values.toList.last.toString()
                val groupsOfSender = MongoDatabase.collectionForGroup.find(equal("sender", sender)).toFuture()
                logger.info("groups in which sender is added: " + groupsOfSender)
                complete(groupsOfSender)
              }
            }
          } ~
           /**
           * It accepts single slash as input if given by user
           * @return Response to enter proper URL.
           */
        pathEndOrSingleSlash {
          get {
            logger.info("Inside get '/'")
            complete(JsonResponse("Please enter proper url"))
          }
        }
        }
}
