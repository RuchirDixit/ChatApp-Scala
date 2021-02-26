// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.bridgelabz.email

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import com.bridgelabz.caseclasses.EmailCase
import com.bridgelabz.database.{DatabaseService, MongoConfig}
import com.bridgelabz.services.UserManagementService
import com.typesafe.scalalogging.LazyLogging
import courier.{Envelope, Mailer, Text}
import javax.mail.internet.InternetAddress
import org.mongodb.scala.MongoClient

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Email extends LazyLogging {
  val mongoConfig = new MongoConfig
  val databaseService = new DatabaseService(mongoConfig)
  val service = new UserManagementService(databaseService)
  implicit val executionContext = service.executionContext

  def sendEmail(email: EmailCase) : Future[Unit] = {
    val mailer = Mailer(sys.env("GMAILMAILER"), sys.env("SMTPPORT").toInt)
      .auth(true)
      .as(sys.env("SENDEREMAIL"),sys.env("PASSWORD"))
      .startTls(true)()
    mailer(Envelope.from(new InternetAddress(sys.env("SENDEREMAIL")))
      .to(new InternetAddress(email.receiverAddress))
      .subject(email.subject)
      .content(Text(email.body)))
  }
  /**
   *
   * @param email : Object of type EmailCase
   *  Uses courier library to send email to registered user
   *  @return : Success if email send successfully or NOT Found if not able to send mail
   */
  def sendEmailReminder(email: EmailCase) : String = {
    sendEmail(email).onComplete {
        case Success(_) => logger.info("Email notification Sent!")
        case Failure(_) => complete(StatusCodes.NotFound,"Failed To Deliver Notification!")
      }
    "Mail send"
  }
}
