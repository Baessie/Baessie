package org.baessie.ws

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.custommonkey.xmlunit.XMLUnit
import org.baessie.common.TestData
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.TransformerFactory
import java.io.ByteArrayOutputStream
import javax.xml.transform.stream.StreamResult


class WSTestDataSpec extends Spec with ShouldMatchers {
  type ? = this.type

  val reference = new WSTestData("reference", XMLUnit.buildControlDocument("<hello>content</hello>"), null, null, null, 0, null)

  describe("WSTestData") {
    it("should match identical requests") {
      val result = new WSTestData("other", XMLUnit.buildControlDocument("<hello>content</hello>"), null, null, null, 0, null) matches reference
      result should equal(true)
    }
    it("should ignore whitespace") {
      val result = new WSTestData("other", XMLUnit.buildControlDocument("<hello>\n content \n</hello>"), null, null, null, 0, null) matches reference
      result should equal(true)
    }
    it("should handle wildcards") {
      val result = new WSTestData("other", XMLUnit.buildControlDocument("<hello>*</hello>"), null, null, null, 0, null) matches reference
      result should equal(true)
    }
    it("should accept backreferences") {
      val wsTestData = new WSTestData("other", XMLUnit.buildControlDocument("<hello>*(TEST)*</hello>"), XMLUnit.buildControlDocument("<world>*(TEST)*</world>"), null, null, 0, null)
      val result = wsTestData matches reference
      val resultWithBackReferences = wsTestData.handleBackReferences(reference)
      result should equal(true)
    }
    it("should move data from request to response in accordance with backreferences") {
      val wsTestData = new WSTestData("other", XMLUnit.buildControlDocument("<hello>*(TEST)*</hello>"), XMLUnit.buildControlDocument("<world>*(TEST)*</world>"), null, null, 0, null)
      val result = wsTestData.handleBackReferences(reference)
      val domSource = new DOMSource(result.get.asInstanceOf[WSTestData].outControlDocument)
      val outputStream = new ByteArrayOutputStream()
      val streamResult = new StreamResult(outputStream)
      TransformerFactory.newInstance().newTransformer().transform(domSource, streamResult)

      new String(outputStream.toByteArray) should be("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><world>content</world>")
    }
    it("should not match other types than WSTestData") {
      val result = reference matches new TestDataMock
      result should equal(false)
    }
  }
}

private class TestDataMock extends TestData {
  val testId = "test"

  def matches(other: TestData) = false

  def incrementCallCount = {}

  def callCount = 0

  def handleBackReferences(request: TestData) = null
}
