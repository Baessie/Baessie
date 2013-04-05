package org.baessie.simulator.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.baessie.simulator.SimulatorException;
import org.baessie.simulator.util.SimulatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RestSimulatorUtils {

	private static final Logger LOG = LoggerFactory.getLogger(RestSimulatorUtils.class);

	private RestSimulatorUtils() {

	}

	public static Map<String, String> getQueryStringParametersFromRequest(final Map<String, String[]> mapParameters) {

		final Map<String, String> parameters = new HashMap<String, String>();
		if (mapParameters != null && !mapParameters.isEmpty()) {
			for (final Map.Entry<String, String[]> entry : mapParameters.entrySet()) {
				String key = entry.getKey();
				key = SimulatorUtils.decode(key);
				final String[] values = entry.getValue();
				String value = null;
				// The following code may cause problems in the future
				if (values != null && values.length > 0) {
					if (SimulatorUtils.shouldDecode(values[0])) {
						value = SimulatorUtils.decode(values[0]);
					} else {
						value = values[0];
					}
					parameters.put(key, value);

				}
			}
		}
		return parameters;
	}

	public static String buildQueryStringFromInputStream(final InputStream inputStream) throws SimulatorException {
		BufferedReader reader = null;
		final StringBuilder sb = new StringBuilder();
		if (inputStream != null) {
			try {
				reader = new BufferedReader(new InputStreamReader(inputStream, SimulatorUtils.CHARSET));
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} catch (final IOException e) {
				LOG.warn("Failed to read query string from REST request", e);
				throw new SimulatorException("Failed to read query string from request", e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (final IOException e) {
						LOG.warn("Failed to close buffered reader/input stream", e);
					}
				}
			}
		}
		final String builtQueryString = sb.toString();
		return builtQueryString;
	}

}
