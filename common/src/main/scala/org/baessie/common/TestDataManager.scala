package org.baessie.common

trait TestDataManager {
  def findMatching(testData: TestData): Option[TestData]

  def add(testData: TestData)

  def clear(): Int

  def getCallCountForTestId(testId: String): Int

  def getAllTestData: List[TestData]
}
