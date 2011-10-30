package org.baessie.pages

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.baessie.ws.WSTestData
import org.custommonkey.xmlunit.XMLUnit
import org.baessie.services.TestDataManagerImpl
import org.baessie.testtools.PageTestTool


class VerifySpec extends Spec with ShouldMatchers {
  describe("Verify-page") {
    it("should return the current call count for the provided request when onActivate is called") {
      val testData = new WSTestData("test", XMLUnit.buildControlDocument("<hello>content</hello>"), null, null, null, 0, null)
      val manager = new TestDataManagerImpl
      val page = new Verify

      PageTestTool.inject(page, Array(manager))
      PageTestTool.setRequestParameters(page, Map("testId" -> "test"))

      manager.add(testData)
      testData.incrementCallCount
      testData.incrementCallCount

      page.onActivate()

      page.getCallcount should be(2)
    }
  }
}
