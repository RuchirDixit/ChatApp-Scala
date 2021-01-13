package com.bridgelabz.Main

import akka.actor.{ActorSystem, Props}
import concurrent.duration._
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.bridgelabz.{Chat, EmailNotificationActor, SaveToDatabaseActor, TokenAuthorization, User}
import com.nimbusds.jose.JWSObject
import com.typesafe.scalalogging.LazyLogging
import courier.{Envelope, Mailer, Text}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import javax.mail.internet.InternetAddress
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.util.{Failure, Success}

class UserManagementRoutes(service: UserManagementService) extends PlayJsonSupport with LazyLogging {
  val system = ActorSystem("Chat-App")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val routes: Route =
    // TODO : for '/' API handle
    pathPrefix("user") {
      /**
       * for login using post request,
       * It accepts id, name and password as body and generates jwt token based on name
       * Returns success on successful login or else returns unauthorized
       */
      path("login") {
        (post & entity(as[User])) { loginRequest =>
          logger.info("Login response: " + service.userLogin(loginRequest))
          if (service.userLogin(loginRequest) == "Login Successful") {
            val id = service.returnId(loginRequest.name)
            val token: String = TokenAuthorization.generateToken(loginRequest.name,id)
            complete((StatusCodes.OK, token))
          }
          else if (service.userLogin(loginRequest) == "User Not verified") {
            complete(StatusCodes.UnavailableForLegalReasons,"User's Email Id is not verified!")
          } else {
            complete(StatusCodes.Unauthorized,"Invalid credentials. User not found! Try again with correct details.")
          }
        }
      } ~
      /**
         * to verify user using post request,
         * It accepts parameters token and name from the link
         * It fetches user by decoding the token and updates the isVerified field to true
         * returns success on successful verification of user and error if verification failed
         */
        path("verify"){
          get {
            parameters('token.as[String],'name.as[String]) {
              (token,name) =>
                val jwsObject = JWSObject.parse(token)
                val updateUserAsVerified = MongoDatabase.collectionForUserRegistration.updateOne(equal("name",name),set("isVerified",true)).toFuture()
                Await.result(updateUserAsVerified,60.seconds)
                if(jwsObject.getPayload.toJSONObject.get("name").equals(name)) {
                  complete(StatusCodes.OK,"User successfully verified and registered!")
                }
                else {
                  complete(StatusCodes.BadRequest ->"User could not be verified! Please verify using the mail sent.")
                }
            }
          }
        } ~
        /**
         * to get all messages using get request,
         * It accepts group name as parameter
         * It fetches all the messages inside group chat name mentioned in as query parameter
         * returns All the messages on success and error if group chat name not found
         */
        path("getMessages"){
          get {
            // TODO: room name
            parameters('roomname.as[String]) {
              (roomname) =>
                val messagesByGroupName = MongoDatabase.collectionForChat.find(equal("groupChatName",roomname)).toFuture()
                val messages = Await.result(messagesByGroupName,60.seconds)
                // TODO: Covert to json to print
                println("Messages:"+messages)
                complete("")
            }
          }
        } ~
      /**
         * to register user using post request,
         * It accepts id,name,password
         * It generates jwt token on successful registration of user and sends email to user for verification
         * returns success on successful registration of user or else error message if user already exists
         */
        path("register") {
          (post & entity(as[User])) { createUserRequest =>
            val userCreatedResponse = service.createUser(createUserRequest)
            logger.info("Create response::"+userCreatedResponse)
            if (userCreatedResponse == "User created") {
              logger.info("User object in register:" + createUserRequest)
              val token: String = TokenAuthorization.generateToken(createUserRequest.name,createUserRequest.id)
              val mailer = Mailer(sys.env("mailer"), sys.env("smtp_port").toInt)
                .auth(true)
                .as(sys.env("sender_email"),sys.env("sender_password"))
                .startTls(true)()
              mailer(Envelope.from(new InternetAddress(sys.env("sender_email")))
                .to(new InternetAddress(createUserRequest.name))
                .subject("Token")
                .content(Text("Thanks for registering! Click on this link to verify your email address: http://"+sys.env("Host")+":"+sys.env("Port_number")+"/user/verify?token="
                  +token+"&name="+createUserRequest.name)))
                .onComplete {
                  case Success(_) =>
                    val schedulerActor = system.actorOf(Props[EmailNotificationActor],"schedulerActor")
                    system.scheduler.schedule(30.seconds,120.seconds,schedulerActor,createUserRequest.name)
                    logger.info("Message delivered. Email verified!")
                  case Failure(_) => complete(StatusCodes.NotFound,"Failed To Deliver Mail. Please check the email again.")
                }
              complete((StatusCodes.OK, "User Registered! Now you can Login using these details."))
            } else if(userCreatedResponse == "User Validation Failed") {
              complete(StatusCodes.BadRequest -> "Error! Please enter correct email address.")
            }
            else {
              complete(StatusCodes.Unauthorized,"Error! User already exists!")
            }
          }
        } ~
      /**
           * For getting token and decoding username of sender from token
           * It accepts token as authorization parameter, and sender, receiver and message to save
           * It checks if receiver is a registered user if not throws message to register, if yes Saved message to Database
           * Returns success message if message delivered or error message if not delivered.
           */
        path("protectedcontent") {
          // TODO: handle if token not passed
          (post & entity(as[Chat])) { getDataToSaveChats =>
            TokenAuthorization.authenticated { token =>
              logger.info("Token:"+token.values.toList.last.toString())
              //val tokenList = token.values.toList
              getDataToSaveChats.setSenderName(token.values.toList.last.toString())
              logger.info("receiver: "+ getDataToSaveChats.receiver)
              println(Database_service.checkIfExists(getDataToSaveChats.receiver))
              if(Database_service.checkIfExists(getDataToSaveChats.receiver) == true) {
                val receiverId = service.returnId(getDataToSaveChats.receiver)
                val senderId = token.values.toList.head.toString
                getDataToSaveChats.setReceiverName(getDataToSaveChats.receiver)
                getDataToSaveChats.setMessage(getDataToSaveChats.message)
                val userService = new UserManagementService
                val groupChatName = userService.generateGroupChatName(token.values.toList.last.toString(),getDataToSaveChats.receiver,senderId,receiverId)
                getDataToSaveChats.setGroupChatName(groupChatName)
                // TODO: Use singleton pattern for actors creation
                val saveMessageActor = system.actorOf(Props[SaveToDatabaseActor],"saveActor")
                implicit val timeout = Timeout(10.seconds)
                val futureResponse = saveMessageActor ? getDataToSaveChats
                val result = Await.result(futureResponse,60.seconds)
                if (result.equals("Message added")) {
                  complete(StatusCodes.OK,"Message sent to receiver!")
                }
                else {
                  complete(StatusCodes.BadRequest -> "Message could not be delivered! Try sending all the data correctly again.")
                }
              }
              else {
                complete(StatusCodes.Unauthorized -> "Receiver needs to be a registered user. Please register to continue communication.")
              }
            }
            }
          }
        }
}
