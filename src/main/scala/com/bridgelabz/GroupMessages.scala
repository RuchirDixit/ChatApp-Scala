
package com.bridgelabz

import play.api.libs.json.{Json, Reads}

case class GroupMessages(groupName:String)

object GroupMessages {
  implicit val requestReads: Reads[GroupMessages] = Json.reads[GroupMessages]
}