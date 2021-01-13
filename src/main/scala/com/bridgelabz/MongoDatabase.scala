package com.bridgelabz.Main
import akka.actor.ActorSystem
import com.bridgelabz.Chat
import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.ExecutionContext

object MongoDatabase {
  val chatCodecProvider = Macros.createCodecProvider[Chat]()

  val codecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(chatCodecProvider),
    DEFAULT_CODEC_REGISTRY
  )
  implicit val system = ActorSystem("Scala_jwt-App")
  implicit val executor: ExecutionContext = system.dispatcher
  val mongoClient: MongoClient = MongoClient()
  val databaseName = sys.env("database_name")
  // Getting mongodb database
  val database: MongoDatabase = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry)
  val registrationCollection = sys.env("register_collection_name")
  val chatCollection = sys.env("chat_collection")
  // Getting mongodb collection
  val collectionForUserRegistration: MongoCollection[Document] = database.getCollection(registrationCollection)
  collectionForUserRegistration.drop()
  val collectionForChat: MongoCollection[Document] = database.getCollection(chatCollection)
  collectionForChat.drop()
}
