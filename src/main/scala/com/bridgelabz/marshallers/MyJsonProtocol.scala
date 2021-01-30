
package com.bridgelabz.marshallers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.bridgelabz.caseclasses.{ChatCase, GroupChat}
import spray.json.DefaultJsonProtocol

// To convert object into json format
trait MyJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val templateFormat = jsonFormat4(ChatCase)
  implicit val templateFormatGroup = jsonFormat3(GroupChat)
}
