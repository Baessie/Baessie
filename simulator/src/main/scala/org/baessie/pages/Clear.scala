package org.baessie.pages

import org.apache.tapestry5.ioc.annotations.Inject
import org.baessie.common.TestDataManager
import org.slf4j.Logger
import org.apache.tapestry5.annotations.Property

class Clear {
  @Inject
  private var testDataManager: TestDataManager = _
  @Inject
  private var logger: Logger = _

  @Property(read = true, write = false)
  private var testCount = 0

  def onActivate() = {
    testCount = testDataManager.clear()
  }
}
