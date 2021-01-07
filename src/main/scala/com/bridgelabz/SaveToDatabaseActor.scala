package com.bridgelabz

import akka.actor.Actor
import com.bridgelabz.Main.UserManagementService

class SaveToDatabaseActor extends Actor{
  override def receive = {
    case chat:Chat =>
      val userManagementService = new UserManagementService
      val response = userManagementService.sendMessage(chat)
      sender() ! response
  }
}
