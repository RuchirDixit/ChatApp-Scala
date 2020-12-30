package com.bridgelabz

import play.api.libs.json.{Json, Reads}

case class User(name:String,password:String,message:String)
object User {
  implicit val requestReads: Reads[User] = Json.reads[User]
}