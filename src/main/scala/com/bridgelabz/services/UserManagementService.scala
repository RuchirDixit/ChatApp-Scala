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
package com.bridgelabz.services

import com.bridgelabz.actors.ActorSystemFactory
import com.bridgelabz.caseclasses.{ChatCase, GroupChat, User}
import com.bridgelabz.database.DatabaseService
import com.typesafe.scalalogging.LazyLogging
import org.bson.BsonType
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class UserManagementService extends LazyLogging {

  implicit val system = ActorSystemFactory.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  /**
   *
   * @param loginRequest : Request object containing details of login
   * Fetches data from database and checks if credentials passed matches for login
   * @return : If successful login return "Login Successful"
   */
  def userLogin(loginRequest: User): Future[String] = {
    //val users = Await.result(DatabaseService.getUsersUsingFilter(loginRequest.name),60.seconds)
    DatabaseService.getUsersUsingFilter(loginRequest.name).map(users => {
      users.foreach(document => document.foreach(bsonObject =>
        if(bsonObject._2.getBsonType() == BsonType.STRING) {
          if(bsonObject._2.asString().getValue.equals(loginRequest.name)){
            logger.info("Inside if value (name):" + bsonObject._2.asString().getValue)
            users.foreach(document => document.foreach(bsonStringObject =>
              if(bsonStringObject._2.getBsonType() == BsonType.STRING){
                if(bsonStringObject._2.asString().getValue.equals(loginRequest.password)){
                  logger.info("Inside if value (password):" + bsonStringObject._2.asString().getValue)
                  users.foreach(document => document.foreach(bsonObject =>
                    if(bsonObject._2.getBsonType() == BsonType.BOOLEAN){
                      if(!bsonObject._2.asBoolean().getValue){
                        return Future("User Not verified")
                      }
                    }
                  ))
                  return Future("Login Successful")
                }
              }
            ))
          }
        }
      ))
      "User not found"
    })
  }

  /**
   *
   * @param createUserRequest : Request object containing details of registering user
   *  It is used to save users
   * @return : If successful login return "User created"
   */
  def createUser(createUserRequest: User):  Future[String] = {
    val status = DatabaseService.saveUser(createUserRequest)
     status
    }

  /**
   *
   * @param sendMessageRequest: Contains request of type Chat which has sender, receiver and message to be send
   * It is used to save messages to chat
   * @return : Returns String message as "Message added if message successfully sent or else "Sending Msg failed" if failed to deliver message"
   */
  def sendMessage(sendMessageRequest: ChatCase) : String = {
    val status = DatabaseService.saveChatMessage(sendMessageRequest)
    if(status.equals("Message sent")){
      "Message added"
    }
    else {
      "Sending Msg failed"
    }
  }

  /**
   *
   * @param name : Accepts name of user whose ID we have to find
   *  It searches for all users and returns ID of mentioned user
   * @return : Id of user with specified names
   */
  def returnId(name: String) : Int = {
    val users = Await.result(DatabaseService.getUsersUsingFilter(name),60.seconds)
    users.foreach(document => document.foreach(bsonObject =>
      if(bsonObject._2.getBsonType == BsonType.INT32){
        return bsonObject._2.asInt32().getValue
      }
    ))
    -1
  }

  /**
   *
   * @param senderName : Name of sender of message
   * @param receiverName : Name of receiver of message
   * @param senderId : ID of sender of message
   * @param receiverId : ID of receiver of message
   *  It generates group chat name by concatenating id and names in sorted order
   * @return : Group chat name as s String
   */
  def generateGroupChatName(senderName:String,receiverName:String,senderId: String, receiverId: Int): String = {
    val nameString = senderName.charAt(0) + "" + receiverName.charAt(0)
    val idString = senderId + "" + receiverId
    logger.info("Final name:" + nameString.sorted + "-" + idString.sorted)
    nameString.sorted + "-" + idString.sorted
  }

  /**
   * Save messages to group chat and return if message added or failed
   * @param groupChatInfo : Information about sender, receiver and message in group chat
   * @return : String message if message is added to group or not
   */
  def saveGroupChat(groupChatInfo:GroupChat) : String = {
    val status = DatabaseService.saveToGroupChat(groupChatInfo)
    if(status.equals("Message sent to group")){
      "Message added"
    }
    else {
      "Failed to send message"
    }
  }
}
