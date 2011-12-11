package org.baessie.socket.pages

import org.apache.tapestry5.ioc.annotations.Inject
import org.baessie.common.TestDataManager
import org.slf4j.Logger
import org.apache.tapestry5.annotations.{ActivationRequestParameter, Property}
import org.baessie.socket.SocketTestData

class Setup {
  @Inject
  private var testDataManager: TestDataManager = _
  @Inject
  private var logger         : Logger          = _

  @Property(read = true, write = false)
  @ActivationRequestParameter
  private var testId            : String  = ""
  @ActivationRequestParameter
  private var request           : String  = ""
  @ActivationRequestParameter
  private var response          : String  = ""
  @ActivationRequestParameter
  private var maxCallCount      : Int     = 0
  @ActivationRequestParameter
  private var closeAfterResponse: Boolean = false

  def onActivate() = {
    testDataManager.add(new SocketTestData(testId, request.trim, response.replaceAll("\\n", "\n"), maxCallCount, closeAfterResponse))

    logger.debug("---> added " + testId)
  }
}
