
package com.bridgelabz.caseclasses

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class ChatAppCaseTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest {
  "Check if sender exists" should {
    "return true" in {
      val status = ChatCase(Some("ruchircool007@gmail.com"),"xyz@gmail.com","hey",Some(""))
      assert(status == ChatCase(Some("ruchircool007@gmail.com"),"xyz@gmail.com","hey",Some("")))
    }
  }
}
