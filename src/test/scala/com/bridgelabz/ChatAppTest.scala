package com.bridgelabz
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.util.ByteString
import com.bridgelabz.Main.{UserManagementRoutes, UserManagementService}
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
class ChatAppTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest {

  "A Router" should {
    "register successfully" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "email":"ruchirtd96@gmail.com",
           |    "password":"demo"
           |}
              """.stripMargin)
      Post("/user/register", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~> new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        status shouldBe StatusCodes.OK
      }
    }
    "login successfully" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "email":"ruchirtd96@gmail.com",
           |    "password":"demo",
           |    "isVerified":false
           |}
              """.stripMargin)
      Post("/user/login", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~> new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        status shouldBe StatusCodes.UnavailableForLegalReasons
      }
    }
    "Should not send message" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "sender":"ruchirtd96@gmail.com",
           |    "receiver":"demo@gmail.com",
           |    "message":"Helloworld"
           |}
              """.stripMargin)
      Post("/user/protectedcontent", HttpEntity(MediaTypes.`application/json`,jsonRequest)) ~> new UserManagementRoutes(new UserManagementService).routes ~> check
      {
        status shouldBe StatusCodes.Unauthorized
      }
    }
  }
}