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
import com.bridgelabz.{ChatCase, GroupChat}
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
  implicit val system = ActorSystem("Scala_jwt-App")
  implicit val executor: ExecutionContext = system.dispatcher
  val mongoClient: MongoClient = MongoClient()
  val databaseName = sys.env("database_name")
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
