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

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.bridgelabz.caseclasses.TokenCase
import com.bridgelabz.jwt.TokenAuthorization
import com.bridgelabz.services.UserManagementService
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

class ChatAppRouteTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest with MockitoSugar  {
  "A Router" should {
    "register successfully" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "name":"ruchirtd111@gmail.com",
           |    "password":"demo"
           |}
              """.stripMargin)
      Post("/user/register", HttpEntity(MediaTypes.`application/json`, jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check {
        assert(status == StatusCodes.OK)
      }
    }
  }

  "A Router" should {
    "return bad request" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "name":"@gmail.com",
           |    "password":"demo"
           |}
              """.stripMargin)
      Post("/user/register", HttpEntity(MediaTypes.`application/json`, jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check {
        assert(status == StatusCodes.BadRequest)
      }
    }
  }

    "A router" should {
      "login successfully" in {
        val jsonRequest = ByteString(
          s"""
             |{
             |    "name":"ruchir99@gmail.com",
             |    "password":"demoemail",
             |    "isVerified":true
             |}
              """.stripMargin)
        Post("/user/login", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
          new UserManagementRoutes(new UserManagementService).routes ~> check
        {
          assert(status == StatusCodes.OK)
        }
      }
  }
  "A router" should {
    "return unavailable for legal reasons" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "name":"xyz@gmail.com",
           |    "password":"1234",
           |    "isVerified":false
           |}
              """.stripMargin)
      Post("/user/login", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.UnavailableForLegalReasons)
      }
    }
  }
  "A router" should {
    "return unauthorized" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "name":"ruchir99@gmail.com",
           |    "password":"de",
           |    "isVerified":true
           |}
              """.stripMargin)
      Post("/user/login", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.Unauthorized)
      }
    }
  }
  "A router" should {
    "return verified" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "sender":"ruchir99@gmail.com",
           |    "receiver":"xyz@gmail.com"
           |}
              """.stripMargin)
      Post("/user/messages", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.OK)
      }
    }
  }
  "A router" should {
    "return internal error" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "receiver":"demogroup",
           |    "message":"hey"
           |}
              """.stripMargin)
      Post("/user/groupChat", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.InternalServerError)
      }
    }
  }
  "A router" should {
    "return 500 error" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |
           |}
              """.stripMargin)
      Post("/user/viewGroups", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.InternalServerError)
      }
    }
  }
  "A router" should {
    "return OK for authorize" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "receiver":"xyz@gmail.com",
           |   "message":"Hey ruchir"
           |}
              """.stripMargin)
      val service = new UserManagementService
      val id = service.returnId("xyz@gmail.com")
      val tokenCase = TokenCase("ruchirtd96@gmail.com",id)
      val token = TokenAuthorization.generateToken(tokenCase)
      Post("/user/authorize").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
        addCredentials(OAuth2BearerToken(token)) ~> Route.seal(new UserManagementRoutes(new UserManagementService).routes) ~> check {
        status shouldBe StatusCodes.OK
      }
    }
  }
  "A router" should {
    "return bad request for authorize" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "receiver":null,
           |   "message":"Hey ruchir"
           |}
              """.stripMargin)
      val service = new UserManagementService
      val id = service.returnId(null)
      val tokenCase = TokenCase(null,id)
      val token = TokenAuthorization.generateToken(tokenCase)
      Post("/user/authorize").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
        addCredentials(OAuth2BearerToken(token)) ~> Route.seal(new UserManagementRoutes(new UserManagementService).routes) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }

  "A router" should {
    "return 401 Unauthorized for authorize" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "receiver":"hey@rediffmail.com",
           |   "message":"Hey ruchir"
           |}
              """.stripMargin)
      val service = new UserManagementService
      val id = service.returnId("hey@rediffmail.com")
      val tokenCase = TokenCase("hey@rediffmail.com",id)
      val token = TokenAuthorization.generateToken(tokenCase)
      Post("/user/authorize").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
        addCredentials(OAuth2BearerToken(token)) ~> Route.seal(new UserManagementRoutes(new UserManagementService).routes) ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }
  }

  "A router" should {
    "return OK for GroupChat" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "receiver":"xyz@gmail.com",
           |   "message":"Hey ruchir"
           |}
              """.stripMargin)
      val service = new UserManagementService
      val id = service.returnId("xyz@gmail.com")
      val tokenCase = TokenCase("ruchirtd96@gmail.com",id)
      val token = TokenAuthorization.generateToken(tokenCase)
      Post("/user/groupChat").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
        addCredentials(OAuth2BearerToken(token)) ~> Route.seal(new UserManagementRoutes(new UserManagementService).routes) ~> check {
        status shouldBe StatusCodes.OK
      }
    }
  }
  "A router" should {
    "return OK for ViewGroups" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |
           |}
              """.stripMargin)
      val service = new UserManagementService
      val id = service.returnId("xyz@gmail.com")
      val tokenCase = TokenCase("ruchirtd96@gmail.com",id)
      val token = TokenAuthorization.generateToken(tokenCase)
      Post("/user/viewGroups").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
        addCredentials(OAuth2BearerToken(token)) ~> Route.seal(new UserManagementRoutes(new UserManagementService).routes) ~> check {
        status shouldBe StatusCodes.OK
      }
    }
  }
  "A router" should {
    "return OK" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |
           |}
              """.stripMargin)
      Get("/user/", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.OK)
      }
    }
  }
  "A router" should {
    "return check if valid" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "sender":"ruchir99@gmail.com",
           |    "receiver":"@gmail.com"
           |}
              """.stripMargin)
      Post("/user/messages", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.OK)
      }
    }
  }
  "A router" should {
    "return check if receiver group exists" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "sender":"ruchir99@gmail.com",
           |    "receiver":"",
           |    "message":"messages in test"
           |}
              """.stripMargin)
      Post("/user/groupChat", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assertThrows _
      }
    }
  }
  "A router" should {
    "return unauthorized viewGroups" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "receiver":"xyz@gmail.com",
           |   "message":"Hey ruchir"
           |}
              """.stripMargin)
      Post("/user/viewGroups").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
        addCredentials(OAuth2BearerToken("anyrandomtoken")) ~> Route.seal(new UserManagementRoutes(new UserManagementService).routes) ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }
  }
  "A router" should {
    "return Unauthorized group chat" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "receiver":"xyz@gmail.com",
           |   "message":"Hey ruchir"
           |}
              """.stripMargin)
      Post("/user/groupChat").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
        addCredentials(OAuth2BearerToken("randomtoken")) ~> Route.seal(new UserManagementRoutes(new UserManagementService).routes) ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }
  }

  "A router" should {
    "return GroupMessages OK" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "groupName":"daundboys"
           |}
              """.stripMargin)
      Post("/user/groupMessages", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.OK)
      }
    }
  }
  "A router" should {
    "return bad request authorize" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "receiver":null,
           |   "message":"Hey ruchir"
           |}
              """.stripMargin)
      val service = new UserManagementService
      val id = service.returnId(null)
      val tokenCase = TokenCase(null,id)
      val token = TokenAuthorization.generateToken(tokenCase)
      Post("/user/groupChat").withEntity(ContentTypes.`application/json`, jsonRequest) ~>
        addCredentials(OAuth2BearerToken(token)) ~> Route.seal(new UserManagementRoutes(new UserManagementService).routes) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }
  "A router" should {
    "return get some" in {
      Get("/user/verify") ~> new UserManagementRoutes(new UserManagementService).routes ~> check {
        handled shouldBe false
      }
    }
  }

  "A router" should {
    "return true for get request" in {
      Get("/user/verify?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6LTIxNDc0ODM2NDgsIm5hbWUiOiJydWNoaXJ0ZDk2QGdtYWlsLmNvbSJ9." +
        "iZaPfKKAcmiREDuUytdaPnVeh30VoTtTpneiX6HQiPY&name=hello@gmail.com")~> new UserManagementRoutes(new UserManagementService).routes ~> check {
        handled shouldBe true
      }
    }
  }

  "A router" should {
    "return get some ver" in {
      val service = new UserManagementService
      val id = service.returnId("ruchir99@gmail.com")
      val tokenCase = TokenCase("ruchir99@gmail.com",id)
      val token = TokenAuthorization.generateToken(tokenCase)
      Get("/user/verify?token=" + token + "&name=ruchir99@gmail.com")~> new UserManagementRoutes(new UserManagementService).routes ~> check {
        handled shouldBe true
      }
    }
  }
}
