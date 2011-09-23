package org.baessie.ws

import org.w3c.dom.Document
import org.baessie.common.TestData
import org.custommonkey.xmlunit.XMLUnit

class WSTestData(
  val testId: String,
  val inControlDocument: Document,
  val outControlDocument: Document,
  val inBackReferences: List[BackReferenceLocation],
  val outBackReferences: List[BackReferenceLocation],
  val delay: Int,
  val responseHeaders: Map[String, String]) extends TestData {
  var callCount: Int = _

  override def matches(other: Any): Boolean = {
    XMLUnit.setIgnoreWhitespace(true)
    XMLUnit.setIgnoreComments(true)
    XMLUnit.setIgnoreAttributeOrder(true)
    XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)

    other match {
      case that: WSTestData => {
        val diff = XMLUnit.compareXML(this.inControlDocument, that.inControlDocument)
        (that canEqual this) && diff.similar()
      }

      case _ => false
    }
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[WSTestData]
}
