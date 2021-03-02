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
import com.bridgelabz.database.interfaces.IDatabaseConfig
import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.{Completed, Document, MongoClient, MongoCollection, MongoDatabase, result}
import scala.concurrent.{ExecutionContext, Future}

class MongoConfig extends IDatabaseConfig {

  var collectionForGroup: MongoCollection[GroupChat]
  var collectionForUserRegistration: MongoCollection[Document]
  var collectionForChat: MongoCollection[ChatCase]

  def setUp(): Unit = {
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
    // Getting mongodb collections
    collectionForUserRegistration = database.getCollection(registrationCollection)
    collectionForUserRegistration.drop()
    collectionForChat = database.getCollection[ChatCase](chatCollection)
    collectionForChat.drop()
    collectionForGroup = database.getCollection[GroupChat](groupCollection)
    collectionForGroup.drop()
  }
  /**
   * To add user to Chat App System
   * @param id : Id of user
   * @param name : name of user
   * @param password : user password
   * @param boolean : isVerified status of user
   * @return : Future[Completed]
   */
  def addUser(id: Some[Int], name: String, password: String, boolean: Boolean) : Future[Completed] = {
    val userToBeAdded : Document = Document("id" -> id,"name" -> name, "password" -> password, "isVerified" -> boolean)
    collectionForUserRegistration.insertOne(userToBeAdded).toFuture()
  }

  /**
   * To save messages to Chat
   * @param sendMessageRequest: ChatCase object to store sender, receiver,message, chatName
   * @return : Future[Completed]
   */
  def saveChatMessage(sendMessageRequest: ChatCase) : Future[Completed] = {
    val chatMessage = ChatCase(sendMessageRequest.sender,sendMessageRequest.receiver,sendMessageRequest.message,sendMessageRequest.groupChatName)
    collectionForChat.insertOne(chatMessage).toFuture()
  }

  /**
   * To save messages to group chat
   * @param groupChatInfo : GroupChat object to store sender, receiver,message
   * @return
   */
  def saveGroupChat(groupChatInfo: GroupChat) : Future[Completed] = {
    val groupChat = GroupChat(groupChatInfo.sender,groupChatInfo.receiver,groupChatInfo.message)
    collectionForGroup.insertOne(groupChat).toFuture()
  }

  // To find all users
  def find(): Future[Seq[Document]] = {
    collectionForUserRegistration.find().toFuture()
  }

  /**
   * To find user based on name
   * @param name : name of user to search
   * @return : Future[Seq[Document]]
   */
  def find(name: String): Future[Seq[Document]] = {
    collectionForUserRegistration.find(equal("name",name)).projection(excludeId()).toFuture()
  }

  /**
   *
   * @param name : Name of user to search
   * @return : Future[Seq[ChatCase]]
   */
  def findChat(name: String) : Future[Seq[ChatCase]] = {
    collectionForChat.find(equal("groupChatName", name)).toFuture()
  }

  /**
   *
   * @param name : Name of user to search
   * @return : Future[Seq[GroupChat]]
   */
  def findGroupUsingReceiver(name: String) : Future[Seq[GroupChat]] = {
    collectionForGroup.find(equal("receiver", name)).toFuture()
  }

  /**
   *
   * @param name : Name of user to search
   * @return : Future[Seq[GroupChat]]
   */
  def findGroupUsingSender(name: String) : Future[Seq[GroupChat]] = {
    collectionForGroup.find(equal("sender", name)).toFuture()
  }

  /**
   *
   * @param name : Name of user to update
   * @return : Future[result.UpdateResult]
   */
  def updateUser(name: String) : Future[result.UpdateResult] = {
    collectionForUserRegistration.updateOne(equal("name", name), set("isVerified", true)).toFuture()
  }
}
