package com.bridgelabz

import play.api.libs.json.{Json, Reads}

case class User(
                 id : Int,
                 name: String,
                 password: String,
                 isVerified: Option[Boolean] = None
               )
object User {
  implicit val requestReads: Reads[User] = Json.reads[User]
}