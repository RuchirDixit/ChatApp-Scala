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

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.bridgelabz.services.UserManagementService
import com.typesafe.sslconfig.ssl.FakeChainedKeyStore.User
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when

class ChatAppRouteTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest with MockitoSugar  {
  "A Router" should {
    "register successfully" in {
      val regMock = mock[UserManagementService]
      val jsonRequest = ByteString(
        s"""
           |{
           |    "name":"ruchirtd302@gmail.com",
           |    "password":"demo"
           |}
              """.stripMargin)
      Post("/user/register", HttpEntity(MediaTypes.`application/json`, jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check {
        assert(status == StatusCodes.OK)
      }
    }
  }
  "A router" should {
    "register user exists" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "name":"ruchirtd96@gmail.com",
           |    "password":"demo"
           |}
              """.stripMargin)
      Post("/user/register", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.Unauthorized)
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
    "return internal server error for authorize" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "receiver":"xyz@gmail.com",
           |   "message":"Hey ruchir"
           |}
              """.stripMargin)
      Post("/user/authorize", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.InternalServerError)
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
}
