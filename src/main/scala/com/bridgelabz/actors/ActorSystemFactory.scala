
package com.bridgelabz.actors

import akka.actor.ActorSystem

object ActorSystemFactory {
  implicit val system = ActorSystem("QuickStart")
}
