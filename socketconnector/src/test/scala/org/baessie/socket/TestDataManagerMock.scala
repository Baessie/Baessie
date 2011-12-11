package org.baessie.socket

import org.baessie.common.{TestData, TestDataManager}

class TestDataManagerMock extends TestDataManager {
  var data = List[TestData]()

  def findMatching(testData: TestData) = data find (_ matches testData)

  def add(testData: TestData) = {
    data = testData :: data
  }

  def clear() = 0

  def getCallCountForTestId(testId: String) = 0

  def getAllTestData = null
}
