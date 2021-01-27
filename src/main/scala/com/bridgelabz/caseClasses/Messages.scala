
package com.bridgelabz.caseClasses

import play.api.libs.json.{Json, Reads}

case class Messages(sender: String, receiver:String)

object Messages {
  implicit val requestReads: Reads[Messages] = Json.reads[Messages]
}