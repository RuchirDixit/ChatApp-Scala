
package com.bridgelabz

import play.api.libs.json.{Json, Reads}

case class GroupChat(
                      sender:Option[String],
                      receiver:String,
                      message:String)
