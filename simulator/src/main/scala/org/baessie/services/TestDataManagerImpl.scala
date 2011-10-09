package org.baessie.services

import collection.mutable.ListBuffer
import org.baessie.common.{TestDataManager, TestData}

class TestDataManagerImpl extends TestDataManager {
  private val data: ListBuffer[TestData] = ListBuffer()

  def add(testData: TestData) = {
    data += testData
  }

  def findMatching(testData: TestData): Option[TestData] = {
    return data find (_ matches testData)
  }

  def clear() = {
    val count = data.size
    data.clear()
    count
  }
}
