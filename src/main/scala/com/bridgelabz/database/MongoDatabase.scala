
package com.bridgelabz.database

import akka.actor.ActorSystem
import com.bridgelabz.actors.ActorSystemFactory
import com.bridgelabz.caseClasses.{ChatCase, GroupChat}
import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.ExecutionContext

object MongoDatabase {
  val chatCodecProvider = Macros.createCodecProvider[ChatCase]()
  val groupCodecProvider = Macros.createCodecProvider[GroupChat]()
  val codecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(chatCodecProvider,groupCodecProvider),
    DEFAULT_CODEC_REGISTRY
  )
  implicit val system = ActorSystemFactory.system
  implicit val executor: ExecutionContext = system.dispatcher
  val mongoClient: MongoClient = MongoClient()
  val databaseName = "ChatApp"
  // Getting mongodb database
  val database: MongoDatabase = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry)
  val registrationCollection = sys.env("register_collection_name")
  val chatCollection = sys.env("chat_collection")
  val groupCollection = sys.env("group-collection")
  // Getting mongodb collection
  val collectionForUserRegistration: MongoCollection[Document] = database.getCollection(registrationCollection)
  collectionForUserRegistration.drop()
  val collectionForChat: MongoCollection[ChatCase] = database.getCollection[ChatCase](chatCollection)
  collectionForChat.drop()
  val collectionForGroup: MongoCollection[GroupChat] = database.getCollection[GroupChat](groupCollection)
  collectionForGroup.drop()
}
