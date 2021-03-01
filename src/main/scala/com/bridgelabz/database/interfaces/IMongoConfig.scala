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
package com.bridgelabz.database.interfaces

import com.bridgelabz.caseclasses.{ChatCase, GroupChat}
import org.mongodb.scala.{Completed, Document, MongoCollection}

import scala.concurrent.Future

trait IMongoConfig {
  val collectionForUserRegistration: MongoCollection[Document]
  val collectionForChat: MongoCollection[ChatCase]
  val collectionForGroup: MongoCollection[GroupChat]
  /**
   * To add user to Chat App System
   * @param id : Id of user
   * @param name : name of user
   * @param password : user password
   * @param boolean : isVerified status of user
   * @return : Future[Completed]
   */
  def addUser(id: Some[Int], name: String, password: String, boolean: Boolean) : Future[Completed]

  /**
   * To save messages to Chat
   * @param sendMessageRequest: ChatCase object to store sender, receiver,message, chatName
   * @return : Future[Completed]
   */
  def saveChatMessage(sendMessageRequest: ChatCase) : Future[Completed]

  /**
   * To save messages to group chat
   * @param groupChatInfo : GroupChat object to store sender, receiver,message
   * @return
   */
  def saveGroupChat(groupChatInfo: GroupChat) : Future[Completed]

  // To find all users
  def find(): Future[Seq[Document]]

  /**
   * To find user based on name
   * @param name : name of user to search
   * @return : Future[Seq[Document]]
   */
  def find(name: String): Future[Seq[Document]]
}
