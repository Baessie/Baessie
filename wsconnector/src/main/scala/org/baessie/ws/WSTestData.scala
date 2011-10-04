package org.baessie.ws

import scala.collection.JavaConverters._

import org.w3c.dom.Document
import org.baessie.common.TestData
import org.custommonkey.xmlunit.{Difference, DetailedDiff, Diff, XMLUnit}
import java.util.{List, ArrayList}

class WSTestData(
                  val testId: String,
                  val inControlDocument: Document,
                  val outControlDocument: Document,
                  val inBackReferences: List[BackReferenceLocation],
                  val outBackReferences: List[BackReferenceLocation],
                  val delay: Int,
                  val responseHeaders: Map[String, String]) extends TestData {
  var callCount: Int = _

  XMLUnit.setIgnoreWhitespace(true)
  XMLUnit.setIgnoreComments(true)
  XMLUnit.setIgnoreAttributeOrder(true)
  XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)

  def hasAcceptableDifferences(diff: Diff): Boolean = {
    val detailedDiff = new DetailedDiff(diff)
    var acceptable = true

    val differences = new ArrayList[Difference](detailedDiff.getAllDifferences.asInstanceOf[List[Difference]]).asScala

    differences foreach (diff => {
      if (diff.isRecoverable()) {

      } else if (isWildcard(diff)) {

      } else {
        acceptable = false
      }
    })

    acceptable
  }

  def isWildcard(diff: Difference): Boolean = {
    val controlValue = diff.getControlNodeDetail.getValue.replaceAll("\\*", ".*?")
    val firstMatch = controlValue.r findFirstIn diff.getTestNodeDetail.getValue
    if (firstMatch.isDefined) {
      true
    } else {
      false
    }
  }

  override def matches(other: Any): Boolean = {
    other match {
      case that: WSTestData => {
        val diff = XMLUnit.compareXML(this.inControlDocument, that.inControlDocument)
        (that canEqual this) && (diff.identical() || hasAcceptableDifferences(diff))
      }

      case _ => false
    }
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[WSTestData]

  def incrementCallCount = {
    callCount += 1
  }
}
