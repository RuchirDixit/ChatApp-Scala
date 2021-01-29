
package com.bridgelabz.traits

import com.bridgelabz.caseClasses.{ChatCase, GroupChat, User}
import org.mongodb.scala.Document
import scala.concurrent.Future

trait DatabaseService {

  /**
   *
   * @param credentials : User details
   * @return : String
   */
  def saveUser(credentials:User) : String

  /**
   *
   * @param name : User name
   * @return : Boolean
   */
  def checkIfExists(name : String): Boolean

  /**
   *
   * @param sendMessageRequest : save message to database
   * @return : String
   */
  def saveChatMessage(sendMessageRequest: ChatCase) : String

  // get All users
  def getUsers() : Future[Seq[Document]]

  /**
   *
   * @param name : User's name
   * @return : Future[Seq[Document]]
   */
  def getUsersUsingFilter(name : String) : Future[Seq[Document]]

  /**
   *
   * @param groupChatInfo : Save to group chat
   * @return : String
   */
  def saveToGroupChat(groupChatInfo: GroupChat) : String
}
