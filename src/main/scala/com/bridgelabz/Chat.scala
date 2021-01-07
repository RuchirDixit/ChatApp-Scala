package com.bridgelabz

import play.api.libs.json.{Json, Reads}

case class Chat(
                 sender:String,
                 receiver:String,
                 message:String
               )

object Chat {
  implicit val requestReads: Reads[Chat] = Json.reads[Chat]
}
