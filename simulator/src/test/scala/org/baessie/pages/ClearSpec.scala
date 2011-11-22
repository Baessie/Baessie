package org.baessie.pages

import org.scalatest.Spec
import org.apache.tapestry5.test.PageTester
import org.baessie.common.TestDataManager
import org.baessie.ws.WSTestData
import org.custommonkey.xmlunit.XMLUnit
import org.scalatest.matchers.ShouldMatchers
import org.baessie.services.TestModule
import org.baessie.ws.services.WsModule

class ClearSpec extends PageTester("org.baessie", "app", "src/main/webapp", classOf[TestModule], classOf[WsModule]) with Spec with ShouldMatchers {
  val manager = getService(classOf[TestDataManager])

  describe("Clear-page") {
    it("should clear the test data manager when onActivate is called") {
      manager.add(new WSTestData("test", XMLUnit.buildControlDocument("<hello>content</hello>"), null, null, null, 0, null))
      manager.getAllTestData.size should be(1)

      val page = renderPage("clear")

      page.toString should equal("<result>Testdata cleared: number of entries removed=1</result>")
      manager.getAllTestData.size should be(0)
    }
  }
}
