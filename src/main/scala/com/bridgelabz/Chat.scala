package com.bridgelabz

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

class Chat(var sender:String,var receiver:String,var message:String){
  def setSenderName(senderName:String) = {
    sender = senderName
  }
  def setReceiverName(receiverName:String) = {
    receiver = receiverName
  }
  def setMessage(getMessage:String) = {
    message = getMessage
  }
}

object Chat {
//  implicit val citySuggestionWrites: Writes[Chat] = (
//    (JsPath \ "name").write[String] and
//      (JsPath \ "locationId").write[String] and
//      (JsPath \ "locationKind").write[String]
//    )(unlift(Chat.
  def apply(sender: String, receiver: String, message: String): Chat
  = new Chat(sender, receiver, message)

  def unapply(arg: Chat): Option[(String, String, String)] = ???
  implicit val requestReads: Reads[Chat] = Json.reads[Chat]
}
