
package com.bridgelabz

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.util.ByteString
import com.bridgelabz.Main.{UserManagementRoutes, UserManagementService}
import org.scalatest.wordspec.AnyWordSpec

class ChatAppTest extends AnyWordSpec with ScalatestRouteTest {

  "A Router" should {
    "register successfully" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "email":"ruchirtd96@gmail.com",
           |    "password":"demo"
           |}
              """.stripMargin)
      Post("/user/register", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~>
        new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        assert(status == StatusCodes.OK)
      }
    }
  }
}