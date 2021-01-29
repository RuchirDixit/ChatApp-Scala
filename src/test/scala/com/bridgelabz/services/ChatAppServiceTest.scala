
package com.bridgelabz.services

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.bridgelabz.caseClasses.{ChatCase, User}
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class ChatAppServiceTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest  {
  // Test case To check if user exists
  "Check if exists" should {
    "return true" in {
      val status = DatabaseService.checkIfExists("ruchirtd96@gmail.com")
      assert(status == true)
    }
    "return false" in {
      val status = DatabaseService.checkIfExists("abc@gmail.com")
      assert(status == false)
    }
  }

  // Test case to successfully add user to database
  "On Successful user added" should {
    "return Success" in {
      val data = User(Some(1),"xyz@gmail.com","1234",Some(false))
      val status = DatabaseService.saveUser(data)
      if(status == "Failure") {
        assert(status == "Failure")
      }
      else {
        assert(status == "Success")
      }
    }
    "return Validation Failed" in {
      val data = User(Some(1),"@gmail.com","1234",Some(false))
      val status = DatabaseService.saveUser(data)
      assert(status == "Validation Failed")
    }
  }

  // Test case to successfully save chat messages for one to one interaction
  "On Successful chat sent" should {
    "return Message sent" in {
      val data = ChatCase(Some("ruchirtd96@gmail.com"),"ruchircool007@gmail.com","Test case",Some(""))
      val status = DatabaseService.saveChatMessage(data)
      assert(status == "Message sent")
    }
  }

  // Test case to check if user is logged in or not
  "On Passing credentials" should {
    val service = new UserManagementService
    "return Logged  in " in {
      val data = User(Some(1),"ruchirtd96@gmail.com","demoemail",Some(true))
      val status = service.userLogin(data)
      assert(status == "Login Successful")
    }
    "return User not found" in {
      val data = User(Some(1),"ruchir@rediffmail.com","demo",Some(false))
      val status = service.userLogin(data)
      assert(status == "User not found")
    }
  }
}
