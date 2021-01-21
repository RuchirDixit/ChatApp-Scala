
package com.bridgelabz

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import com.typesafe.scalalogging.LazyLogging

object TokenAuthorization extends LazyLogging{
  private val secretKey = "super_secret_key"
  private val header = JwtHeader("HS256")
  private val tokenExpiryPeriodInDays = 1

  /**
   *
   * @param username : using username field to generate token
   * @return: : String as a token for authentication
   */
  def generateToken(username: String,id:Int): String = {
    logger.info("in generate token")

    val claims = JwtClaimsSet(
      Map(
        "id" -> id,
        "name" -> username
       // "expiredAt" -> (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(tokenExpiryPeriodInDays)).toString
      )
    )
    JsonWebToken(header, claims, secretKey)
  }

  /**
   *
   * @return : Map with value whether authenticated or not
   */
  def authenticated: Directive1[Map[String, Any]] = {

    optionalHeaderValueByName("Authorization").flatMap { tokenFromUser =>

      val jwtToken = tokenFromUser.get.split(" ")
      jwtToken(1) match {
//        case token if isTokenExpired(token) =>
//          complete(StatusCodes.Unauthorized -> "Session expired.")

        case token if JsonWebToken.validate(token, secretKey) =>
          provide(getClaims(token))

        case _ =>  complete(StatusCodes.Unauthorized ->"Invalid Token")
      }
    }
  }

  // checks whether is token is expired or not
//  private def isTokenExpired(jwt: String): Boolean =
//    getClaims(jwt).get("expiredAt").exists(_.toLong < System.currentTimeMillis())

  // Value value inside token or else return empty
  private def getClaims(jwt: String): Map[String, String] =
    JsonWebToken.unapply(jwt) match {
      case Some(value) => value._2.asSimpleMap.getOrElse(Map.empty[String, String])
      case None => Map.empty[String, String]
    }
}
