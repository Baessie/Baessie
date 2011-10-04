package org.baessie.common

trait TestData {
  def matches(other: Any): Boolean
  def incrementCallCount
}
