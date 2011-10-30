package org.baessie.ws

import scala.collection.JavaConverters._

import org.baessie.common.TestData
import org.custommonkey.xmlunit.{Difference, DetailedDiff, Diff, XMLUnit}
import java.util.{List, ArrayList}
import javax.xml.xpath.{XPathConstants, XPathFactory}
import org.w3c.dom.{NodeList, Document}
import javax.xml.transform.dom.{DOMResult, DOMSource}
import javax.xml.transform.TransformerFactory

class WSTestData(
                  val testId: String,
                  val inControlDocument: Document,
                  val outControlDocument: Document,
                  val inBackReferences: List[BackReferenceLocation],
                  val outBackReferences: List[BackReferenceLocation],
                  val delay: Int,
                  val responseHeaders: Map[String, String]) extends TestData {
  var callCount: Int = _
  val backReferenceRegexp = "\\*\\(([[a-zA-Z0-9_-]\\s]*)\\)\\*".r

  XMLUnit.setIgnoreWhitespace(true)
  XMLUnit.setIgnoreComments(true)
  XMLUnit.setIgnoreAttributeOrder(true)
  XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)

  override def matches(other: TestData): Boolean = {
    other match {
      case that: WSTestData => {
        val diff = XMLUnit.compareXML(this.inControlDocument, that.inControlDocument)
        (that canEqual this) && (diff.identical() || hasAcceptableDifferences(diff))
      }

      case _ => false
    }
  }

  def handleBackReferences(request: TestData): Option[TestData] = {
    val detailedDiff = new DetailedDiff(XMLUnit.compareXML(this.inControlDocument, request.asInstanceOf[WSTestData].inControlDocument))
    val differences = new ArrayList[Difference](detailedDiff.getAllDifferences.asInstanceOf[List[Difference]]).asScala
    val backReferenceDiffs = differences filter isBackReference _
    val backReferenceMap = backReferenceDiffs.map(diff => (diff.getControlNodeDetail.getValue, diff.getTestNodeDetail.getValue)).toMap
    val expression = XPathFactory.newInstance().newXPath().compile("//*")
    val result = copyDocument(this.outControlDocument)
    val nodes = expression.evaluate(result, XPathConstants.NODESET).asInstanceOf[NodeList]
    0 to nodes.getLength - 1 foreach (index => {
      if (backReferenceRegexp findFirstIn nodes.item(index).getTextContent isDefined) {
        nodes.item(index).setTextContent(backReferenceMap(nodes.item(index).getTextContent))
      }
    })

    Some(request.asInstanceOf[WSTestData].copy(outControlDocument = result))
  }

  def incrementCallCount = {
    callCount += 1
  }

  def copy(testId: String = this.testId,
           inControlDocument: Document = this.inControlDocument,
           outControlDocument: Document = this.outControlDocument,
           inBackReferences: List[BackReferenceLocation] = this.inBackReferences,
           outBackReferences: List[BackReferenceLocation] = this.outBackReferences,
           delay: Int = this.delay,
           responseHeaders: Map[String, String] = this.responseHeaders) = {
    new WSTestData(testId, inControlDocument, outControlDocument, inBackReferences, outBackReferences, delay, responseHeaders)
  }

  private def hasAcceptableDifferences(diff: Diff): Boolean = {
    val detailedDiff = new DetailedDiff(diff)
    var acceptable = true

    val differences = new ArrayList[Difference](detailedDiff.getAllDifferences.asInstanceOf[List[Difference]]).asScala

    differences foreach (diff => {
      if (diff.isRecoverable()) {
      } else if (isWildcard(diff)) {
      } else if (isBackReference(diff)) {
      } else {
        acceptable = false
      }
    })

    acceptable
  }

  private def isBackReference(diff: Difference): Boolean = {
    return backReferenceRegexp findFirstIn diff.getControlNodeDetail.getValue isDefined
  }

  private def isWildcard(diff: Difference): Boolean = {
    val controlValue = diff.getControlNodeDetail.getValue.replaceAll("\\*", ".*?")
    val firstMatch = controlValue.r findFirstIn diff.getTestNodeDetail.getValue
    if (firstMatch.isDefined) {
      true
    } else {
      false
    }
  }

  private def canEqual(other: Any): Boolean = other.isInstanceOf[WSTestData]

  private def copyDocument(document: Document): Document = {
    val source = new DOMSource(document)
    val result = new DOMResult()
    TransformerFactory.newInstance().newTransformer().transform(source, result)
    result.getNode().asInstanceOf[Document]
  }
}
