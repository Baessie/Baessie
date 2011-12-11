package org.baessie.socket

import org.baessie.common.TestData

class SocketTestData(val testId: String, val request: String, val response: String, val maxCallCount: Int, val closeAfterResponse: Boolean) extends TestData {
  def matches(other: TestData) = {
    other match {
      case that: SocketTestData => {
        request == that.request
      }
      case _ => false
    }
  }

  var callCount = 0

  def incrementCallCount = {
    callCount += 1
  }

  def handleBackReferences(request: TestData) = Some(this)
}
