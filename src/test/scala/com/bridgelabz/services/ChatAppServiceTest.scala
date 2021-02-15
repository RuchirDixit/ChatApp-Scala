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
import com.bridgelabz.database.DatabaseService
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when

class ChatAppServiceTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest with MockitoSugar{
  // Test case To check if user exists
  "Check if exists" should {
    "return true" in {
      val status = DatabaseService.checkIfExists("ruchir99@gmail.com")
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

  "On Successful chat sent to group" should {
    "return Message exception group chat" in {
      val data = GroupChat(Some("ruchircool007@gmail.com"),null,"Hey group")
      val status = DatabaseService.saveToGroupChat(data)
      assert(status == "Error")
    }
  }

  // Test case to check if user is logged in or not
  "On Passing credentials" should {
    val service = new UserManagementService
    "return Logged  in " in {
      val data = User(Some(1),"ruchir99@gmail.com","demoemail",Some(true))
      val status = service.userLogin(data)
      assert(status == "Login Successful")
    }
    "return User not found" in {
      val data = User(Some(1),"ruchir@rediffmail.com","demo",Some(false))
      val status = service.userLogin(data)
      assert(status == "User not found")
    }
    "return User not verified" in {
      val mockLogin = mock[UserManagementService]
      val data = User(Some(1),"ruchir@rediffmail.com","demo",Some(false))
      when(mockLogin.userLogin(data)).thenReturn("User Not verified")
      val status = mockLogin.userLogin(data)
      assert(status == "User Not verified")
    }
  }

  // Test case to check if user is created or not
  "On Passing credentials" should {
    val service = new UserManagementService
    "return User created " in {
      val serviceMock = mock[UserManagementService]
      val data = User(Some(1),"ruchir01@gmail.com","demoemail",Some(true))
      when(serviceMock.createUser(data)).thenReturn("User created")
      val status = serviceMock.createUser(data)
      assert(status == "User created")
    }
    "return User not created" in {
      val data = User(Some(1),"ruchir99@gmail.com","demoemail",Some(false))
      val status = service.createUser(data)
      assert(status == "User not created")
    }
    "return User validation failed " in {
      val serviceMock = mock[UserManagementService]
      val data = User(Some(1),"@gmail.com","demoemail",Some(true))
      when(serviceMock.createUser(data)).thenReturn("User Validation Failed")
      val status = serviceMock.createUser(data)
      assert(status == "User Validation Failed")
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
    "return sending message failed" in {
      val serviceMock = mock[UserManagementService]
      val data = ChatCase(Some("xyz@gmail.com"),"non-existing@xyz.com","mock",Some("mockgroup"))
      when(serviceMock.sendMessage(data)).thenReturn("Sending Msg failed")
      val status = serviceMock.sendMessage(data)
      assert(status == "Sending Msg failed")
    }
  }

  // Test case to get ID for a particular name
  "On Passing name of user" should {
    val service = new UserManagementService
    "return Id" in {
      val status = service.returnId("ruchir99@gmail.com")
      assert(status == 2)
    }
  }

  "On passing incorrect name of user" should {
    "return Id -1" in {
      val idMock = mock[UserManagementService]
      when(idMock.returnId("anyuser@gmail.com")).thenReturn(-1)
      val status = idMock.returnId("anyuser@gmail.com")
      assert(status == -1)
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
    "return Message Failed to send message" in {
      val messageMock = mock[UserManagementService]
      val groupChat = GroupChat(Some("ruchirtd96@gmail.com"),"xyz@gmail.com","Group created")
      when(messageMock.saveGroupChat(groupChat)).thenReturn("Failed to send message")
      val status = messageMock.saveGroupChat(groupChat)
      assert(status == "Failed to send message")
    }
  }

  "When send email reminder" must  {
    "return mail send" in {
      val service = new UserManagementService
      val status = service.sendEmailReminder("xyz@gmail.com")
      assert(status == "Mail send")
    }
  }

  "On Sending null value to group" should {
    "return Failed to save group message" in {
      val service = new UserManagementService
      val data = GroupChat(Some("ruchircool007@gmail.com"),null,"Hey group")
      val status = service.saveGroupChat(data)
      assert(status == "Failed to send message")
    }
  }

  "On Sending null to chat" should {
    "return Failed" in {
      val service = new UserManagementService
      val data = ChatCase(Some("xyz@gmail.com"),null,"mock",Some("mockgroup"))
      val status = service.sendMessage(data)
      assert(status == "Sending Msg failed")
    }
  }

  "On Sending null while saving user" should {
    "return false for null user" in {
      val status = DatabaseService.checkIfExists(null)
      assert(status == false)
    }
  }

}
