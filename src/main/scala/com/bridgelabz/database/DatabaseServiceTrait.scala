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

import com.bridgelabz.caseclasses.{ChatCase, GroupChat, User}
import org.mongodb.scala.Document

import scala.concurrent.Future

trait DatabaseServiceTrait {

  /**
   *
   * @param credentials : User details
   * @return : String
   */
  def saveUser(credentials:User) : String

  /**
   *
   * @param name : User name
   * @return : Boolean
   */
  def checkIfExists(name : String): Boolean

  /**
   *
   * @param sendMessageRequest : save message to database
   * @return : String
   */
  def saveChatMessage(sendMessageRequest: ChatCase) : String

  // get All users
  def getUsers() : Future[Seq[Document]]

  /**
   *
   * @param name : User's name
   * @return : Future[Seq[Document]]
   */
  def getUsersUsingFilter(name : String) : Future[Seq[Document]]

  /**
   *
   * @param groupChatInfo : Save to group chat
   * @return : String
   */
  def saveToGroupChat(groupChatInfo: GroupChat) : String
}
