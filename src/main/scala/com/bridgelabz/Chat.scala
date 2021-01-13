package com.bridgelabz

import play.api.libs.json.{Json, Reads, Writes}

class Chat(var sender:String,var receiver:String,var message:String, var groupChatName:String){
  def setSenderName(senderName:String) = {
    sender = senderName
  }
  def setReceiverName(receiverName:String) = {
    receiver = receiverName
  }
  def setMessage(getMessage:String) = {
    message = getMessage
  }
  def setGroupChatName(chatName:String) = {
    groupChatName = chatName
  }
}

object Chat {
  def apply(sender: String, receiver: String, message: String, groupname: String): Chat
  = new Chat(sender, receiver, message,groupname)

  def unapply(arg: Chat): Option[(String, String, String,String)] = ???
  implicit val requestReads: Reads[Chat] = Json.reads[Chat]
  implicit val requestWrites: Writes[Chat] = Json.writes[Chat]
}
