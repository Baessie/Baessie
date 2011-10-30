package org.baessie.common

trait TestData {
  val testId: String

  def matches(other: TestData): Boolean

  def incrementCallCount

  def callCount: Int

  def handleBackReferences(request: TestData): Option[TestData]
}
