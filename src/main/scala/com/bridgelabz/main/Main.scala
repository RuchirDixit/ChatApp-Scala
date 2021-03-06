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
package com.bridgelabz.main

import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.bridgelabz.actors.ActorSystemFactory
import com.bridgelabz.routes.UserManagementRoutes
import com.bridgelabz.services.UserManagementService
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext
import scala.io.StdIn
// $COVERAGE-OFF$
object Main extends App with LazyLogging {
  private val _ = ConfigFactory.load()
  private val host = sys.env("HOST")
  private val port_number = sys.env("PORT").toInt
  implicit val system = ActorSystemFactory.system
  implicit val mat = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher
  val userManagementService = new UserManagementService
  val userManagementRoutes = new UserManagementRoutes(userManagementService)
  private val routes = userManagementRoutes.routes
  val bindingFuture = Http().bindAndHandle(routes,host,port_number)
  logger.info(s"Server online at http://" + host + ":" + port_number)
  StdIn.readLine()
  bindingFuture
    .onComplete(_ => system.terminate())
}
