package com.bridgelabz.Main

import com.bridgelabz.{Chat, User}
import com.typesafe.scalalogging.LazyLogging
import org.bson.BsonType

import scala.concurrent.Await
import scala.concurrent.duration._
class UserManagementService extends LazyLogging{


  /**
   *
   * @param loginRequest : Request object containing details of login
   * @return : If successful login return "Login Successful"
   */
  def userLogin(loginRequest: User): String = {
    val users = Await.result(Database_service.getUsersUsingFilter(loginRequest.name),60.seconds)
    users.foreach(document => document.foreach(bsonObject =>
      if(bsonObject._2.asString().getValue.equals(loginRequest.name)){
        logger.info("Inside if value (name):" + bsonObject._2.asString().getValue)
        users.foreach(document => document.foreach(bsonStringObject =>
          if(bsonStringObject._2.asString().getValue.equals(loginRequest.password)){
            logger.info("Inside if value (password):" + bsonStringObject._2.asString().getValue)
            users.foreach(document => document.foreach(bsonObject =>
              if(bsonObject._2.getBsonType() == BsonType.BOOLEAN){
                if(bsonObject._2.asBoolean().getValue == false){
                  return "User Not verified"
                }
              }
            ))
            return "Login Successful"
          }
        ))
      }
    ))
    "User not found"
  }

  /**
   *
   * @param createUserRequest : Request object containing details of registering user
   * @return : If successfull login return "User created"
   */
  def createUser(createUserRequest: User):  String = {
    val status = Database_service.saveUser(createUserRequest)
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
  def sendMessage(sendMessageRequest: Chat) = {
    val status = Database_service.saveChatMessage(sendMessageRequest)
    if(status.equals("Message sent")){
      "Message added"
    }
    else {
      "Sending Msg failed"
    }
  }
  def protectedContent: String  = "This method is secured"

}