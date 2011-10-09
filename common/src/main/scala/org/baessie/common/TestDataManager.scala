package org.baessie.common

trait TestDataManager {
  def findMatching(testData: TestData): Option[TestData]

  def add(testData: TestData)

  def clear() : Int
}
