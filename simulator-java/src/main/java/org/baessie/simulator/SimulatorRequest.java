package org.baessie.simulator;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.baessie.simulator.ws.WSSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorRequest {

	private static final Logger LOG = LoggerFactory.getLogger(WSSimulator.class);

	private String charset;
	private String queryString;
	private String path;
	private final Map<String, String[]> parameters = new HashMap<String, String[]>();
	private InputStream inputStream;

	public String getCharset() {
		return charset;
	}

	public void setCharset(final String charset) {
		this.charset = charset;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(final String queryString) {
		this.queryString = queryString;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public Map<String, String[]> getParameters() {
		return parameters;
	}

	public void addParameter(final String name, final String... values) {
		parameters.put(name, values);
	}

	public void addParameters(final Map<String, String[]> parameters) {
		this.parameters.putAll(parameters);
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(final InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getParameter(final String key) {
		String value = null;
		if (parameters != null) {
			final String[] values = parameters.get(key);
			if (values != null && values.length > 0) {
				value = values[0];
			}
		}
		return value;
	}

	public String[] getParameterValues(final String key) {
		String[] values = null;
		if (parameters != null) {
			values = parameters.get(key);
		}
		return values;
	}

	public Integer getIntegerParameter(final String key) {
		Integer value = null;
		try {
			value = Integer.parseInt(getParameter(key));
		} catch (final Exception e) {
			LOG.warn("Could not parse: " + key, e);
		}
		return value;
	}

	public boolean getBooleanParameter(final String key) {
		return "true".equalsIgnoreCase(getParameter(key));
	}

}
