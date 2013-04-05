package org.baessie.simulator.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.baessie.simulator.ws.BackReferenceLocation;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimulatorUtils {

   private static final Logger LOG = LoggerFactory.getLogger(SimulatorUtils.class);

   public static final String CHARSET = "UTF-8";
   public static final String CONTENT_TYPE_TEXT = "text/plain";
   public static final String CONTENT_TYPE_XML = "text/xml";
   public static final String TEST_ID = "testId";
   public static final Pattern BACK_REFERENCE_PATTERN = Pattern.compile("\\*\\(([[a-zA-Z0-9_-]\\s]*)\\)\\*");
   private static final String ENCODING = java.nio.charset.Charset.defaultCharset().name();
   public static final String WILDCARD = "*";
   private static final String REGEX_WILDCARD = "\\\\E.*?\\\\Q";
   private static final String ESCAPED_WILDCARD = "\\*";

   private static TransformerFactory transformerFactory;
   static {
      transformerFactory = TransformerFactory.newInstance();
      XMLUnit.setIgnoreWhitespace(true);
      XMLUnit.setNormalizeWhitespace(true);
      XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
      XMLUnit.setIgnoreAttributeOrder(true);
   }

   public static void writeTextResponse(final HttpServletResponse response, final String msg) {
      response.setContentType(CONTENT_TYPE_TEXT);
      response.setCharacterEncoding(CHARSET);
      ServletOutputStream out = null;
      try {
         out = response.getOutputStream();
         out.write(msg.getBytes(CHARSET));
         out.flush();
         LOG.info("Wrote text response: " + msg);
      } catch (final Exception e) {
         LOG.error("Failed to write text response: " + msg, e);
      } finally {
         if (out != null) {
            try {
               out.close();
            } catch (final IOException e) {
               LOG.warn("Failed to close output stream", e);
            }
         }
      }
   }

   public static void writeXmlResponse(final HttpServletResponse response, final Document outTestDocument) {
      OutputStream out = null;

      String charset = outTestDocument.getXmlEncoding();
      try {
         Charset.forName(charset);
      } catch (final Exception e) {
         charset = SimulatorUtils.CHARSET;
      }

      response.setContentType(SimulatorUtils.CONTENT_TYPE_XML);
      response.setCharacterEncoding(charset);

      try {
         out = response.getOutputStream();
         final DOMSource source = new DOMSource(outTestDocument);
         final StreamResult result = new StreamResult(out);
         newTransformer().transform(source, result);
         out.flush();
         LOG.info("Wrote xml response");
      } catch (final Exception e) {
         LOG.error("Failed to write xml response", e);
      } finally {
         if (out != null) {
            try {
               out.close();
            } catch (final IOException e) {
               LOG.warn("Failed to close output stream", e);
            }
         }
      }
   }

   public static void replaceBackReferences(final Document document, final List<BackReferenceLocation> outBackReferences,
         final List<BackReferenceValue> backReferenceValues, final Map<String, String> nameSpaces) {
      final XPath xPath = XPathFactory.newInstance().newXPath();
      if (nameSpaces != null) {
         xPath.setNamespaceContext(new MapNamspaceContext(nameSpaces));
      } else {
         xPath.setNamespaceContext(new DocumentNamspaceContext(document));
      }

      for (final BackReferenceLocation outBackReference : outBackReferences) {
         final String id = outBackReference.getId();
         innerLoop: for (final BackReferenceValue backReferenceValue : backReferenceValues) {
            if (id.equals(backReferenceValue.getId())) {

               try {
                  final NodeList nodeList = (NodeList) xPath.evaluate(outBackReference.getLocation(), document, XPathConstants.NODESET);

                  for (int i = 0; i < nodeList.getLength(); i++) {
                     final Node node = nodeList.item(i);
                     String newNodeValue = node.getNodeValue();

                     int pos;
                     while ((pos = newNodeValue.indexOf(id)) >= 0) {
                        newNodeValue = newNodeValue.substring(0, pos) + backReferenceValue.getValue() + newNodeValue.substring(pos + id.length());
                     }
                     LOG.debug("Replacing back reference : {} <=> {} : {}", new Object[] { id, backReferenceValue.getValue(), outBackReference.getLocation() });

                     node.setNodeValue(newNodeValue);
                  }
               } catch (final Exception e) {
                  LOG.warn("Failed to find nodes in response: " + outBackReference.getLocation(), e);
               }

               break innerLoop;
            }
         }
      }
   }

   public static Document copyDocument(final Document document) {
      Document copy = null;
      final DOMSource source = new DOMSource(document);
      final DOMResult result = new DOMResult();
      try {
         SimulatorUtils.newTransformer().transform(source, result);
         copy = (Document) result.getNode();
      } catch (final TransformerException e) {
      }
      return copy;
   }

   public static Transformer newTransformer() throws TransformerConfigurationException {
      return transformerFactory.newTransformer();
   }

   public static void delay(final Integer delay) {
      if (delay != null && delay > 0) {
         try {
            Thread.sleep(delay.longValue());
         } catch (final InterruptedException e) {
         }
      }
   }

   public static List<BackReferenceLocation> createBackReferences(final String[] strs, final Document document) {
      final List<BackReferenceLocation> list = new ArrayList<BackReferenceLocation>();
      if (strs != null) {

         final XPath xPath = XPathFactory.newInstance().newXPath();
         xPath.setNamespaceContext(new DocumentNamspaceContext(document));

         for (final String xpath : strs) {
            try {
               final String value = xPath.evaluate(xpath, document);
               final Matcher matcher = SimulatorUtils.BACK_REFERENCE_PATTERN.matcher(value);
               while (matcher.find()) {
                  final String id = matcher.group();
                  list.add(new BackReferenceLocation(id, xpath));
               }
            } catch (final Exception e) {
               LOG.warn("Failed to resolve xpath expression: " + xpath + ", " + e.getMessage());
            }
         }
      }
      return list;
   }

   public static void scanForBackReferences(final Document document, final List<BackReferenceLocation> backReferences, final Map<String, String> nameSpaces) {
      final List<BackReferenceLocation> list = backReferences;
      LOG.debug("Scanning for back references");
      final long t0 = System.currentTimeMillis();
      scanNodeForBackReferences(document.getFirstChild(), list, "/" + document.getFirstChild().getNodeName() + "[1]", nameSpaces);
      final long t1 = System.currentTimeMillis();
      LOG.debug("Scanning for back references in document completed: {} ms", (t1 - t0));

   }

   private static void scanNodeForBackReferences(final Node node, final List<BackReferenceLocation> list, final String xpath,
         final Map<String, String> nameSpaces) {

      final String prefix = node.getPrefix();
      if (prefix != null && !nameSpaces.containsKey(prefix)) {
         nameSpaces.put(prefix, node.getNamespaceURI());
      }

      final NodeList childNodes = node.getChildNodes();
      if (childNodes != null) {
         final Map<String, Integer> childCounts = new HashMap<String, Integer>();
         for (int i = 0; i < childNodes.getLength(); i++) {
            final Node childNode = childNodes.item(i);
            String nodeName = childNode.getNodeName();
            Integer childCount = childCounts.get(nodeName);
            if (childCount == null) {
               childCount = 1;
            } else {
               childCount++;
            }
            childCounts.put(nodeName, childCount);
            if (Node.TEXT_NODE == childNode.getNodeType()) {
               scanNodeForBackReferences(childNode, list, xpath + "/text()" + "[" + childCount + "]", nameSpaces);
            } else {
               if (childNode.getPrefix() == null && childNode.getNamespaceURI() != null) {
                  nodeName = "*[local-name()='" + nodeName + "']";
               }

               scanNodeForBackReferences(childNode, list, xpath + "/" + nodeName + "[" + childCount + "]", nameSpaces);
            }
         }
      }

      final NamedNodeMap attributes = node.getAttributes();
      if (attributes != null) {
         for (int i = 0; i < attributes.getLength(); i++) {
            final Node attribute = attributes.item(i);
            scanNodeForBackReferences(attribute.getFirstChild(), list, xpath + "/@" + attribute.getNodeName(), nameSpaces);
         }
      }

      final String nodeValue = node.getNodeValue();
      if (nodeValue != null) {
         final Matcher matcher = BACK_REFERENCE_PATTERN.matcher(nodeValue);
         while (matcher.find()) {
            final String id = matcher.group();
            LOG.debug("Found back reference: {} : {}", id, xpath);
            list.add(new BackReferenceLocation(id, xpath));
         }
      }
   }

   public static Map<String, String> readHeaders(final String[] strs) {
      final Map<String, String> headers = new HashMap<String, String>();
      if (strs != null) {
         for (final String responseHeader : strs) {
            final int index = responseHeader.indexOf("=");
            if (index > 0) {
               final String key = responseHeader.substring(0, index);
               final String value = responseHeader.substring(index + 1);
               headers.put(key, value);
            }
         }
      }
      return headers;
   }

   public static List<BackReferenceLocation> scanForBackReferences(final Map<String, String> queryStringParameters) {
      final List<BackReferenceLocation> list = new ArrayList<BackReferenceLocation>();

      for (final String key : queryStringParameters.keySet()) {
         final String value = queryStringParameters.get(key);
         final Matcher matcher = BACK_REFERENCE_PATTERN.matcher(value);
         while (matcher.find()) {
            final String id = matcher.group();
            LOG.debug("Found back reference: {} : {}", id, key);
            list.add(new BackReferenceLocation(id, key));
         }
      }

      return list;
   }

   public static Map<String, String> readQueryStringParameters(final String queryString) {
      final Map<String, String> map = new HashMap<String, String>();
      if (queryString != null) {
         final String[] kvps = queryString.split("&");
         for (final String kvp : kvps) {
            // Find first equal sign. It is not possible to use
            // 'kvp.split("=")' because a query may contain several equal
            // signs.
            final int nIndexOfEqualSign = kvp.indexOf('=');
            if (nIndexOfEqualSign > 0 && nIndexOfEqualSign + 1 < kvp.length()) {
               final String key = kvp.substring(0, nIndexOfEqualSign);
               String value = kvp.substring(nIndexOfEqualSign + 1);
               if (shouldDecode(value)) {
                  value = decode(value);
               }
               map.put(key, value);
            }
         }
      }
      return map;
   }

   public static String decode(final String text) {
      String actualText = text;
      try {
         actualText = URLDecoder.decode(text, ENCODING);
      } catch (final UnsupportedEncodingException e) {
         LOG.warn("UnsupportedEncodingException: could not decode {} due to {}", text, e);
      }
      return actualText;
   }

   public static boolean shouldDecode(final String text) {
      return text != null && text.length() > 0 && text.charAt(0) != '<';
   }

   public static boolean isDecodable(final String text) {
      return text != null && text.length() > 0 && text.charAt(0) == '<';
   }

   public static List<BackReferenceValue> doesXMLDocumentsMatch(final Document source, final Document compare) {
      List<BackReferenceValue> localBackReferenceValues = null;
      final DetailedDiff detailedDiff = new DetailedDiff(XMLUnit.compareXML(source, compare));
      detailedDiff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
      localBackReferenceValues = new ArrayList<BackReferenceValue>();
      if (!detailedDiff.identical() && !acceptableDifferences(detailedDiff, localBackReferenceValues)) {
         localBackReferenceValues = null;
      }

      return localBackReferenceValues;
   }

   public static boolean acceptableDifferences(final DetailedDiff detailedDiff, final List<BackReferenceValue> backReferenceValues) {
      boolean acceptable = true;
      @SuppressWarnings("unchecked")
      final List<Difference> allDifferences = detailedDiff.getAllDifferences();
      for (final Difference diff : allDifferences) {
         if (diff.isRecoverable()) {
            LOG.debug("Found recoverable diff: {}", getDiffInfo(diff));
         } else if (isBackReference(diff)) {
            LOG.debug("Found back reference diff: {}", getDiffInfo(diff));
            backReferenceValues.add(createBackReferenceValueFromDiff(diff));
         } else if (isWildCard(diff)) {
            LOG.debug("Found wildcard diff: {}", getDiffInfo(diff));
         } else {
            acceptable = false;
            LOG.warn("Found unacceptable diff: {}", getDiffInfo(diff));
         }
      }
      return acceptable;
   }

   private static BackReferenceValue createBackReferenceValueFromDiff(final Difference diff) {
      return new BackReferenceValue(diff.getControlNodeDetail().getValue(), diff.getTestNodeDetail().getValue());
   }

   private static String getDiffInfo(final Difference diff) {
      return "Req: " + diff.getControlNodeDetail().getValue() + " <=> Test: " + diff.getTestNodeDetail().getValue() + " : "
            + diff.getTestNodeDetail().getXpathLocation() + " : " + diff.getDescription();
   }

   public static boolean isBackReference(final Difference diff) {
      return isBackReference(diff.getControlNodeDetail().getValue());
   }

   public static boolean isBackReference(final String str) {
      final Matcher matcher = SimulatorUtils.BACK_REFERENCE_PATTERN.matcher(str);
      return matcher.find();
   }

   private static boolean isWildCard(final Difference diff) {
      boolean wildcard;
      final long t0 = System.currentTimeMillis();
      final String controlValue = diff.getControlNodeDetail().getValue();
      if (controlValue.contains(WILDCARD)) {
         final String regexp = createRegexp(controlValue);
         final String testValue = diff.getTestNodeDetail().getValue();
         wildcard = isBackReference(testValue) || testValue.matches(regexp);
      } else {
         wildcard = false;
      }
      final long t1 = System.currentTimeMillis();
      LOG.debug("Time to check for wildcard: {} ms", t1 - t0);
      return wildcard;
   }

   private static String createRegexp(final String str) {
      return Pattern.quote(str).replaceAll(ESCAPED_WILDCARD, REGEX_WILDCARD);
   }

}
