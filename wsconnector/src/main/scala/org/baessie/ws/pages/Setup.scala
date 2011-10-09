package org.baessie.ws.pages

import org.custommonkey.xmlunit.XMLUnit
import org.apache.tapestry5.ioc.annotations.Inject
import org.baessie.ws.WSTestData
import org.baessie.common.TestDataManager
import org.slf4j.Logger
import org.apache.tapestry5.annotations.{Property, ActivationRequestParameter, RequestParameter}

class Setup {
  @Inject
  private var testDataManager: TestDataManager = _
  @Inject
  private var logger: Logger = _

  @Property(read = true, write = false)
  @ActivationRequestParameter
  private var testId: String = ""
  @ActivationRequestParameter
  private var request: String = ""
  @ActivationRequestParameter
  private var response: String = ""
  @ActivationRequestParameter
  private var delay: Int = 0
  @ActivationRequestParameter
  private var responseHeaders: Array[String] = null
  @ActivationRequestParameter
  private var requestBackReferences: Array[String] = null
  @ActivationRequestParameter
  private var responseBackReferences: Array[String] = null
  @ActivationRequestParameter
  private var scanBackReferences: Boolean = false

  def onActivate() = {
    val inControlDocument = XMLUnit.buildControlDocument(request)
    val outControlDocument = XMLUnit.buildControlDocument(response)

    // TODO: Add support for back references and response headers

    testDataManager.add(new WSTestData(testId, inControlDocument, outControlDocument, null, null, delay, null))

    logger.debug("---> added " + testId)
  }
}
