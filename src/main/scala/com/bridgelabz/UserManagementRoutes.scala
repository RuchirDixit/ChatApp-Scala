package com.bridgelabz.Main
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.bridgelabz.User
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
class UserManagementRoutes(service: UserManagementService) extends PlayJsonSupport with LazyLogging {
  val routes: Route =
    pathPrefix("user") {
      // for login using post request, returns success on successful login or else returns unauthorized
      path("login") {
        (post & entity(as[User])) { loginRequest =>
          logger.info("Login response: " + service.userLogin(loginRequest))
          if (service.userLogin(loginRequest) == "Login Successful") {
            complete((StatusCodes.OK, "Successfully logged in!"))
          } else {
            complete(StatusCodes.Unauthorized,"Invalid credentials. User not found!")
          }
        }
      } ~
        // to register user using post request, returns success on successful registration or else returns Cannot registered
        path("register") {
          (post & entity(as[User])) { createUserRequest =>
            if (service.createUser(createUserRequest) == "User created") {
              complete((StatusCodes.OK, "User Registered!"))
            } else {
              complete(StatusCodes.BadRequest -> "Error! User already exists!")
            }
          }
        }
    }
}
