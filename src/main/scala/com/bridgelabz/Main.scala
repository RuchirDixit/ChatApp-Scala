
package com.bridgelabz.Main

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Main extends App with LazyLogging {
  val conf = ConfigFactory.load()
  val host = sys.env("Host")
  val port_number = sys.env("Port_number").toInt
  implicit val system = ActorSystem("QuickStart")
  implicit val mat = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher
  val userManagementService = new UserManagementService
  val userManagementRoutes = new UserManagementRoutes(userManagementService)
  val routes = userManagementRoutes.routes
  val bindingFuture = Http().bindAndHandle(routes,host,port_number)
  logger.info(s"Server online at http://localhost:8081")
  StdIn.readLine()
  bindingFuture
    .onComplete(_ => system.terminate())
}
