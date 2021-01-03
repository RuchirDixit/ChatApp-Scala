package com.bridgelabz.Chat

import akka.actor.{ActorSystem, Props}

object ChatMain extends App {
  val system = ActorSystem("PingPongSystem")
  val receiver = system.actorOf(Props[Receive], name = "ReceiverActor")
  val sender = system.actorOf(Props(new Send(receiver)), name = "SenderActor")
  // start them going
  sender ! StartMessage
}
