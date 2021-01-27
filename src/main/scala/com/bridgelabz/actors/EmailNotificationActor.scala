
package com.bridgelabz.actors

import akka.actor.Actor
import com.bridgelabz.services.UserManagementService

class EmailNotificationActor extends Actor {
  def receive: Receive = {
    case receiverAddress:String =>
      val service = new UserManagementService
      service.sendEmailReminder(receiverAddress)
  }
}
