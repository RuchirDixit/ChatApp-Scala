package com.bridgelabz.Chat

import akka.actor.Actor

class Receive extends Actor{
  def receive = {
    case SendMessage =>
      println("  Helloooo")
      sender ! ReceiveMessage
    case StopMessage =>
      println("Hello stopped")
      context.stop(self)
  }
}
