package org.baessie.services

import org.scalatest.matchers.ShouldMatchers
import org.baessie.common.TestData
import org.scalatest.{OneInstancePerTest, Spec}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestDataManagerImplSpec extends Spec with ShouldMatchers with OneInstancePerTest {
  val manager = new TestDataManagerImpl

  describe("TestDataManagerImpl") {
    it("should be possible to add test data") {
      manager.getTestDataCount should be(0)
      val testData = new TestDataMock
      manager add testData
      manager.getTestDataCount should be(1)
    }

    it("should return matching test data") {
      val testData = new TestDataMock
      manager add testData

      manager.findMatching(new TestDataMock).isDefined should be(true)
    }

    it("should ask test data to handle back references between actual request and found response") {
      val testData = new TestDataMock
      manager add testData

      val request = new TestDataMock
      manager.findMatching(request)

      testData.handleBackReferencesCalled should be(true)
    }
  }
}

private class TestDataMock extends TestData {
  val testId                     = "test"
  var handleBackReferencesCalled = false

  def matches(other: TestData) = true

  def incrementCallCount = {}

  def callCount = 0

  def handleBackReferences(request: TestData): Option[TestData] = {
    handleBackReferencesCalled = true
    Some(request)
  }
}
