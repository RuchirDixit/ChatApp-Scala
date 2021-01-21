
package com.bridgelabz

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait MyJsonResponse extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat1(JsonResponse)
}
