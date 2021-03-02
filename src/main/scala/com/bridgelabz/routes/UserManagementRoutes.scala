// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.bridgelabz.routes

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.bridgelabz.actors.{ActorSystemFactory, EmailNotificationActor}
import com.bridgelabz.caseclasses._
import com.bridgelabz.database.interfaces.{IDatabaseService, IDatabaseConfig}
import com.bridgelabz.database.SaveToDatabaseActor
import com.bridgelabz.email.Email
import com.bridgelabz.jwt.TokenAuthorization
import com.bridgelabz.marshallers.{ChatCaseJsonProtocol, GroupChatJsonProtocol, JsonResponseProtocol}
import com.bridgelabz.services.interfaces.IUserManagementService
import com.nimbusds.jose.JWSObject
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, TimeoutException}
import scala.util.{Failure, Success}

class UserManagementRoutes(service: IUserManagementService, databaseConfig: IDatabaseConfig,
                           databaseService: IDatabaseService) extends PlayJsonSupport with LazyLogging
  with ChatCaseJsonProtocol with JsonResponseProtocol with GroupChatJsonProtocol {
  implicit val system = ActorSystemFactory.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val routes: Route =
    pathPrefix("user") {
        /**
         * for login using post request and sending token in headers and Status code as OK
         * @input : It accepts id, name and password as body and generates jwt token based on name
         * @Return :  success on successful login or else returns unauthorized
         */
        path("login") {
          (post & entity(as[User])) { loginRequest =>
            onComplete(service.userLogin(loginRequest)) {
              case Success(response) =>
                if (response == "Login Successful") {
                  val id = service.returnId(loginRequest.name)
                  val tokenCase = TokenCase(loginRequest.name,id)
                  val token: String = TokenAuthorization.generateToken(tokenCase)
                  respondWithHeaders(RawHeader("token",token)) {
                    complete(StatusCodes.OK,JsonResponse("Login successful."))
                  }
                }
                else if (response == "User Not verified") {
                  logger.error("User's email not verified")
                  complete(StatusCodes.UnavailableForLegalReasons,JsonResponse("User's Email Id is not verified!"))
                } else {
                  logger.error("Invalid credentials")
                  complete(StatusCodes.Unauthorized, JsonResponse("Invalid credentials. User not found! Try again with correct details."))
                }
            }
          }
        } ~
        /**
         * It fetches user by decoding the token and updates the isVerified field to true
         * @input : It accepts parameters token and name from the link
         * @return :  success on successful verification of user and error if verification failed
         */
        path("verify") {
          get {
            parameters('token.as[String], 'name.as[String]) {
              (token, name) =>
                val jwsObject = JWSObject.parse(token)
                if (jwsObject.getPayload.toJSONObject.get("name").equals(name)) {
                 val updateUserAsVerified = databaseConfig.updateUser(name)
                  onComplete(updateUserAsVerified) {
                    case Success(_) =>
                      logger.info("Successfully verified user!")
                      complete(StatusCodes.OK, JsonResponse("User successfully verified and registered!"))
                  }
                }
                else {
                  logger.error("User not verified.")
                  complete(StatusCodes.BadRequest -> JsonResponse("User could not be verified! Please verify using the mail sent."))
                }
            }
          }
        } ~
        /**
         * It fetches all the messages inside group chat name mentioned in as query parameter
         * @input : It accepts group name as parameter
         * @return : All the messages on success and error if group chat name not found
         */
        path("messages") {
          (post & entity(as[Messages])) { messageRequest =>
            try {
              val senderId = service.returnId(messageRequest.sender)
              val receiverId = service.returnId(messageRequest.receiver)
              val roomname = service.generateGroupChatName(messageRequest.sender, messageRequest.receiver, senderId.toString, receiverId)
              val messagesByGroupName = databaseConfig.findChat(roomname)
              onComplete(messagesByGroupName) {
                case Success(groupMessages) =>
                  logger.info("Successfully fetched roomname")
                  complete(groupMessages)
                case Failure(error) =>
                  logger.error("Error while fetching roomname")
                  // $COVERAGE-OFF$
                  complete(error)
              }
            }
            catch {
              case _: TimeoutException =>
                logger.error("Timeout exception for getting messages on roomname")
                // $COVERAGE-OFF$
                complete(JsonResponse("Reading file timeout."))
              case exception: Exception =>
                logger.error(exception.toString)
                // $COVERAGE-OFF$
                complete(JsonResponse(exception.toString))
            }
          }
        } ~
          /**
           * It generates jwt token on successful registration of user and sends email to user for verification
           * @input : It accepts id,name,password
           * @return : success on successful registration of user or else error message if user already exists
           */
          path("register") {
            (post & entity(as[User])) { createUserRequest =>
              val userCreatedResponse = service.createUser(createUserRequest)
              onComplete(userCreatedResponse) {
                case Success(response) =>
                  if (response == "User created") {
                    logger.info("User object in register:" + createUserRequest)
                    val a:Option[Any] = Some(createUserRequest.id)
                    val id = (a match {
                      case Some(x:Int) => x
                      case _ => Int.MinValue
                    })
                    val tokenCase = TokenCase(createUserRequest.name,id)
                    val token: String = TokenAuthorization.generateToken(tokenCase)
                    val email = EmailCase(createUserRequest.name,"Token","Thanks for registering! Click on this link to verify your email address: http://"
                      + sys.env("HOST") + ":" + sys.env("PORT") + "/user/verify?token="
                      + token + "&name=" + createUserRequest.name)
                    Email.sendEmail(email).onComplete {
                        case Success(_) =>
                          // $COVERAGE-OFF$
                          val schedulerActor = system.actorOf(Props[EmailNotificationActor], "schedulerActor")
                          system.scheduler.schedule(30.seconds, 120.seconds, schedulerActor, createUserRequest.name)
                          logger.info("Message delivered. Email verified!")
                        case Failure(_) =>
                          logger.error("Failed to deliver mail.")
                          // $COVERAGE-OFF$
                          complete(StatusCodes.NotFound, JsonResponse("Failed To Deliver Mail. Please check the email again."))
                      }
                    complete((StatusCodes.OK,JsonResponse("User Registered! Now you can Login using these details.")))
                  } else if (response == "User Validation Failed") {
                    logger.error("Email address not correct according to pattern")
                    complete(StatusCodes.BadRequest -> JsonResponse("Error! Please enter correct email address."))
                  }
                  // $COVERAGE-OFF$
                  else {
                    logger.error("User already exists")
                    complete(StatusCodes.Unauthorized, JsonResponse("Error! User already exists!"))
                  }
              }
            }
          } ~
          /**
           * It checks if receiver is a registered user if not throws message to register, if yes Saved message to Database
           * @input: It accepts token as authorization parameter, and sender, receiver and message to save
           * @return:  success message if message delivered or error message if not delivered.
           */
          path("authorize") {
            (post & entity(as[ChatCase])) { getDataToSaveChats =>
              TokenAuthorization.authenticated { token =>
                logger.info("Token:" + token.values.toList.last.toString())
                logger.info("receiver: " + getDataToSaveChats.receiver)
                if (databaseService.checkIfExists(getDataToSaveChats.receiver) == true) {
                  val receiverId = service.returnId(getDataToSaveChats.receiver)
                  val senderId = token.values.toList.head.toString
                  val groupChatName = service.generateGroupChatName(token.values.toList.last.toString(), getDataToSaveChats.receiver, senderId, receiverId)
                  val saveToChat = getDataToSaveChats.copy(sender = Some(token.values.toList.last.toString()),groupChatName = Some(groupChatName))
                  implicit val timeout = Timeout(10.seconds)
                  val saveMessageActor = system.actorOf(Props(new SaveToDatabaseActor(service)), "saveActor")
                  val futureResponse = saveMessageActor ? saveToChat
                  onComplete(futureResponse) {
                    case Success(result) =>
                      if (result.equals("Message added")) {
                        complete(StatusCodes.OK, JsonResponse("Message sent to receiver!"))
                      }
                      // $COVERAGE-OFF$
                      else {
                        logger.error("Message could not be delivered")
                        complete(StatusCodes.BadRequest -> JsonResponse("Message could not be delivered! Try sending all the data correctly again."))
                      }
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
           * It saves messages from sender to a particular group mentioned by user
           * @input: It accepts token as authorization parameter, receiver (groupname) and message to save
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
                val messagesByGroupName = databaseConfig.findGroupUsingReceiver(groupname)
                onComplete(messagesByGroupName) {
                  case Success(groupMessages) => complete(groupMessages)
                  case Failure(error) => complete(error)
                }
              }
              catch {
                case _: TimeoutException =>
                  logger.error("Timeout exception while reading data")
                  complete(JsonResponse("Reading file timeout."))
                case exception: Exception =>
                  logger.error(exception.toString)
                  complete(JsonResponse(exception.toString))
              }
            }
          } ~
          /**
           * It displays all the group names that user can send messages to
           * @input : It accepts sender name decoded from token
           *  @returns: All the messages with sender, group name and message s
           */
          path("viewGroups") {
            post {
              TokenAuthorization.authenticated { token =>
                val sender = token.values.toList.last.toString()
                val groupsOfSender = databaseConfig.findGroupUsingSender(sender)
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
