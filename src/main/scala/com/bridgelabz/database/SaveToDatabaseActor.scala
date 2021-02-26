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
package com.bridgelabz.database

import akka.actor.Actor
import com.bridgelabz.caseclasses.ChatCase
import com.bridgelabz.services.UserManagementService
import com.typesafe.scalalogging.LazyLogging

class SaveToDatabaseActor extends Actor with LazyLogging{
  override def receive: Receive = {
    case chat:ChatCase =>
      val databaseService = new DatabaseService(new MongoConfig)
      val userManagementService = new UserManagementService(databaseService)
      val response = userManagementService.sendMessage(chat)
      logger.info(response)
      sender() ! response
  }
}
