
package com.bridgelabz.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import com.bridgelabz.caseclasses.ChatCase
import com.bridgelabz.database.SaveToDatabaseActor
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import concurrent.duration._
import scala.concurrent.ExecutionContextExecutor
class ChatAppActorTest extends TestKit(ActorSystem("ChatApp-test")) with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll{
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
}
