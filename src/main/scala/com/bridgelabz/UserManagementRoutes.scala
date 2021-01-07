package com.bridgelabz.Main
import akka.actor.{ActorSystem, Props}

import concurrent.duration._
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
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
      // It accepts name and password as body and token as header
      // returns success on successful login or else returns unauthorized
      path("login") {
        (post & entity(as[User])) { loginRequest =>
          logger.info("Login response: " + service.userLogin(loginRequest))
          if (service.userLogin(loginRequest) == "Login Successful") {
            complete((StatusCodes.OK, "Successfully logged in!"))
          }
          else if (service.userLogin(loginRequest) == "User Not verified") {
            complete(StatusCodes.UnavailableForLegalReasons,"User's Email Id is not verified!")
          } else {
            complete(StatusCodes.Unauthorized,"Invalid credentials. User not found! Try again with correct details.")
          }
        }
      } ~
        // to verify whether user's email id exists or not. If user reaches this path that means his email id is authentic.
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
                  complete("User could not be verified!")
                }
            }
          }
        } ~
        // to register user using post request, returns success on successful registration or else returns Cannot registered
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
        // to save message into collection by use of actors;
        // It sends message request data to SaveToDatabaseActor and after saving data receives response and prints it accordingly
        path("saveMessage") {
          (post & entity(as[Chat])) { createUserRequest =>
            val saveMessageActor = system.actorOf(Props[SaveToDatabaseActor],"saeMessageActor")
            implicit val timeout = Timeout(10.seconds)
            val futureResponse = saveMessageActor ? createUserRequest
            val result = Await.result(futureResponse,60.seconds)
            if (result.equals("Message added")){
              complete((StatusCodes.OK),"Message sent to receiver!")
            }
            else {
              complete(StatusCodes.BadRequest -> "Message could not be delivered! Try sending all the data correctly again.")
            }
          }
        }
    }
}
