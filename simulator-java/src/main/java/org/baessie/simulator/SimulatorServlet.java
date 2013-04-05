package org.baessie.simulator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.baessie.simulator.rest.RestSimulator;
import org.baessie.simulator.socket.SocketSimulator;
import org.baessie.simulator.util.SimulatorUtils;
import org.baessie.simulator.ws.WSSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class SimulatorServlet extends HttpServlet {

	private static final long serialVersionUID = 767208887906992596L;
	private static final Logger LOG = LoggerFactory.getLogger(SimulatorServlet.class);

	private static final String CLEAR = "clearData";
	private static final String VERIFY = "verifyTest";
	private static final String SOCKET_VERIFY = "socket/verify";
	private static final String WS_SETUP = "ws/setup";
	private static final String SOCKET_SETUP = "socket/setup";
	private static final String REST_SETUP = "servlet/setup";
	private static final String LEGACY_SETUP = "setupTest";

	private final WSSimulator wsSimulator;
	private final RestSimulator restSimulator;
	private final SocketSimulator socketSimulator;

	public SimulatorServlet() {
		try {
			Class.forName(SimulatorUtils.class.getName());
		} catch (final Exception e) {
			LOG.warn("Failed to load SimulatorUtils", e);
		}
		wsSimulator = new WSSimulator();
		restSimulator = new RestSimulator();
		socketSimulator = new SocketSimulator();
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String requestURI = request.getRequestURI();
		logRequest(request);

		ensureCharsetIsSet(request);

		final SimulatorRequest simulatorRequest = createSimulatorRequest(request);

		final long t0 = System.currentTimeMillis();

		if (contains(requestURI, CLEAR)) {
			clearTests(response);
		} else if (contains(requestURI, WS_SETUP)) {
			removeTest(simulatorRequest);
			final String testId = wsSimulator.setupTest(simulatorRequest);
			writeSetupResponse(response, testId);
		} else if (contains(requestURI, SOCKET_SETUP)) {
			removeTest(simulatorRequest);
			final String testId = socketSimulator.setupTest(simulatorRequest);
			writeSetupResponse(response, testId);
		} else if (contains(requestURI, SOCKET_VERIFY)) {
			socketSimulator.verify(simulatorRequest, response);
		} else if (contains(requestURI, LEGACY_SETUP)) {
			removeTest(simulatorRequest);
			final String testId = wsSimulator.setupTest(simulatorRequest);
			writeSetupResponse(response, testId);
		} else if (contains(requestURI, REST_SETUP)) {
			removeTest(simulatorRequest);
			final String testId = restSimulator.setupTest(simulatorRequest);
			writeSetupResponse(response, testId);
		} else if (contains(requestURI, VERIFY)) {
			verifyTest(simulatorRequest, response);
		} else if (request.getContentLength() > 0) {
			if (isXmlRequest(request)) {
				try {
					final SimulatorResponse simulatorResponse = wsSimulator.executeTest(simulatorRequest);
					writeSimulatorResponse(response, simulatorResponse);
				} catch (final SimulatorException e) {
					SimulatorUtils.writeTextResponse(response, e.getMessage());
				}
			} else {
				try {
					final SimulatorResponse simulatorResponse = restSimulator.executeTest(simulatorRequest);
					writeSimulatorResponse(response, simulatorResponse);
				} catch (final SimulatorException e) {
					SimulatorUtils.writeTextResponse(response, e.getMessage());
				}
			}
		} else {
			try {
				final SimulatorResponse simulatorResponse = restSimulator.executeTest(simulatorRequest);
				writeSimulatorResponse(response, simulatorResponse);
			} catch (final SimulatorException e) {
				SimulatorUtils.writeTextResponse(response, e.getMessage());
			}
		}

		final long t1 = System.currentTimeMillis();

		LOG.info("Time to handle call to simulator: " + (t1 - t0) + " ms");
	}

	private void logRequest(final HttpServletRequest request) {
		LOG.debug("Request URI: {}, Content type: {}, Charset: {}, Content length: {}",
				new Object[] { request.getRequestURI(), request.getContentType(), request.getCharacterEncoding(), String.valueOf(request.getContentLength()) });
	}

	private SimulatorRequest createSimulatorRequest(final HttpServletRequest request) throws IOException {
		final SimulatorRequest simulatorRequest = new SimulatorRequest();

		simulatorRequest.setCharset(request.getCharacterEncoding());
		@SuppressWarnings("unchecked")
		final Map<String, String[]> parameterMap = request.getParameterMap();
		simulatorRequest.addParameters(parameterMap);
		simulatorRequest.setInputStream(request.getInputStream());
		simulatorRequest.setQueryString(request.getQueryString());
		simulatorRequest.setPath(request.getRequestURI());

		// DEBUG OUTPUT
		for (final Map.Entry<String, String[]> entry : simulatorRequest.getParameters().entrySet()) {
			final String key = entry.getKey();
			final String[] values = entry.getValue();
			if (values == null || values.length == 0) {
				LOG.debug("createSimulatorRequest. Add parameter: key=" + key + ", value=.");
			} else {
				if (values.length == 1) {
					LOG.debug("createSimulatorRequest. Add parameter: key=" + key + ", value=" + values[0] + ".");
				} else {
					LOG.debug("createSimulatorRequest. Add parameter: key=" + key + ", value=" + values[0] + ", " + values[1] + ".");
				}
			}
		}
		if (simulatorRequest.getParameters().size() == 0) {
			LOG.debug("createSimulatorRequest. No parameters found.");
		}
		return simulatorRequest;
	}

	private void ensureCharsetIsSet(final HttpServletRequest request) {
		String charset = request.getCharacterEncoding();
		if (charset == null) {
			charset = SimulatorUtils.CHARSET;
			try {
				request.setCharacterEncoding(charset);
			} catch (final UnsupportedEncodingException uee) {
				LOG.warn("Failed to set request charset: {}", charset);
			}
		} else {
			try {
				Charset.forName(charset);
			} catch (final Exception e) {
				LOG.warn("Failed to use request charset: {}", charset);
				charset = SimulatorUtils.CHARSET;
				try {
					request.setCharacterEncoding(charset);
				} catch (final UnsupportedEncodingException uee) {
					LOG.warn("Failed to set request charset: {}", charset);
				}
			}
		}
	}

	private void writeSimulatorResponse(final HttpServletResponse response, final SimulatorResponse simulatorResponse) {
		final Document document = simulatorResponse.getDocument();
		final String text = simulatorResponse.getText();
		if (document != null) {
			final Map<String, String> headers = simulatorResponse.getHeaders();
			if (headers != null) {
				for (final Entry<String, String> entry : headers.entrySet()) {
					LOG.info("Writing response header: {}={}", entry.getKey(), entry.getValue());
					try {
						response.setHeader(entry.getKey(), entry.getValue());
					} catch (final Exception e) {
						LOG.warn("Failed to write response header", e);
					}
				}
			}
			SimulatorUtils.writeXmlResponse(response, document);
		} else if (text != null) {
			final Map<String, String> headers = simulatorResponse.getHeaders();
			if (headers != null) {
				for (final Entry<String, String> entry : headers.entrySet()) {
					response.setHeader(entry.getKey(), entry.getValue());
				}
			}
			SimulatorUtils.writeTextResponse(response, text);
		} else {
			SimulatorUtils.writeTextResponse(response, "No response was created when executing test");
		}

	}

	private void writeSetupResponse(final HttpServletResponse response, final String testId) {
		if (testId != null) {
			SimulatorUtils.writeTextResponse(response, "Testdata added: testId=" + testId);
		} else {
			SimulatorUtils.writeTextResponse(response, "Failed to add test");
		}
	}

	private void clearTests(final HttpServletResponse response) {
		int numberOfRemovedTests = 0;
		numberOfRemovedTests += wsSimulator.clearTests();
		numberOfRemovedTests += restSimulator.clearTests();
		SimulatorUtils.writeTextResponse(response, "Testdata cleared: number of entries removed=" + numberOfRemovedTests);
	}

	private void verifyTest(final SimulatorRequest simulatorRequest, final HttpServletResponse response) {
		final String testId = simulatorRequest.getParameter(SimulatorUtils.TEST_ID);
		Integer result = wsSimulator.verifyTest(testId);
		if (result == null) {
			result = restSimulator.verifyTest(testId);
		}
		if (result != null) {
			SimulatorUtils.writeTextResponse(response, "Callcount: " + result.intValue());
		} else {
			SimulatorUtils.writeTextResponse(response, "Failed to find matching testdata for the testId=" + testId);
		}
	}

	private void removeTest(final SimulatorRequest simulatorRequest) {
		final String testId = simulatorRequest.getParameter(SimulatorUtils.TEST_ID);
		wsSimulator.removeTest(testId);
		restSimulator.removeTest(testId);
		socketSimulator.removeTest(testId);
	}

	private boolean isXmlRequest(final HttpServletRequest request) {
		final String contentType = request.getContentType();
		return contentType != null && contentType.contains("xml");
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	private boolean contains(final String string, final String fragment) {
		return string != null && string.contains(fragment);
	}
}
