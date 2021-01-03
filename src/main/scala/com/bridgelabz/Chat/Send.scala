package com.bridgelabz.Chat

import akka.actor.{Actor, ActorRef}

class Send(pong: ActorRef) extends Actor{
  var count = 0
  def incrementAndPrint { count += 1; println("Hiiii") }
  def receive = {
    case StartMessage =>
      incrementAndPrint
      pong ! SendMessage
    case ReceiveMessage =>
      incrementAndPrint
      if (count > 30) {
        sender ! StopMessage
        println("Hiiii stopped")
        context.stop(self)
      } else {
        sender ! SendMessage
      }
  }
}
