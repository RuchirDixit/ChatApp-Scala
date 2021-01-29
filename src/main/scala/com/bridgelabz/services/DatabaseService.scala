
package com.bridgelabz.services

import java.util.regex.Pattern

import akka.http.scaladsl.server.Directives.complete
import com.bridgelabz.caseClasses.{ChatCase, GroupChat, JsonResponse, User}
import com.bridgelabz.database.MongoDatabase
import com.typesafe.scalalogging.LazyLogging
import org.bson.BsonType
import org.mongodb.scala.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import com.bridgelabz.traits.DatabaseService

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, TimeoutException}

object DatabaseService extends LazyLogging with DatabaseService {
  /**
   *
   * @param credentials : Data about the user that is getting stored in database
   * @return : If data is entered successfully then returns SUCCESS or else return FAILURE
   */
  def saveUser(credentials:User) : String = {
    val emailRegexPattern = "^[a-zA-Z]+([+.#&_-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+.[a-zA-Z]{2,3}([.][a-zA-Z]{2,3})*$"
    if(Pattern.compile(emailRegexPattern).matcher(credentials.name).matches()){
      logger.info("Email valid!")
      var length = 0
      try {
        val users = Await.result(getUsers(),120.seconds)
        length = users.length
      }
      catch {
        case _: TimeoutException =>
          logger.info("Timeout!")
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
          Await.result(future,10.seconds)
          "Success"
        }
        catch {
          case _:TimeoutException =>
            logger.error("Timeout while added data to users")
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
   *
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
    }
    catch {
      case _: TimeoutException =>
        logger.error("Timeout exception for getting users")
        false
    }
  }

  /**
   *
   * @param sendMessageRequest: The data about sender, receiver and the message that is to be send; added to chat-log collection
   * @return : String "Message sent" to inform that message is saved to database
   */
  def saveChatMessage(sendMessageRequest: ChatCase) : String = {
    val chatMessage = ChatCase(sendMessageRequest.sender,sendMessageRequest.receiver,sendMessageRequest.message,sendMessageRequest.groupChatName)
    val chatAddedFuture = MongoDatabase.collectionForChat.insertOne(chatMessage).toFuture()
    Await.result(chatAddedFuture,60.seconds)
    "Message sent"
  }

  // Returns All the users present inside database in the form of future
  def getUsers() : Future[Seq[Document]] = {
    MongoDatabase.collectionForUserRegistration.find().toFuture()
  }

  /**
   *
   * @param name : Find user based on name specified
   * @return : Future of users that are returned using find method from database
   */
  def getUsersUsingFilter(name : String) : Future[Seq[Document]] = {
    MongoDatabase.collectionForUserRegistration.find(equal("name",name)).projection(excludeId()).toFuture()
  }

  /**
   *
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
      case _:TimeoutException =>
        logger.error("Timeout while adding data to group")
        "Timeout"
    }
  }
}
