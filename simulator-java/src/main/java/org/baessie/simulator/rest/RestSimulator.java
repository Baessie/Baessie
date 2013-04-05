package org.baessie.simulator.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baessie.simulator.SimulatorException;
import org.baessie.simulator.SimulatorRequest;
import org.baessie.simulator.SimulatorResponse;
import org.baessie.simulator.util.BackReferenceValue;
import org.baessie.simulator.util.SimulatorUtils;
import org.baessie.simulator.ws.BackReferenceLocation;
import org.custommonkey.xmlunit.XMLUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class RestSimulator {

   private static final Logger LOG = LoggerFactory.getLogger(RestSimulator.class);

   public static final String PATH = "path";
   public static final String QUERYSTRING = "queryString";
   public static final String OUT_DATA = "response";
   public static final String DELAY = "delay";
   public static final String RESPONSE_HEADERS = "responseHeaders";

   public static final String OUT_BACK_REFERENCES = "responseBackReferences";
   public static final String SCAN_FOR_BACK_REFERENCES = "scanBackReferences";

   private final List<RestTestData> testDatas;

   public RestSimulator() {
      testDatas = new ArrayList<RestTestData>();
   }

   public int clearTests() {
      final int numberOfRemovedTests = testDatas.size();
      testDatas.clear();
      LOG.info("Cleared all REST tests");
      return numberOfRemovedTests;
   }

   public String setupTest(final SimulatorRequest request) {
      final String charset = request.getCharset();
      LOG.info("Setup REST is using charset: " + charset);

      try {
         final String testId = createAndStoreTest(request);
         LOG.info("Added REST test with testId: " + testId);
         return testId;
      } catch (final MissingParameterException e) {
         LOG.warn("Failed to add REST test due to missing parameter", e);
         return null;
      }
   }

   public SimulatorResponse executeTest(final SimulatorRequest request) throws SimulatorException {
      LOG.info("execute REST test");
      Map<String, String> queryStringParameters = SimulatorUtils.readQueryStringParameters(readQueryStringFromRequest(request));

      if (isEmpty(queryStringParameters)) {
         queryStringParameters = RestSimulatorUtils.getQueryStringParametersFromRequest(request.getParameters());
      }

      final String path = trim(request.getPath());

      final RestTestData testdata = findMatchingTestData(queryStringParameters, path);
      if (testdata == null) {
         LOG.warn("Failed to find matching REST test");
         throw new SimulatorException("Failed to find matching testdata for the request");
      } else {
         SimulatorUtils.delay(testdata.getDelay());
         testdata.incCallCount();
         final SimulatorResponse response = new SimulatorResponse();
         if (testdata.getOutControlDocument() == null) {
            final String outText = createResponseText(testdata, queryStringParameters, path);
            response.setHeaders(testdata.getResponseHeaders());
            response.setText(outText);
         } else {
            final Document outTestDocument = createResponseDocument(testdata, queryStringParameters, path);
            LOG.info("executed REST test successfully");

            // TODO: Add back reference processing here

            response.setHeaders(testdata.getResponseHeaders());
            response.setDocument(outTestDocument);
         }
         return response;
      }
   }

   private boolean isEmpty(final Map<String, String> map) {
      return map == null || map.isEmpty();
   }

   private RestTestData findMatchingTestData(final Map<String, String> queryStringParameters, final String path) {
      RestTestData matchingTestData = null;

      LOG.debug("Request query string: " + queryStringParameters);

      searchloop: for (final RestTestData testData : testDatas) {
         LOG.debug("Test query string:    " + testData.getQueryStringParameters());
         if (acceptableQueryString(queryStringParameters, testData.getQueryStringParameters()) && acceptablePath(path, testData.getPath())) {
            matchingTestData = testData;
            break searchloop;
         } else {

         }
      }

      if (matchingTestData != null) {
         // Put last in list to possibly improve performance
         testDatas.remove(matchingTestData);
         testDatas.add(matchingTestData);
      }

      return matchingTestData;
   }

   private boolean acceptablePath(final String requestPath, final String testPath) {
      if (testPath != null) {
         return requestPath.endsWith(testPath);
      } else {
         return true;
      }
   }

   private boolean acceptableQueryString(final Map<String, String> requestParameters, final Map<String, String> testParameters) {
      boolean acceptable = true;
      if (requestParameters.size() == testParameters.size()) {
         for (final String key : requestParameters.keySet()) {
            final String requestValue = requestParameters.get(key);
            final String testValue = testParameters.get(key);
            if (testValue == null) {
               acceptable = false;
               LOG.warn("Found unacceptable diff: Querystring key {} is missing in test", key);
            } else if (requestValue == null) {
               acceptable = false;
               LOG.warn("Found unacceptable diff: Querystring key {} is missing in request", key);
            } else {
               final Document requestDocument = createXMLDocument(key, requestValue);
               final Document testDocument = createXMLDocument(key, testValue);
               if (requestDocument != null && testDocument != null) {
                  acceptable = acceptableQueryDocument(key, testDocument, requestDocument);
               } else {
                  if (!requestValue.equals(testValue)) {
                     if (SimulatorUtils.isBackReference(testValue)) {
                        LOG.debug("Found back reference diff: ", key);
                     } else {
                        acceptable = false;
                        LOG.warn("Found unacceptable diff: Querystring key {} does not have matching values {} != {}", new String[] { key, requestValue,
                              testValue });
                     }
                  }
               }
            }
         }
      } else {
         LOG.warn("Found unacceptable diff: Number of querystring parameters is not matching {} : {}", requestParameters.size(), testParameters.size());
         acceptable = false;
      }

      return acceptable;
   }

   protected boolean canBeXMLCompared(final String key, final String requestValue, final String testValue) {
      boolean bIsXml = false;
      try {
         XMLUnit.buildControlDocument(requestValue);
         XMLUnit.buildControlDocument(testValue);
         bIsXml = true;
      } catch (final SAXException e) {
         LOG.debug("Querystring key {} values are not xml. Request: {} test: {}. SAXException: {}.",
               new String[] { key, requestValue, testValue, e.getMessage() });
      } catch (final IOException e) {
         LOG.debug("Querystring key {} values are not xml. Request: {} test: {}. IOException: {}.",
               new String[] { key, requestValue, testValue, e.getMessage() });
      }
      return bIsXml;
   }

   protected Document createXMLDocument(final String key, final String possibleXML) {
      Document document = null;
      try {
         document = XMLUnit.buildControlDocument(possibleXML);
      } catch (final SAXException e) {
         LOG.debug("Querystring key {} value is not xml. Document: {} SAXException: {}.", new Object[] { key, possibleXML, e.getMessage() });
      } catch (final IOException e) {
         LOG.debug("Querystring key {} value is not xml. Document: {} IOException: {}.", new Object[] { key, possibleXML, e.getMessage() });
      }
      return document;
   }

   protected Document createXMLDocument(final String possibleXML) {
      Document document = null;
      try {
         document = XMLUnit.buildControlDocument(possibleXML);
      } catch (final SAXException e) {
         LOG.debug("Could not convert data to XML - data: {} SAXException: {}.", new Object[] { possibleXML, e.getMessage() });
      } catch (final IOException e) {
         LOG.debug("Could not convert data to XML - data: {} IOException: {}.", new Object[] { possibleXML, e.getMessage() });
      }
      return document;
   }

   protected boolean acceptableQueryXmlString(final String key, final String setupValue, final String testValue) {
      boolean bAcceptable;
      try {
         final Document setupDocument = XMLUnit.buildControlDocument(setupValue);
         final Document testDocument = XMLUnit.buildControlDocument(testValue);
         bAcceptable = acceptableQueryDocument(key, setupDocument, testDocument);

      } catch (final SAXException e) {
         bAcceptable = false;
      } catch (final IOException e) {
         bAcceptable = false;
      }
      return bAcceptable;
   }

   protected boolean acceptableQueryDocument(final String key, final Document setupDocument, final Document testDocument) {
      boolean bAcceptable = true;
      final List<BackReferenceValue> backReferences = SimulatorUtils.doesXMLDocumentsMatch(setupDocument, testDocument);

      if (backReferences == null) {
         LOG.warn("Found unacceptable diff: Querystring key {} contained xml: {} which did not match the setup data: {}", new Object[] { key, setupDocument,
               testDocument });
         bAcceptable = false;
      }
      return bAcceptable;
   }

   private Document createResponseDocument(final RestTestData testdata, final Map<String, String> queryStringParameters, final String path) {
      final Document outTestDocument = SimulatorUtils.copyDocument(testdata.getOutControlDocument());

      if (outTestDocument != null && !testdata.getQueryStringBackReferences().isEmpty() && !testdata.getOutBackReferences().isEmpty()) {
         final List<BackReferenceValue> backReferenceValues = getBackReferenceValues(queryStringParameters, testdata.getQueryStringBackReferences());

         SimulatorUtils.replaceBackReferences(outTestDocument, testdata.getOutBackReferences(), backReferenceValues, testdata.getOutNameSpaces());
      }

      return outTestDocument;
   }

   private List<BackReferenceValue> getBackReferenceValues(final Map<String, String> queryStringParameters,
         final List<BackReferenceLocation> queryStringBackReferences) {
      final List<BackReferenceValue> list = new ArrayList<BackReferenceValue>();
      for (final BackReferenceLocation backReferenceLocation : queryStringBackReferences) {
         final String id = backReferenceLocation.getId();
         final String value = queryStringParameters.get(backReferenceLocation.getLocation());
         if (value != null) {
            list.add(new BackReferenceValue(id, value));
         }
      }
      return list;
   }

   private String createResponseText(final RestTestData testdata, final Map<String, String> queryStringParameters, final String path) {
      String text = testdata.getText();
      if (text != null) {
         final List<BackReferenceValue> backReferenceValues = getBackReferenceValues(queryStringParameters, testdata.getQueryStringBackReferences());

         text = replaceBackReferences(text, backReferenceValues);
      }
      return text;
   }

   private String replaceBackReferences(final String startingText, final List<BackReferenceValue> backReferenceValues) {
      final StringBuilder textBuilder = new StringBuilder(startingText);
      for (final BackReferenceValue backReferenceValue : backReferenceValues) {
         final String id = backReferenceValue.getId();
         int pos;
         final String value = backReferenceValue.getValue();
         while ((pos = textBuilder.indexOf(id)) >= 0) {
            textBuilder.delete(pos, pos + id.length());
            textBuilder.insert(pos, value);
         }
         LOG.debug("Replacing back reference : {} <=> {}", id, value);
      }
      return textBuilder.toString();
   }

   public Integer verifyTest(final String testId) {
      if (testId != null) {
         for (final RestTestData testData : testDatas) {
            if (testId.equals(testData.getTestId())) {
               LOG.info("Verified REST test with testId: " + testId);
               return testData.getCallCount();
            }
         }
      }
      LOG.info("Failed to verify REST test with testId: " + testId);
      return null;
   }

   private String createAndStoreTest(final SimulatorRequest request) throws MissingParameterException {
      final String testId = request.getParameter(SimulatorUtils.TEST_ID);
      if (testId != null) {
         final RestTestData testData = new RestTestData();
         testData.setTestId(testId);
         Map<String, String> queryStringParameters = SimulatorUtils.readQueryStringParameters(trim(request.getParameter(QUERYSTRING)));
         // JBoss fix below!
         if (isEmpty(queryStringParameters)) {
            final Map<String, String> allParameters = RestSimulatorUtils.getQueryStringParametersFromRequest(request.getParameters());
            final String queryString = allParameters.get(QUERYSTRING);
            final String trimedQueryString = trim(queryString);
            queryStringParameters = SimulatorUtils.readQueryStringParameters(trimedQueryString);
         }
         testData.setQueryStringParameters(queryStringParameters);
         testData.setPath(trim(request.getParameter(PATH)));
         String outData = request.getParameter(OUT_DATA);
         if (outData == null) {
            outData = "";
         }
         boolean wasXML = true;
         try {
            if (outData.startsWith("<")) {
               testData.setOutControlDocument(XMLUnit.buildControlDocument(outData));

            } else {
               wasXML = false;
            }
         } catch (final SAXException e) {
            LOG.debug("Failed to parse xml from response. Interpreting response as text", e);
            wasXML = false;

         } catch (final IOException e) {
            LOG.debug("Failed to read xml from response. Interpreting response as text", e);
            wasXML = false;
         }
         if (!wasXML) {
            testData.setOutText(outData);
         }

         final String[] responseHeaders = request.getParameterValues(RESPONSE_HEADERS);
         testData.setResponseHeaders(SimulatorUtils.readHeaders(responseHeaders));

         final List<BackReferenceLocation> queryStringBackRefs = SimulatorUtils.scanForBackReferences(testData.getQueryStringParameters());
         testData.setQueryStringBackReferences(queryStringBackRefs);

         if (testData.getOutControlDocument() != null) {
            final String[] outBR = request.getParameterValues(OUT_BACK_REFERENCES);
            final boolean scanBR = request.getBooleanParameter(SCAN_FOR_BACK_REFERENCES);

            if (!scanBR && outBR != null) {
               testData.setOutBackReferences(SimulatorUtils.createBackReferences(outBR, testData.getOutControlDocument()));
            } else {
               final List<BackReferenceLocation> outBackRefs = new ArrayList<BackReferenceLocation>();
               final Map<String, String> outNameSpaces = new HashMap<String, String>();
               SimulatorUtils.scanForBackReferences(testData.getOutControlDocument(), outBackRefs, outNameSpaces);
               testData.setOutBackReferences(outBackRefs);
               testData.setOutNameSpaces(outNameSpaces);
            }
         }

         testData.setDelay(request.getIntegerParameter(DELAY));
         testDatas.add(testData);
         return testId;
      } else {
         throw new MissingParameterException("Missing parameter: " + SimulatorUtils.TEST_ID);
      }
   }

   private String readQueryStringFromRequest(final SimulatorRequest request) throws SimulatorException {
      String queryString = request.getQueryString();
      if (queryString == null) {
         final InputStream inputStream = request.getInputStream();
         final String builtQueryString = RestSimulatorUtils.buildQueryStringFromInputStream(inputStream);
         queryString = builtQueryString;
      }
      return trim(queryString);
   }

   private String trim(final String str) {
      if (str != null) {
         return str.trim();
      } else {
         return "";
      }
   }

   public void removeTest(final String testId) {
      if (testId != null) {
         final List<RestTestData> toBeRemoved = new ArrayList<RestTestData>();
         for (final RestTestData testData : testDatas) {
            if (testId.equals(testData.getTestId())) {
               toBeRemoved.add(testData);
            }
         }
         testDatas.removeAll(toBeRemoved);
      }
   }
}
