package org.baessie.common

trait TestData {
  val testId: String

  def matches(other: Any): Boolean

  def incrementCallCount

  def callCount: Int
}
