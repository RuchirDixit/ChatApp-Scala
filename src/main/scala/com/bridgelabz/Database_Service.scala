package com.bridgelabz.Main

import com.bridgelabz.{Chat, User}
import org.bson.BsonType
import org.mongodb.scala.Document
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Filters._
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.regex.Pattern;
object Database_service {

  /**
   *
   * @param credentials : Data about the user that is getting stored in database
   * @return : If data is entered successfully then returns SUCCESS or else return FAILURE
   */
  def saveUser(credentials:User) = {
    val emailRegexPattern = "^[a-zA-Z]+([+.#&_-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+.[a-zA-Z]{2,3}([.][a-zA-Z]{2,3})*$"
    if(Pattern.compile(credentials.name).matcher(emailRegexPattern).matches() == true){
      val userToBeAdded : Document = Document("id" -> credentials.id, "name" -> credentials.name, "password" -> credentials.password, "isVerified" -> false)
      val ifUserExists = checkIfExists(credentials.name)
      if(ifUserExists == true)
      {
        "Failure"
      }
      else
      {
        val future = MongoDatabase.collectionForUserRegistration.insertOne(userToBeAdded).toFuture()
        Await.result(future,10.seconds)
        "Success"
      }
    }
    else {
      "Validation Failed"
    }
  }

  /**
   *
   * @param name : Name of user to check whether this user already exists
   * @return : If user already exists it returns true or else it returns false
   */
  def checkIfExists(name : String): Boolean = {
    val data = Await.result(getUsers(),10.seconds)
    data.foreach(document => document.foreach(bsonObject =>
      if(bsonObject._2.getBsonType() == BsonType.STRING){
        if(bsonObject._2.asString().getValue().equals(name))
          return true
      }
    ))
    false
  }

  /**
   *
   * @param sendMessageRequest: The data about sender, receiver and the message that is to be send; added to chat-log collection
   * @return : String "Message sent" to inform that message is saved to database
   */
  def saveChatMessage(sendMessageRequest: Chat) : String = {
    val senderToReceiverMessage : Document = Document("sender" -> sendMessageRequest.sender, "receiver" -> sendMessageRequest.receiver, "message" -> sendMessageRequest.message, "groupChatName" -> sendMessageRequest.groupChatName)
    val chatAddedFuture = MongoDatabase.collectionForChat.insertOne(senderToReceiverMessage).toFuture()
    Await.result(chatAddedFuture,60.seconds)
    "Message sent"
  }

  // Returns All the users present inside database in the form of future
  def getUsers() = {
    MongoDatabase.collectionForUserRegistration.find().toFuture()
  }

  /**
   *
   * @param name : Find user based on name specified
   * @return : Future of users that are returned using find method from database
   */
  def getUsersUsingFilter(name : String) = {
    MongoDatabase.collectionForUserRegistration.find(equal("name",name)).projection(excludeId()).toFuture()
  }


}
