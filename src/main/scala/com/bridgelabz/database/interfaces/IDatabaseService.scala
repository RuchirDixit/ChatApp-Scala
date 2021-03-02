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

import com.bridgelabz.caseclasses.{ChatCase, GroupChat, User}
import org.mongodb.scala.Document
import scala.concurrent.Future

trait IDatabaseService {

  /**
   * Checks whether user already exists and if email is valid then add user to database
   * @param credentials : Data about the user that is getting stored in database
   * @return : If data is entered successfully then returns SUCCESS or else return FAILURE
   */
  def saveUser(credentials:User) : Future[String]

  /**
   * Checks if user already exists if yes returns true else return false
   * @param name : Name of user to check whether this user already exists
   * @return : If user already exists it returns true or else it returns false
   */
  def checkIfExists(name : String): Future[Boolean]

  /**
   * Saves chat messages to Chat collections
   * @param sendMessageRequest: The data about sender, receiver and the message that is to be send; added to chat-log collection
   * @return : String "Message sent" to inform that message is saved to database
   */
  def saveChatMessage(sendMessageRequest: ChatCase) : String

  // Returns All the users present inside database in the form of future
  def getUsers() : Future[Seq[Document]]

  /**
   * Get user object based on name passed as parameter
   * @param name : Find user based on name specified
   * @return : Future of users that are returned using find method from database
   */
  def getUsersUsingFilter(name : String) : Future[Seq[Document]]

  /**
   * To save messages to group chat
   * @param groupChatInfo : Information about sender, receiver and message from group chat
   * @return : String message that message has been sent inside group
   */
  def saveToGroupChat(groupChatInfo: GroupChat) : String
}