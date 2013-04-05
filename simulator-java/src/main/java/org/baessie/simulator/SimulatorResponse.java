package org.baessie.simulator;

import java.util.Map;

import org.w3c.dom.Document;

public class SimulatorResponse {

	private Map<String, String> headers;
	private Document document;
	private String text;

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(final Map<String, String> headers) {
		this.headers = headers;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(final Document document) {
		this.document = document;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}
}
