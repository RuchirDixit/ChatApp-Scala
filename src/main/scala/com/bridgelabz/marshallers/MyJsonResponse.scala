
package com.bridgelabz.marshallers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.bridgelabz.caseClasses.JsonResponse
import spray.json.DefaultJsonProtocol

trait MyJsonResponse extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat1(JsonResponse)
}
