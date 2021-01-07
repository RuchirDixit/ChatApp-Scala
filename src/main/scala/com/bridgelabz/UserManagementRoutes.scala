package com.bridgelabz.Main
import akka.actor.{ActorSystem, Props}

import concurrent.duration._
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthenticationFailedRejection, Route}
import authentikat.jwt.JsonWebToken
import com.bridgelabz.Main.Main.actorSystem
import com.bridgelabz.{Chat, SaveToDatabaseActor, TokenAuthorization, User}
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
    pathPrefix("user") {
      // for login using post request,
      // It accepts name and password as body and generates jwt token based on name
      // returns success on successful login or else returns unauthorized
      path("login") {
        (post & entity(as[User])) { loginRequest =>
          logger.info("Login response: " + service.userLogin(loginRequest))
          if (service.userLogin(loginRequest) == "Login Successful") {
            val token: String = TokenAuthorization.generateToken(loginRequest.name)
            complete((StatusCodes.OK, token))
          }
          else if (service.userLogin(loginRequest) == "User Not verified") {
            complete(StatusCodes.UnavailableForLegalReasons,"User's Email Id is not verified!")
          } else {
            complete(StatusCodes.Unauthorized,"Invalid credentials. User not found! Try again with correct details.")
          }
        }
      } ~
        // to verify user using post request,
        // It fetches user by decoding the token and updates the isVerified field to true
        // returns success on successful verification of user and error if verification failed
        path("verify"){
          get {
            parameters('token.as[String],'name.as[String]) {
              (token,name) =>
                val jwsObject = JWSObject.parse(token)
                val updateUserAsVerified = MongoDatabase.collectionForUserRegistration.updateOne(equal("name",name),set("isVerified",true)).toFuture()
                Await.result(updateUserAsVerified,60.seconds)
                if(jwsObject.getPayload.toJSONObject.get("name").equals(name)){
                  complete("User successfully verified and registered!")
                }
                else {
                  complete("User could not be verified! Please verify using the mail sent.")
                }
            }
          }
        } ~
        // to register user using post request,
        // It generates jwt token on successful registration of user and sends email to user for verification
        // returns success on successful registration of user or else error message if user already exists
        path("register") {
          (post & entity(as[User])) { createUserRequest =>
            if (service.createUser(createUserRequest) == "User created") {
              val token: String = TokenAuthorization.generateToken(createUserRequest.name)
              val mailer = Mailer("smtp.gmail.com", 587)
                .auth(true)
                .as(sys.env("sender_email"),sys.env("sender_password"))
                .startTls(true)()
              mailer(Envelope.from(new InternetAddress(sys.env("sender_email")))
                .to(new InternetAddress(createUserRequest.name))
                .subject("Token")
                .content(Text("Thanks for registering! Click on this link to verify your email address: http://localhost:8081/user/verify?token="
                  +token+"&name="+createUserRequest.name)))
                .onComplete {
                  case Success(_) => println("Message delivered. Email verified!")
                  case Failure(_) => println("Failed to verify user!")
                }
              complete((StatusCodes.OK, "User Registered! Now you can Login using these details."))
            } else {
              complete(StatusCodes.BadRequest -> "Error! User already exists! Please try with different credentials.")
            }
          }
        } ~
        // Post request to save message into collection by use of actors;
        // It sends message request data to SaveToDatabaseActor and after saving data receives response and prints it accordingly
        // Returns success if message is sent to receiver or else gives error as message not delivered
//        path("saveMessage") {
//          (post & entity(as[Chat])) { createUserRequest =>
//            optionalHeaderValueByName("Authorization").map(token =>
//              println("Tokeeen isssssss ::::::::: ==============> "+token))
//            val saveMessageActor = system.actorOf(Props[SaveToDatabaseActor],"saveMessageActor")
//            implicit val timeout = Timeout(10.seconds)
//            val futureResponse = saveMessageActor ? createUserRequest
//            val result = Await.result(futureResponse,60.seconds)
//            if (result.equals("Message added")){
//              complete(StatusCodes.OK,"Message sent to receiver!")
//            }
//            else {
//              complete(StatusCodes.BadRequest -> "Message could not be delivered! Try sending all the data correctly again.")
//            }
//          }
//        }
        path("protectedcontent") {
          (get & entity(as[User])) { getData =>
            TokenAuthorization.authenticated { token =>
              val response = service.protectedContent
              println(token)
              complete(getData.name)
            }
          }
        }
    }
}
