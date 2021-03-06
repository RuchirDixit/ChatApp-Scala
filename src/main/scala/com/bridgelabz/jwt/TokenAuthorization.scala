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
package com.bridgelabz.jwt

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import com.typesafe.scalalogging.LazyLogging

object TokenAuthorization extends LazyLogging{
  private val secretKey = "super_secret_key"
  private val header = JwtHeader("HS256")

  /**
   * To generate token using id and name
   * @param username : using username field to generate token
   * @return: : String as a token for authentication
   */
  def generateToken(username: String,id:Int): String = {
    logger.info("in generate token")

    val claims = JwtClaimsSet(
      Map(
        "id" -> id,
        "name" -> username
      )
    )
    JsonWebToken(header, claims, secretKey)
  }

  /**
   * To authenticate user based on token
   * @return : Map with value whether authenticated or not
   */
  def authenticated: Directive1[Map[String, Any]] = {

    optionalHeaderValueByName("Authorization").flatMap { tokenFromUser =>

      val jwtToken = tokenFromUser.get.split(" ")
      jwtToken(1) match {
        case token if JsonWebToken.validate(token, secretKey) =>
          provide(getClaims(token))

        case _ =>
          logger.warn("Unauthorized user")
          complete(StatusCodes.Unauthorized ->"Invalid Token")
      }
    }
  }


  /**
   * To get value of if exists or empty
   * @param jwt: Token string
   * @return: Map[String,String]
   */
  private def getClaims(jwt: String): Map[String, String] =
    JsonWebToken.unapply(jwt) match {
      case Some(value) => value._2.asSimpleMap.getOrElse(Map.empty[String, String])
      case None => Map.empty[String, String]
    }
}
