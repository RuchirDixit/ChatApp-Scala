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
package com.bridgelabz.actors

import akka.actor.Actor
import com.bridgelabz.caseclasses.EmailCase
import com.bridgelabz.email.Email

class EmailNotificationActor extends Actor {
  def receive: Receive = {
    case receiverAddress:String =>
      val email = EmailCase(receiverAddress,"Full version Reminder",
        "Purchase full version for more exciting features for chatting. Pay $20 for full version. Buy Now!")
      Email.sendEmailReminder(email)
  }
}
