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

import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Projections.excludeId
import com.bridgelabz.actors.ActorSystemFactory
import com.bridgelabz.caseclasses.{ChatCase, GroupChat}
import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import org.mongodb.scala.{Completed, Document, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.{ExecutionContext, Future}

object DatabaseConfig {

  val chatCodecProvider = Macros.createCodecProvider[ChatCase]()
  val groupCodecProvider = Macros.createCodecProvider[GroupChat]()
  val codecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(chatCodecProvider,groupCodecProvider),
    DEFAULT_CODEC_REGISTRY
  )
  implicit val system = ActorSystemFactory.system
  implicit val executor: ExecutionContext = system.dispatcher
  val host = sys.env("HOST")
  val port = sys.env("MONGOPORT")
  val url = s"mongodb://${host}:${port}"
  val mongoClient: MongoClient = MongoClient(url)
  val databaseName = sys.env("DATABASENAME")
  // Getting mongodb database
  val database: MongoDatabase = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry)
  val registrationCollection = "users"
  val chatCollection = "chatlogs"
  val groupCollection = "groupchats"
  // Getting mongodb collection
  val collectionForUserRegistration: MongoCollection[Document] = database.getCollection(registrationCollection)
  collectionForUserRegistration.drop()
  val collectionForChat: MongoCollection[ChatCase] = database.getCollection[ChatCase](chatCollection)
  collectionForChat.drop()
  val collectionForGroup: MongoCollection[GroupChat] = database.getCollection[GroupChat](groupCollection)
  collectionForGroup.drop()

  def addUser(id: Some[Int], name: String, password: String, boolean: Boolean) : Future[Completed] = {
    val userToBeAdded : Document = Document("id" -> id,"name" -> name, "password" -> password, "isVerified" -> boolean)
    DatabaseConfig.collectionForUserRegistration.insertOne(userToBeAdded).toFuture()
  }

  def saveChatMessage(sendMessageRequest: ChatCase) : Future[Completed] = {
    val chatMessage = ChatCase(sendMessageRequest.sender,sendMessageRequest.receiver,sendMessageRequest.message,sendMessageRequest.groupChatName)
    DatabaseConfig.collectionForChat.insertOne(chatMessage).toFuture()
  }

  def saveGroupChat(groupChatInfo: GroupChat) : Future[Completed] = {
    val groupChat = GroupChat(groupChatInfo.sender,groupChatInfo.receiver,groupChatInfo.message)
    DatabaseConfig.collectionForGroup.insertOne(groupChat).toFuture()
  }

  def find(): Future[Seq[Document]] = {
    DatabaseConfig.collectionForUserRegistration.find().toFuture()
  }

  def find(name: String): Future[Seq[Document]] = {
    DatabaseConfig.collectionForUserRegistration.find(equal("name",name)).projection(excludeId()).toFuture()
  }
}
