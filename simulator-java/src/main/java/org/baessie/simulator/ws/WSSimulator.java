package org.baessie.simulator.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.baessie.simulator.SimulatorException;
import org.baessie.simulator.SimulatorRequest;
import org.baessie.simulator.SimulatorResponse;
import org.baessie.simulator.util.BackReferenceValue;
import org.baessie.simulator.util.DocumentNamspaceContext;
import org.baessie.simulator.util.MapNamspaceContext;
import org.baessie.simulator.util.SimulatorUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WSSimulator {

   public static final String IN_DATA = "request";
   public static final String OUT_DATA = "response";
   public static final String DELAY = "delay";

   public static final String IN_BACK_REFERENCES = "requestBackReferences";
   public static final String OUT_BACK_REFERENCES = "responseBackReferences";
   public static final String SCAN_FOR_BACK_REFERENCES = "scanBackReferences";

   public static final String RESPONSE_HEADERS = "responseHeaders";

   public static final String WILDCARD = "*";
   private static final String REGEX_WILDCARD = "\\\\E.*?\\\\Q";
   private static final String ESCAPED_WILDCARD = "\\*";

   private static final Logger LOG = LoggerFactory.getLogger(WSSimulator.class);

   private List<WSTestData> testDatas;

   public WSSimulator() {
      // Since the WSSimulator may be invoked concurrently we must protect the
      // testDatas list, which is modified during test execution.
      testDatas = Collections.synchronizedList(new ArrayList<WSTestData>());
   }

   public int clearTests() {
      int numberOfRemovedTests = testDatas.size();
      testDatas.clear();
      LOG.info("Cleared all WS tests");
      return numberOfRemovedTests;
   }

   public String setupTest(final SimulatorRequest request) {
      final String charset = request.getCharset();
      LOG.debug("Setup WS is using charset: {}", charset);

      try {
         final String testId = createAndStoreTest(request);
         LOG.info("Added WS test with testId: {}", testId);
         return testId;
      } catch (final Exception e) {
         LOG.warn("Failed to add WS test", e);
         return null;
      }

   }

   public Integer verifyTest(final String testId) {
      if (testId != null) {
         synchronized (testDatas) {
            for (final WSTestData testData : testDatas) {
               if (testId.equals(testData.getTestId())) {
                  LOG.info("Verified WS test with testId: {}", testId);
                  return testData.getCallCount();
               }
            }
         }
      }
      LOG.info("Failed to verify WS test with testId: " + testId);
      return null;
   }

   public SimulatorResponse executeTest(final SimulatorRequest request) throws SimulatorException {
      LOG.info("execute WS test");
      Document inTestDocument = null;
      try {
         inTestDocument = readDocumentFromRequest(request);
      } catch (final IOException e) {
         LOG.warn("Failed to read body from request for WS test");
         throw new SimulatorException("Failed to read body from request", e);
      } catch (final SAXException e) {
         LOG.warn("Failed to parse xml from request for WS test");
         throw new SimulatorException("Failed to parse xml from request", e);
      }

      final List<BackReferenceValue> backReferenceValues = new ArrayList<BackReferenceValue>();
      final WSTestData testdata = findMatchingTestData(inTestDocument, backReferenceValues);
      if (testdata != null) {
         SimulatorUtils.delay(testdata.getDelay());
         testdata.incCallCount();
         final Document outTestDocument = createResponseDocument(testdata, inTestDocument);
         LOG.info("executed WS test successfully");

         final SimulatorResponse response = new SimulatorResponse();
         response.setHeaders(testdata.getResponseHeaders());
         response.setDocument(outTestDocument);
         return response;
      } else {
         LOG.warn("Failed to find matching WS test");
         throw new SimulatorException("Failed to find matching testdata for the request");
      }

   }

   private Document createResponseDocument(final WSTestData testdata, final Document inTestDocument) {

      final Document outTestDocument = SimulatorUtils.copyDocument(testdata.getOutControlDocument());

      if (outTestDocument != null && !testdata.getInBackReferences().isEmpty() && !testdata.getOutBackReferences().isEmpty()) {

         final List<BackReferenceValue> backReferenceValues = getBackReferenceValues(inTestDocument, testdata);

         SimulatorUtils.replaceBackReferences(outTestDocument, testdata.getOutBackReferences(), backReferenceValues, testdata.getOutNameSpaces());
      }

      return outTestDocument;
   }

   private List<BackReferenceValue> getBackReferenceValues(final Document inTestDocument, final WSTestData testdata) {

      final Map<String, String> nameSpaces = testdata.getInNameSpaces();
      final List<BackReferenceLocation> inBackReferences = testdata.getInBackReferences();

      final List<BackReferenceValue> list = new ArrayList<BackReferenceValue>();
      final XPath xPath = XPathFactory.newInstance().newXPath();

      if (nameSpaces != null) {
         xPath.setNamespaceContext(new MapNamspaceContext(nameSpaces));
      } else {
         xPath.setNamespaceContext(new DocumentNamspaceContext(testdata.getInControlDocument()));
      }

      for (final BackReferenceLocation wsBackReference : inBackReferences) {
         try {
            final NodeList matchingNodes = (NodeList) xPath.evaluate(wsBackReference.getLocation(), inTestDocument, XPathConstants.NODESET);

            if (matchingNodes != null && matchingNodes.getLength() > 0) {
               final String value = matchingNodes.item(0).getNodeValue();
               list.add(new BackReferenceValue(wsBackReference.getId(), value));
            }
         } catch (final Exception e) {
            LOG.warn("Failed to find nodes in request: " + wsBackReference.getLocation(), e);
         }
      }
      return list;
   }

   private WSTestData findMatchingTestData(final Document inTestDocument, final List<BackReferenceValue> backReferenceValues) {
      WSTestData matchingTestData = null;
      List<BackReferenceValue> localBackReferenceValues = null;
      synchronized (testDatas) {
         for (final WSTestData testData : testDatas) {
            final Document inControlDocument = testData.getInControlDocument();
            localBackReferenceValues = SimulatorUtils.doesXMLDocumentsMatch(inControlDocument, inTestDocument);
            if (localBackReferenceValues != null) {
               matchingTestData = testData;
               break;
            }
         }

         if (matchingTestData != null) {
            // Put last in list to possibly improve performance
            testDatas.remove(matchingTestData);
            testDatas.add(matchingTestData);

            backReferenceValues.addAll(localBackReferenceValues);
         }
      }
      return matchingTestData;
   }

   private String createAndStoreTest(final SimulatorRequest request) throws Exception {

      final String testId = request.getParameter(SimulatorUtils.TEST_ID);
      if (testId != null) {
         final WSTestData testData = new WSTestData();
         testData.setTestId(testId);
         testData.setInControlDocument(XMLUnit.buildControlDocument(request.getParameter(IN_DATA)));
         testData.setOutControlDocument(XMLUnit.buildControlDocument(request.getParameter(OUT_DATA)));

         final String[] responseHeaders = request.getParameterValues(RESPONSE_HEADERS);
         testData.setResponseHeaders(SimulatorUtils.readHeaders(responseHeaders));

         final String[] inBR = request.getParameterValues(IN_BACK_REFERENCES);
         final String[] outBR = request.getParameterValues(OUT_BACK_REFERENCES);
         final boolean scanBR = request.getBooleanParameter(SCAN_FOR_BACK_REFERENCES);

         if (!scanBR && inBR != null && outBR != null) {
            testData.setInBackReferences(SimulatorUtils.createBackReferences(inBR, testData.getInControlDocument()));
            testData.setOutBackReferences(SimulatorUtils.createBackReferences(outBR, testData.getOutControlDocument()));
         } else {
            final List<BackReferenceLocation> inBackRefs = new ArrayList<BackReferenceLocation>();
            final Map<String, String> inNameSpaces = new HashMap<String, String>();
            SimulatorUtils.scanForBackReferences(testData.getInControlDocument(), inBackRefs, inNameSpaces);
            testData.setInBackReferences(inBackRefs);
            testData.setInNameSpaces(inNameSpaces);

            final List<BackReferenceLocation> outBackRefs = new ArrayList<BackReferenceLocation>();
            final Map<String, String> outNameSpaces = new HashMap<String, String>();
            SimulatorUtils.scanForBackReferences(testData.getOutControlDocument(), outBackRefs, outNameSpaces);
            testData.setOutBackReferences(outBackRefs);
            testData.setOutNameSpaces(outNameSpaces);
         }

         testData.setDelay(request.getIntegerParameter(DELAY));
         testDatas.add(testData);
         return testId;
      } else {
         throw new Exception("Missing parameter: " + SimulatorUtils.TEST_ID);
      }
   }

   private Document readDocumentFromRequest(final SimulatorRequest request) throws IOException, SAXException {
      final String charset = request.getCharset();

      Document inTestDocument = null;
      InputStream in = null;
      try {
         in = request.getInputStream();
         final InputSource is = new InputSource(in);
         is.setEncoding(charset);
         inTestDocument = XMLUnit.buildTestDocument(is);
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (final IOException e) {
            }
         }
      }
      return inTestDocument;
   }

   public void removeTest(final String testId) {
      if (testId != null) {
         final List<WSTestData> toBeRemoved = new ArrayList<WSTestData>();
         synchronized (testDatas) {
            for (final WSTestData testData : testDatas) {
               if (testId.equals(testData.getTestId())) {
                  toBeRemoved.add(testData);
               }
            }
            testDatas.removeAll(toBeRemoved);
         }
      }
   }
}
