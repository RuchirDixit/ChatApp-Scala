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
import com.bridgelabz.database.interfaces.{IChatService, IDatabaseService, IGetUserService, IGroupService, IDatabaseConfig, ISaveUserService}
import com.typesafe.scalalogging.LazyLogging
import org.bson.BsonType
import org.mongodb.scala.Document
import scala.concurrent.{ExecutionContext, Future, TimeoutException}

class DatabaseService(databaseConfig: IDatabaseConfig) extends LazyLogging with IGetUserService with IChatService with IGroupService with ISaveUserService
  with IDatabaseService {
  implicit val system = ActorSystemFactory.system
  implicit val executor: ExecutionContext = system.dispatcher
  /**
   * Checks whether user already exists and if email is valid then add user to database
   * @param credentials : Data about the user that is getting stored in database
   * @return : If data is entered successfully then returns SUCCESS or else return FAILURE
   */
  def saveUser(credentials:User) : Future[String] = {
    val emailRegexPattern = "^[a-zA-Z]+([+.#&_-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+.[a-zA-Z]{2,3}([.][a-zA-Z]{2,3})*$"
    if(Pattern.compile(emailRegexPattern).matcher(credentials.name).matches()) {
      logger.info("Email valid!")
      var length = 0
      try {
        getUsers().map(users =>
          length = users.size
        )
      }
      catch {
        case timeoutException: TimeoutException =>
          logger.info(timeoutException.toString)
        case exception: Exception =>
          logger.info(exception.toString)
      }

      val ifUserExists = checkIfExists(credentials.name)
      ifUserExists.map(ifUserExists => {
        if (ifUserExists) {
          logger.error("User exists!")
          "Failure"
        }
        else
        {
          val future = databaseConfig.addUser(Some(length + 1),credentials.name,credentials.password,false)
          try{
            future.map(future => future)
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
      })
    }
    else {
      logger.error("Invalid email")
      Future("Validation Failed")
    }
  }

  /**
   * Checks if user already exists if yes returns true else return false
   * @param name : Name of user to check whether this user already exists
   * @return : If user already exists it returns true or else it returns false
   */
  def checkIfExists(name : String): Future[Boolean] = {
      getUsers().map(users => {
        try {
          var userExists : Boolean = false
          users.foreach(document => document.foreach(bsonObject =>
            if(bsonObject._2.getBsonType() == BsonType.STRING){
              if(bsonObject._2.asString().getValue().equals(name)) {
                userExists = true
              }
            }
          ))
          userExists
        }
        catch {
          case exception: Exception => logger.error(exception.toString)
            false
        }

      })
    }

  /**
   * Saves chat messages to Chat collections
   * @param sendMessageRequest: The data about sender, receiver and the message that is to be send; added to chat-log collection
   * @return : String "Message sent" to inform that message is saved to database
   */
  def saveChatMessage(sendMessageRequest: ChatCase) : String = {
    try {
      val chatAddedFuture = databaseConfig.saveChatMessage(sendMessageRequest)
      chatAddedFuture.map(chatAddedFuture => chatAddedFuture)
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
    databaseConfig.find()
  }


  /**
   * Get user object based on name passed as parameter
   * @param name : Find user based on name specified
   * @return : Future of users that are returned using find method from database
   */
  def getUsersUsingFilter(name : String) : Future[Seq[Document]] = {
    databaseConfig.find(name)
  }

  /**
   * To save messages to group chat
   * @param groupChatInfo : Information about sender, receiver and message from group chat
   * @return : String message that message has been sent inside group
   */
  def saveToGroupChat(groupChatInfo: GroupChat) : String = {
    try{
      val chatAddedFuture = databaseConfig.saveGroupChat(groupChatInfo)
      chatAddedFuture.map(chatAddedFuture => chatAddedFuture)
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
