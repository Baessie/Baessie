package org.baessie.services

import collection.mutable.ListBuffer
import org.baessie.common.{TestDataManager, TestData}

class TestDataManagerImpl extends TestDataManager {
  private val data: ListBuffer[TestData] = ListBuffer()

  def add(testData: TestData) = {
    data += testData
  }

  def findMatching(testData: TestData): Option[TestData] = {
    val matchingTestData = data find (_ matches testData)
    val result = if (matchingTestData.isDefined) matchingTestData.get.handleBackReferences(testData) else matchingTestData
    return result
  }

  def clear() = {
    val count = data.size
    data.clear()
    count
  }

  def getCallCountForTestId(testId: String) = {
    val testData = data find (_.testId == testId)
    if (testData.isDefined) testData.get.callCount else 0
  }

  def getTestDataCount = data.size
}
