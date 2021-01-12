package com.bridgelabz
import play.api.libs.json._

object MyJsonProtocol{
  implicit object ColorJsonFormat extends Format[Chat] {
    def writes(c: Chat) : JsValue = {
      val chatSeq = Seq (
        "sender" -> JsString(c.sender),
        "receiver" -> JsString(c.receiver),
        "message" -> JsString(c.message),
        "groupChatName" -> JsString(c.groupChatName),
      )
      JsObject(chatSeq)
    }


    def reads(value: JsValue) = {
      JsSuccess(Chat("","","",""))
    }
  }
}
