package com.bridgelabz.Main
import akka.actor.ActorSystem
import org.mongodb.scala.{Document,MongoClient, MongoCollection, MongoDatabase}
import scala.concurrent.ExecutionContext

object MongoDatabase {
  implicit val system = ActorSystem("Scala_jwt-App")
  implicit val executor: ExecutionContext = system.dispatcher
  val mongoClient: MongoClient = MongoClient()
  val databaseName = sys.env("database_name")
  // Getting mongodb database
  val database: MongoDatabase = mongoClient.getDatabase(databaseName)
  val registrationCollection = sys.env("register_collection_name")
  val chatCollection = sys.env("register_collection_name")
  // Getting mongodb collection
  val collectionForUserRegistration: MongoCollection[Document] = database.getCollection(registrationCollection)
  collectionForUserRegistration.drop()
  val collectionForChat: MongoCollection[Document] = database.getCollection(chatCollection)
  collectionForChat.drop()
}
