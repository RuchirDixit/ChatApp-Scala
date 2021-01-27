
package com.bridgelabz.actors

import akka.actor.Actor
import com.bridgelabz.caseClasses.ChatCase
import com.bridgelabz.services.UserManagementService
import com.typesafe.scalalogging.LazyLogging

class SaveToDatabaseActor extends Actor with LazyLogging{
  override def receive: Receive = {
    case chat:ChatCase =>
      val userManagementService = new UserManagementService
      val response = userManagementService.sendMessage(chat)
      logger.info(response)
      sender() ! response
  }
}
