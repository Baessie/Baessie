package org.baessie.simulator.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.baessie.simulator.SimulatorException;
import org.baessie.simulator.SimulatorRequest;
import org.baessie.simulator.SimulatorResponse;
import org.baessie.simulator.util.SimulatorUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class RestSimulatorTest {

	private RestSimulator simulator;

	@Before
	public void setup() {
		simulator = new RestSimulator();
	}

	@Test(expected = SimulatorException.class)
	public void executeTestWithoutMatch() throws Exception {
		simulator.executeTest(new SimulatorRequest());
	}

	@Test
	public void clearTestsWithoutAnyTests() throws Exception {
		assertEquals(0, simulator.clearTests());
	}

	@Test
	public void clearTestsWithOneTests() throws Exception {
		final SimulatorRequest simulatorRequest = generateValidRequest();
		assertNotNull(simulator.setupTest(simulatorRequest));
		assertEquals(1, simulator.clearTests());
	}

	@Test
	public void setupTestWithoutId() throws Exception {
		final SimulatorRequest simulatorRequest = new SimulatorRequest();
		assertNull(simulator.setupTest(simulatorRequest));
	}

	@Test
	public void verifyTestWithNullNoPossibleMatch() throws Exception {
		assertNull(simulator.verifyTest(null));
	}

	@Test
	public void verifyTestWithEmptyStringNoPossibleMatch() throws Exception {
		assertNull(simulator.verifyTest(""));
	}

	private SimulatorRequest generateValidRequest() {
		final SimulatorRequest request = new SimulatorRequest();
		request.addParameter(SimulatorUtils.TEST_ID, "SPEX");
		return request;
	}

	@Test
	public void executeTestMinimal() throws Exception {
		final String testQueryString = "";
		final String testPath = "/";
		final String realQueryString = "";
		final String realPath = "/";
		final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><response/>";

		// SETUP

		SimulatorRequest request = new SimulatorRequest();
		request.addParameter(SimulatorUtils.TEST_ID, "MINIMAL");
		request.addParameter(RestSimulator.PATH, testPath);
		request.addParameter(RestSimulator.QUERYSTRING, testQueryString);
		request.addParameter(RestSimulator.OUT_DATA, "<response/>");

		String output = simulator.setupTest(request);

		assertEquals("Added test", "MINIMAL", output);

		// EXECUTE

		request = new SimulatorRequest();
		request.setPath(realPath);
		final String charset = "UTF-8";
		request.setInputStream(createInputStream(realQueryString, charset));
		request.setCharset(charset);

		final SimulatorResponse response = simulator.executeTest(request);
		output = createResponseString(response);

		// System.out.println(output);

		assertEquals("Response", expectedResponse, output);

		// VERIFY

		final Integer result = simulator.verifyTest("MINIMAL");

		assertEquals("Test called once", Integer.valueOf(1), result);
	}

	@Test
	public void executeTestTextResponse() throws Exception {
		final String testQueryString = "br=*(TEST-1)*";
		final String testPath = "/";
		final String realQueryString = "br=1234";
		final String realPath = "/";
		final String testResponse = "TEXT *(TEST-1)*";
		final String expectedResponse = "TEXT 1234";

		// SETUP

		SimulatorRequest request = new SimulatorRequest();
		request.addParameter(SimulatorUtils.TEST_ID, "TEXT RESPONSE");
		request.addParameter(RestSimulator.PATH, testPath);
		request.addParameter(RestSimulator.QUERYSTRING, testQueryString);
		request.addParameter(RestSimulator.OUT_DATA, testResponse);

		String output = simulator.setupTest(request);

		assertEquals("Added test", "TEXT RESPONSE", output);

		// EXECUTE

		request = new SimulatorRequest();
		request.setPath(realPath);
		final String charset = "UTF-8";
		request.setInputStream(createInputStream(realQueryString, charset));
		request.setCharset(charset);

		final SimulatorResponse response = simulator.executeTest(request);
		output = createResponseString(response);

		// System.out.println(output);

		assertEquals("Response", expectedResponse, output);

		// VERIFY

		final Integer result = simulator.verifyTest("TEXT RESPONSE");

		assertEquals("Test called once", Integer.valueOf(1), result);
	}

	@Test
	public void executeTestNullResponseBecomesEmptyString() throws Exception {
		final String testQueryString = "br=1234";
		final String testPath = "/";
		final String realQueryString = "br=1234";
		final String realPath = "/";
		final String testResponse = null;
		final String expectedResponse = "";

		// SETUP

		SimulatorRequest request = new SimulatorRequest();
		request.addParameter(SimulatorUtils.TEST_ID, "TEXT RESPONSE");
		request.addParameter(RestSimulator.PATH, testPath);
		request.addParameter(RestSimulator.QUERYSTRING, testQueryString);
		request.addParameter(RestSimulator.OUT_DATA, testResponse);

		String output = simulator.setupTest(request);

		assertEquals("Added test", "TEXT RESPONSE", output);

		// EXECUTE

		request = new SimulatorRequest();
		request.setPath(realPath);
		final String charset = "UTF-8";
		request.setInputStream(createInputStream(realQueryString, charset));
		request.setCharset(charset);

		final SimulatorResponse response = simulator.executeTest(request);
		output = createResponseString(response);

		// System.out.println(output);

		assertEquals("Response", expectedResponse, output);

		// VERIFY

		final Integer result = simulator.verifyTest("TEXT RESPONSE");

		assertEquals("Test called once", Integer.valueOf(1), result);
	}

	@Test
	public void executeTestXMLResponseWithScan() throws Exception {
		final String testQueryString = "br=*(TEST-1)*";
		final String testPath = "/";
		final String realQueryString = "br=1234";
		final String realPath = "/";
		final String testResponse = "<response>*(TEST-1)*</response>";
		final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><response>1234</response>";

		// SETUP

		SimulatorRequest request = new SimulatorRequest();
		request.addParameter(SimulatorUtils.TEST_ID, "XML RESPONSE");
		request.addParameter(RestSimulator.PATH, testPath);
		request.addParameter(RestSimulator.QUERYSTRING, testQueryString);
		request.addParameter(RestSimulator.OUT_DATA, testResponse);

		String output = simulator.setupTest(request);

		assertEquals("Added test", "XML RESPONSE", output);

		// EXECUTE

		request = new SimulatorRequest();
		request.setPath(realPath);
		final String charset = "UTF-8";
		request.setInputStream(createInputStream(realQueryString, charset));
		request.setCharset(charset);

		final SimulatorResponse response = simulator.executeTest(request);
		output = createResponseString(response);

		// System.out.println(output);

		assertEquals("Response", expectedResponse, output);

		// VERIFY

		final Integer result = simulator.verifyTest("XML RESPONSE");

		assertEquals("Test called once", Integer.valueOf(1), result);
	}

	@Test
	public void executeTestXMLResponseWithoutScan() throws Exception {
		final String testQueryString = "br=*(TEST-1)*";
		final String testPath = "/";
		final String realQueryString = "br=1234";
		final String realPath = "/";
		final String testResponse = "<ns:response xmlns:ns=\"urn:x\">*(TEST-1)*</ns:response>";
		final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns:response xmlns:ns=\"urn:x\">1234</ns:response>";

		// SETUP

		SimulatorRequest request = new SimulatorRequest();
		request.addParameter(SimulatorUtils.TEST_ID, "XML RESPONSE");
		request.addParameter(RestSimulator.PATH, testPath);
		request.addParameter(RestSimulator.QUERYSTRING, testQueryString);
		request.addParameter(RestSimulator.OUT_DATA, testResponse);
		request.addParameter(RestSimulator.OUT_BACK_REFERENCES, "/ns:response[1]/text()[1]");
		request.addParameter(RestSimulator.SCAN_FOR_BACK_REFERENCES, "false");

		String output = simulator.setupTest(request);

		assertEquals("Added test", "XML RESPONSE", output);

		// EXECUTE

		request = new SimulatorRequest();
		request.setPath(realPath);
		final String charset = "UTF-8";
		request.setInputStream(createInputStream(realQueryString, charset));
		request.setCharset(charset);

		final SimulatorResponse response = simulator.executeTest(request);
		output = createResponseString(response);

		// System.out.println(output);

		assertEquals("Response", expectedResponse, output);

		// VERIFY

		final Integer result = simulator.verifyTest("XML RESPONSE");

		assertEquals("Test called once", Integer.valueOf(1), result);
	}

	@Test
	public void isXmlStringLegalXmlString() {
		// SETUP
		final String inputText = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><username>pelle</username><password>pelle</password></userid></request>";
		final RestSimulatorEx simulator = new RestSimulatorEx();
		// EXECUTE
		final boolean result = simulator.canBeXMLCompared("simulator", inputText, inputText);
		// VERIFY
		assertTrue(result);
	}

	@Test
	public void isXmlStringNonXmlString() {
		// SETUP
		final String inputText = "REST-1";
		final RestSimulatorEx simulator = new RestSimulatorEx();
		// EXECUTE
		final boolean result = simulator.canBeXMLCompared("simulator", inputText, inputText);
		// VERIFY
		assertFalse(result);
	}

	@Test
	public void isXmlStringIllegalXmlString() {
		// SETUP
		final String inputText = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><username>pelle</username><password>pelle</userid></request>";
		final RestSimulatorEx simulator = new RestSimulatorEx();
		// EXECUTE
		final boolean result = simulator.canBeXMLCompared("simulator", inputText, inputText);
		// VERIFY
		assertFalse(result);
	}

	@Test
	public void acceptableQueryXmlStringExactEqualXmlStrings() {
		// SETUP
		final String requestXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><username>pelle</username><password>pelle</password></userid></request>";
		final String testXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><username>pelle</username><password>pelle</password></userid></request>";
		final RestSimulatorEx simulator = new RestSimulatorEx();
		// EXECUTE
		final boolean result = simulator.acceptableQueryXmlString("simulator", requestXml, testXml);
		// VERIFY
		assertTrue(result);
	}

	@Test
	public void acceptableQueryXmlStringSimilarXmlStrings() {
		// SETUP
		final String requestXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><username>pelle</username><password>pelle</password></userid></request>";
		final String testXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><password>pelle</password><username>pelle</username></userid></request>";
		final RestSimulatorEx simulator = new RestSimulatorEx();
		// EXECUTE
		final boolean result = simulator.acceptableQueryXmlString("simulator", requestXml, testXml);
		// VERIFY
		assertTrue(result);
	}

	@Test
	public void acceptableQueryDocumentShouldDetectDifference() {
		// SETUP
		final String requestXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><username>pelle</username><password>pelle</password></userid></request>";
		final String testXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><username>kalle</username><password>tjohoo</password></userid></request>";
		final RestSimulatorEx simulator = new RestSimulatorEx();
		// EXECUTE
		final boolean result = simulator.acceptableQueryDocument("simulator", simulator.createXMLDocument(requestXml), simulator.createXMLDocument(testXml));
		// VERIFY
		assertFalse(result);
	}

	@Test
	public void executeTestQueryStringWithWildcardFromSIMTestThatShouldWork() throws SimulatorException {
		// SETUP
		final RestSimulatorEx simulator = new RestSimulatorEx();
		final String setupData = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><username>*</username><password>*</password></userid><query>FIND</query><response>verify.dtd</response><action>ADD</action><id>43206476111</id><phonenumber>020960853</phonenumber><channel>TS</channel><period>30</period><security>4107102958</security><identity></identity></request>";
		final String requestData = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><request><userid><username>simulator</username><password>password</password></userid><query>FIND</query><response>verify.dtd</response><phonenumber>020960853</phonenumber><action>ADD</action><channel>TS</channel><id>43206476111</id><security>4107102958</security><identity></identity><period>30</period></request>";
		final String parameterName = "MfwRequest";
		simulator.setupTest(createSetupRequestWithParameterValue(parameterName, setupData));
		assertNotNull(simulator.executeTest(createRequestWithParameterValue(parameterName, requestData)));
	}

	private SimulatorRequest createSetupRequestWithParameterValue(final String parameterName, final String setupData) {
		final SimulatorRequest request = new SimulatorRequest();
		request.addParameter("testId", "gurka");
		request.addParameter("queryString", parameterName + "=" + setupData);
		return request;
	}

	private SimulatorRequest createRequestWithParameterValue(final String parameterName, final String setupData) {
		final SimulatorRequest request = new SimulatorRequest();
		request.addParameter("testId", "gurka");
		request.setQueryString(parameterName + "=" + setupData);
		return request;
	}

	private InputStream createInputStream(final String str, final String charset) {
		try {
			return new BufferedInputStream(new ByteArrayInputStream(str.getBytes(charset)));
		} catch (final Exception e) {
			return new BufferedInputStream(new ByteArrayInputStream("".getBytes()));
		}
	}

	private String createResponseString(final SimulatorResponse response) throws Exception {
		ByteArrayOutputStream out = null;
		final Document document = response.getDocument();
		if (document != null) {
			try {
				out = new ByteArrayOutputStream();
				final DOMSource source = new DOMSource(document);
				final StreamResult result = new StreamResult(out);
				SimulatorUtils.newTransformer().transform(source, result);
				out.flush();
				String charset = document.getXmlEncoding();
				if (charset == null) {
					charset = "UTF-8";
				}
				return out.toString(charset);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (final IOException e) {
					}
				}
			}
		} else {
			return response.getText();
		}
	}

	class RestSimulatorEx extends RestSimulator {

	}

}
