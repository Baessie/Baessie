package org.baessie.services

import org.baessie.common.{TestDataManager, TestData}

class TestDataManagerImpl extends TestDataManager {
  private var data: List[TestData] = List()

  def add(testData: TestData) {
    data = testData :: data
  }

  def findMatching(testData: TestData): Option[TestData] = {
    val matchingTestData = data find (_ matches testData)
    return if (matchingTestData.isDefined) matchingTestData.get.handleBackReferences(testData) else matchingTestData
  }

  def clear(): Int = {
    val count = data.size
    data = Nil
    return count
  }

  def getCallCountForTestId(testId: String): Int = {
    val testData = data find (_.testId == testId)
    return if (testData.isDefined) testData.get.callCount else 0
  }

  def getTestDataCount = data.size

  def getAllTestData: List[TestData] = data
}
