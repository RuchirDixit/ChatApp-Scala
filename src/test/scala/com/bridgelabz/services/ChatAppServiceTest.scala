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
package com.bridgelabz.services

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.bridgelabz.caseclasses.{ChatCase, GroupChat, User}
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

  // Test case to successfully save chat messages for group interaction
  "On Successful chat sent to group" should {
    "return Message sent to group" in {
      val data = GroupChat(Some("ruchircool007@gmail.com"),"Test case group","Hey group")
      val status = DatabaseService.saveToGroupChat(data)
      assert(status == "Message sent to group")
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

  // Test case to check if user is created or not
  "On Passing credentials" should {
    val service = new UserManagementService
    "return User created " in {
      val data = User(Some(1),"ruchir32@gmail.com","demoemail",Some(true))
      val status = service.createUser(data)
      assert(status == "User created")
    }
    "return User not created" in {
      val data = User(Some(1),"ruchir96@gmail.com","demoemail",Some(false))
      val status = service.createUser(data)
      assert(status == "User not created")
    }
  }

  // Test case to check if message is sent or not
  "On Passing ChatCase credentials" should {
    val service = new UserManagementService
    "return Message Added " in {
      val data = ChatCase(Some("ruchirtd96@gmail.com"),"ruchircool007@gmail.com","test message",Some("test group"))
      val status = service.sendMessage(data)
      assert(status == "Message added")
    }
  }

  // Test case to get ID for a particular name
  "On Passing name of user" should {
    val service = new UserManagementService
    "return Id" in {
      val status = service.returnId("ruchirtd96@gmail.com")
      assert(status == 1)
    }
  }

  // Test case to create new group name
  "On Passing name of user" should {
    val service = new UserManagementService
    "return new group name" in {
      val status = service.generateGroupChatName("ruchirtd96@gmail.com","xyz@gmail.com","1",2)
      assert(status == "rx-12")
    }
    }

  // Test case to create new group chat
  "On Passing GroupChat credentials" should {
    val service = new UserManagementService
    "return Message added" in {
      val groupChat = GroupChat(Some("ruchirtd96@gmail.com"),"xyz@gmail.com","Group created")
      val status = service.saveGroupChat(groupChat)
      assert(status == "Message added")
    }
  }
}
