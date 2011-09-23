package org.baessie.ws.services

import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.services._
import javax.servlet.http.HttpServletRequest
import org.custommonkey.xmlunit.XMLUnit
import org.xml.sax.InputSource
import org.baessie.ws.WSTestData
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.TransformerFactory
import org.slf4j.Logger
import org.baessie.common.{TestDataManager, TestData}

class WSRequestFilter extends ComponentRequestFilter {
  @Inject
  var request: HttpServletRequest = _
  @Inject
  var response: Response = _
  @Inject
  var testDataManager: TestDataManager = _

  def handleComponentEvent(parameters: ComponentEventRequestParameters, handler: ComponentRequestHandler) {
    handler.handleComponentEvent(parameters)
  }

  def handlePageRender(parameters: PageRenderRequestParameters, handler: ComponentRequestHandler) {
    if (request.getContentLength > 0 && "xml".r.findFirstIn(request.getContentType).isDefined) {
      val source: InputSource = new InputSource(request.getInputStream)
      XMLUnit.setIgnoreWhitespace(true)
      XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)
      XMLUnit.setIgnoreAttributeOrder(true)
      val document = XMLUnit.buildControlDocument(source)
      val result = testDataManager.findMatching(new WSTestData("", document, null, null, null, 0, null))
      if (result.isDefined) {
        val out = response.getOutputStream("text/xml")
        val domSource: DOMSource = new DOMSource(result.get.asInstanceOf[WSTestData].outControlDocument);
        val streamResult: StreamResult = new StreamResult(out);
        TransformerFactory.newInstance().newTransformer().transform(domSource, streamResult);
        out.flush();
      }
    } else {
      handler.handlePageRender(parameters)
    }
  }
}
