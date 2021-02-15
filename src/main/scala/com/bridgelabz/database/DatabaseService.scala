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

import java.util.regex.Pattern

import com.bridgelabz.actors.ActorSystemFactory
import com.bridgelabz.caseclasses.{ChatCase, GroupChat, User}
import com.bridgelabz.utilities.Utilities
import com.typesafe.scalalogging.LazyLogging
import com.typesafe.sslconfig.ssl.ClientAuth.None
import org.bson.BsonType
import org.mongodb.scala.Document
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Projections.excludeId

import concurrent.duration._
import scala.None
import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}

object DatabaseService extends LazyLogging with DatabaseServiceTrait {
  implicit val system = ActorSystemFactory.system
  implicit val executor: ExecutionContext = system.dispatcher
  /**
   * Checks whether user already exists and if email is valid then add user to database
   * @param credentials : Data about the user that is getting stored in database
   * @return : If data is entered successfully then returns SUCCESS or else return FAILURE
   */
  def saveUser(credentials:User) : String = {
    val emailRegexPattern = "^[a-zA-Z]+([+.#&_-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+.[a-zA-Z]{2,3}([.][a-zA-Z]{2,3})*$"
    if(Pattern.compile(emailRegexPattern).matcher(credentials.name).matches()){
      logger.info("Email valid!")
      var length = 0
      try {
        val secondsForAwait = 120
        val users = Utilities.tryAwait(getUsers(),secondsForAwait)
        length = users.size
      }
      catch {
        case timeoutException: TimeoutException =>
          logger.info(timeoutException.toString)
        case exception: Exception =>
          logger.info(exception.toString)
      }
      val userToBeAdded : Document = Document("id" -> Some(length + 1),"name" -> credentials.name, "password" -> credentials.password, "isVerified" -> false)
      val ifUserExists = checkIfExists(credentials.name)
      if(ifUserExists)
      {
        logger.error("User exists!")
        "Failure"
      }
      else
      {
        val future = MongoDatabase.collectionForUserRegistration.insertOne(userToBeAdded).toFuture()
        try{
          val duration = 10
          Utilities.tryAwait(future,duration)
          "Success"
        }
        catch {
          case timeoutException:TimeoutException =>
            logger.error(timeoutException.toString)
            "Failure"
          case exception: Exception =>
            logger.info(exception.toString)
            "Failure"
        }
      }
    }
    else {
      logger.error("Invalid email")
      "Validation Failed"
    }
  }

  /**
   * Checks if user already exists if yes returns true else return false
   * @param name : Name of user to check whether this user already exists
   * @return : If user already exists it returns true or else it returns false
   */
  def checkIfExists(name : String): Boolean = {
    try {
      val users = Await.result(getUsers(),10.seconds)
      users.foreach(document => document.foreach(bsonObject =>
        if(bsonObject._2.getBsonType() == BsonType.STRING){
          if(bsonObject._2.asString().getValue().equals(name)) {
            return true
          }
        }
      ))
      false
//      getUsers().flatMap {
//        case Some(users) =>
//        case None => Future.successful(false)
//      }
    }
    catch {
      case timeoutException: TimeoutException =>
        logger.error(timeoutException.toString)
        false
      case exception: Exception =>
        logger.info(exception.toString)
        false
    }
  }

  /**
   * Saves chat messages to Chat collections
   * @param sendMessageRequest: The data about sender, receiver and the message that is to be send; added to chat-log collection
   * @return : String "Message sent" to inform that message is saved to database
   */
  def saveChatMessage(sendMessageRequest: ChatCase) : String = {
    try
      {
      val chatMessage = ChatCase(sendMessageRequest.sender,sendMessageRequest.receiver,sendMessageRequest.message,sendMessageRequest.groupChatName)
      val chatAddedFuture = MongoDatabase.collectionForChat.insertOne(chatMessage).toFuture()
      Await.result(chatAddedFuture,60.seconds)
      "Message sent"
    }
    catch {
      case timeoutException:TimeoutException =>
        logger.error(timeoutException.toString)
        "Timeout"
      case exception: Exception =>
        logger.info(exception.toString)
        "Error"
    }
  }

  // Returns All the users present inside database in the form of future
  def getUsers() : Future[Seq[Document]] = {
    MongoDatabase.collectionForUserRegistration.find().toFuture()
  }


  /**
   * Get user object based on name passed as parameter
   * @param name : Find user based on name specified
   * @return : Future of users that are returned using find method from database
   */
  def getUsersUsingFilter(name : String) : Future[Seq[Document]] = {
    MongoDatabase.collectionForUserRegistration.find(equal("name",name)).projection(excludeId()).toFuture()
  }

  /**
   * To save messages to group chat
   * @param groupChatInfo : Information about sender, receiver and message from group chat
   * @return : String message that message has been sent inside group
   */
  def saveToGroupChat(groupChatInfo: GroupChat) : String = {
    try{
      val groupChat = GroupChat(groupChatInfo.sender,groupChatInfo.receiver,groupChatInfo.message)
      val chatAddedFuture = MongoDatabase.collectionForGroup.insertOne(groupChat).toFuture()
      Await.result(chatAddedFuture,60.seconds)
      "Message sent to group"
    }
    catch {
      case timeoutException:TimeoutException =>
        logger.error(timeoutException.toString)
        "Timeout"
      case exception: Exception =>
        logger.info(exception.toString)
        "Error"
    }
  }
}
