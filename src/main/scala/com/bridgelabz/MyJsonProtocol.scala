
package com.bridgelabz

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

// To convert object into json format
trait MyJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val templateFormat = jsonFormat4(ChatCase)
}
