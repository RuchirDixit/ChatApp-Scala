package com.bridgelabz.Main

import com.bridgelabz.User
import org.bson.BsonType
import org.mongodb.scala.Document
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Filters._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
object Database_service {

  /**
   *
   * @param credentials : Data about the user that is getting stored in database
   * @return : If data is entered successfully then returns SUCCESS or else return FAILURE
   */
  def saveUser(credentials:User) = {
    val userToBeAdded : Document = Document("name" -> credentials.name, "password" -> credentials.password, "message" -> credentials.message)
    val ifUserExists = checkIfExists(credentials.name)
    if(ifUserExists == true)
    {
      "Failure"
    }
    else
    {
      val future = MongoDatabase.collection.insertOne(userToBeAdded).toFuture()
      Await.result(future,10.seconds)
      "Success"
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
  // Returns All the users present inside database in the form of future
  def getUsers() = {
    MongoDatabase.collection.find().toFuture()
  }

  // Returns user that matches with given email ID and excluded ObjectId while returning
  def getUsersUsingFilter(name : String) = {
    MongoDatabase.collection.find(equal("name",name)).projection(excludeId()).toFuture()
  }

}
