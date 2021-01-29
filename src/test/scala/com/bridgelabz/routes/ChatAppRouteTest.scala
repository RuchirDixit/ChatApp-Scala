
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
      Post("/user/register", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.Unauthorized)
      }
    }
  }
}
