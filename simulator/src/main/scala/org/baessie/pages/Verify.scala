package org.baessie.pages

import org.apache.tapestry5.ioc.annotations.Inject
import org.baessie.common.TestDataManager
import org.slf4j.Logger
import org.apache.tapestry5.annotations.ActivationRequestParameter

class Verify {
  @Inject
  private var testDataManager: TestDataManager = _
  @Inject
  private var logger: Logger = _
  @ActivationRequestParameter
  private var testId: String = ""

  private var currentCallcount = 0

  def onActivate() = {
    currentCallcount = testDataManager.getCallCountForTestId(testId)
  }

  def callcount = {
    currentCallcount
  }
}
