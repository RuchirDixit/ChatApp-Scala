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
package com.bridgelabz.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import com.bridgelabz.caseclasses.ChatCase
import com.bridgelabz.database.SaveToDatabaseActor
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import concurrent.duration._
import scala.concurrent.ExecutionContextExecutor
class ChatAppActorTest extends TestKit(ActorSystem("ChatApp-test")) with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll with LazyLogging{
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  "An Email notification actor" must {
    "expect no message" in {
      val emailActor = system.actorOf(Props(new EmailNotificationActor()))
      val chatChatData = ChatCase(Some("ruchir99@gmail.com"),"xyz@gmail.com","test actor",Some("test actor group"))
      emailActor ! chatChatData
      expectNoMessage(3.seconds)
    }
  }
  "Save to database actor" must {
    "expect response" in {
      val saveToDatabaseActor = system.actorOf(Props[SaveToDatabaseActor], "saveActor")
      val chatChatData = ChatCase(Some("ruchir99@gmail.com"),"xyz@gmail.com","test actor",Some("test actor group"))
      saveToDatabaseActor ! chatChatData
      expectMsg("Message added")
    }
  }

  "An Email notification actor" must {
    "expect no message for email" in {
      val emailActor = system.actorOf(Props(new EmailNotificationActor))
      emailActor ! ""
      expectNoMessage()
    }
  }
}
