package org.baessie.ws

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.matchers.MustMatchers
import org.custommonkey.xmlunit.XMLUnit


class WSTestDataSpec extends Spec with ShouldMatchers with MustMatchers {
  type ? = this.type

  val reference = new WSTestData("reference", XMLUnit.buildControlDocument("<hello>content</hello>"), null, null, null, 0, null)

  describe("WSTestData") {
    it("should match identical requests") {
      val result = new WSTestData("other", XMLUnit.buildControlDocument("<hello>content</hello>"), null, null, null, 0, null) matches reference
      result should equal (true)
    }
    it("should ignore whitespace") {
      val result = new WSTestData("other", XMLUnit.buildControlDocument("<hello>\n content \n</hello>"), null, null, null, 0, null) matches reference
      result should equal (true)
    }
    it("should handle wildcards") {
      val result = new WSTestData("other", XMLUnit.buildControlDocument("<hello>*</hello>"), null, null, null, 0, null) matches reference
      result should equal (true)
    }
    it("should not match other types than WSTestData") {
      val result = reference matches this
      result should equal (false)
    }
  }
}
