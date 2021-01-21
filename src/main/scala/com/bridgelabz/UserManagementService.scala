// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.bridgelabz.Main

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import com.bridgelabz.{ChatCase, EmailNotificationActor, User}
import com.typesafe.scalalogging.LazyLogging
import courier.{Envelope, Mailer, Text}
import javax.mail.internet.InternetAddress
import org.bson.BsonType

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class UserManagementService extends LazyLogging {
  val system = ActorSystem("Chat-App-Service")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  /**
   *
   * @param loginRequest : Request object containing details of login
   * @return : If successful login return "Login Successful"
   */
  def userLogin(loginRequest: User): String = {
    val users = Await.result(Database_service.getUsersUsingFilter(loginRequest.name),60.seconds)
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
                      return "User Not verified"
                    }
                  }
                ))
                return "Login Successful"
              }
            }
          ))
        }
      }
    ))
    "User not found"
  }

  /**
   *
   * @param createUserRequest : Request object containing details of registering user
   * @return : If successful login return "User created"
   */
  def createUser(createUserRequest: User):  String = {
    val status = Database_service.saveUser(createUserRequest)
    println("service" + Database_service.saveUser(createUserRequest))
    if (status.equals("Success")){
      "User created"
    }
    else if(status.equals("Validation Failed")){
      "User Validation Failed"
    }
    else {
      "User not created"
    }
  }

  /**
   *
   * @param sendMessageRequest: Contains request of type Chat which has sender, receiver and message to be send
   * @return : Returns String message as "Message added if message successfully sent or else "Sending Msg failed" if failed to deliver message"
   */
  def sendMessage(sendMessageRequest: ChatCase) : String = {
    val status = Database_service.saveChatMessage(sendMessageRequest)
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
   * @return : Id of user with speicified names
   */
  def returnId(name: String) : Int = {
    val users = Await.result(Database_service.getUsersUsingFilter(name),60.seconds)
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
   *
   * @param receiverAddress : To whom we have to send email
   *  Uses courier library to send email to registered user
   *  @return : Success if email send successfully or NOT Found if not able to send mail
   */
  def sendEmailReminder(receiverAddress:String) : Unit = {
    val mailer = Mailer(sys.env("mailer"), sys.env("smtp_port").toInt)
      .auth(true)
      .as(sys.env("sender_email"),sys.env("sender_password"))
      .startTls(true)()
    mailer(Envelope.from(new InternetAddress(sys.env("sender_email")))
      .to(new InternetAddress(receiverAddress))
      .subject("Full version Reminder")
      .content(Text("Purchase full version for more exciting features for chatting. Pay $20 for full version. Buy Now!")))
      .onComplete {
        case Success(_) => logger.info("Email notification Sent!")
        case Failure(_) => complete(StatusCodes.NotFound,"Failed To Deliver Notification!")
      }
  }

}