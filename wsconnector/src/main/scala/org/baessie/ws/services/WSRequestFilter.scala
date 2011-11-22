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
import org.baessie.common.TestDataManager

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
      val source = new InputSource(request.getInputStream)
      val document = XMLUnit.buildControlDocument(source)
      val result = testDataManager.findMatching(new WSTestData("", document, null, null, null, 0, null))
      if (result.isDefined) {
        result.get.incrementCallCount
        val out = response.getOutputStream("text/xml")
        val domSource = new DOMSource(result.get.asInstanceOf[WSTestData].outControlDocument)
        val streamResult = new StreamResult(out)
        TransformerFactory.newInstance().newTransformer().transform(domSource, streamResult)
        out.flush()
      } else {
        // TODO: Fix error output
      }
    } else {
      handler.handlePageRender(parameters)
    }
  }
}
