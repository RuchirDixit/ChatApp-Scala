package com.bridgelabz

import akka.actor.Actor
import com.bridgelabz.Main.UserManagementService

class EmailNotificationActor extends Actor {
  def receive: Receive = {
    case receiverAddress:String =>
      val service = new UserManagementService
      service.sendEmailReminder(receiverAddress)
  }
}
