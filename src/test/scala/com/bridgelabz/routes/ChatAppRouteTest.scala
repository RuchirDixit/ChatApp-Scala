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
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class ChatAppRouteTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest  {
  "A Router" should {
    "register successfully" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "name":"ruchirtd96@gmail.com",
           |    "password":"demo"
           |}
              """.stripMargin)
      Post("/user/register", HttpEntity(MediaTypes.`application/json`, jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check {
        assert(status == StatusCodes.Unauthorized)
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

    "A router" should {
      "login successfully" in {
        val jsonRequest = ByteString(
          s"""
             |{
             |    "name":"ruchirtd96@gmail.com",
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
}
