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

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.bridgelabz.database.{DatabaseService, MongoConfig}
import com.bridgelabz.routes.UserManagementRoutes
import com.bridgelabz.services.UserManagementService
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class ChatAppJwtTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest {
  val mongoConfig = new MongoConfig
  val databaseService = new DatabaseService(mongoConfig)
  val userManagementService = new UserManagementService(databaseService)
  val routes = new UserManagementRoutes(userManagementService,mongoConfig,databaseService)
  "A router" should {
  "return unauthorized" in {
    val jsonRequest = ByteString(
      s"""
         |{
         |  "receiver":"xyz@gmail.com",
         |   "message":"Hey ruchir"
         |}
              """.stripMargin)
    Post("/user/authorize").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
      addCredentials(OAuth2BearerToken("anyrandomtoken")) ~> Route.seal(routes.routes) ~> check {
      status shouldBe StatusCodes.Unauthorized
    }
  }
}
  "A router" should {
    "return internal server error" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "receiver":"xyz@gmail.com",
           |   "message":"Hey ruchir"
           |}
              """.stripMargin)
      Post("/user/authorize").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
        addCredentials(OAuth2BearerToken(null)) ~> Route.seal(routes.routes) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }
  }

}

