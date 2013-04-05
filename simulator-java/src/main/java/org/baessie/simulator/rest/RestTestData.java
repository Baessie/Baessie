package org.baessie.simulator.rest;

import java.util.List;
import java.util.Map;

import org.baessie.simulator.ws.BackReferenceLocation;
import org.w3c.dom.Document;

public class RestTestData {

	private String testId;
	private Map<String, String> queryStringParameters;
	private String path;
	private Document outControlDocument;
	List<BackReferenceLocation> queryStringBackReferences;
	private List<BackReferenceLocation> outBackReferences;
	private Integer delay;
	private int callCount;
	private Map<String, String> responseHeaders;

	private Map<String, String> outNameSpaces;
	private String text;

	public String getTestId() {
		return testId;
	}

	public void setTestId(final String testId) {
		this.testId = testId;
	}

	public Map<String, String> getQueryStringParameters() {
		return queryStringParameters;
	}

	public void setQueryStringParameters(final Map<String, String> queryStringParameters) {
		this.queryStringParameters = queryStringParameters;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public Document getOutControlDocument() {
		return outControlDocument;
	}

	public void setOutControlDocument(final Document outControlDocument) {
		this.outControlDocument = outControlDocument;
	}

	public List<BackReferenceLocation> getOutBackReferences() {
		return outBackReferences;
	}

	public void setOutBackReferences(final List<BackReferenceLocation> outBackReferences) {
		this.outBackReferences = outBackReferences;
	}

	public Integer getDelay() {
		return delay;
	}

	public void setDelay(final Integer delay) {
		this.delay = delay;
	}

	public void incCallCount() {
		callCount++;
	}

	public int getCallCount() {
		return callCount;
	}

	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(final Map<String, String> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public Map<String, String> getOutNameSpaces() {
		return outNameSpaces;
	}

	public void setOutNameSpaces(final Map<String, String> outNameSpaces) {
		this.outNameSpaces = outNameSpaces;
	}

	public String getText() {
		return text;
	}

	public void setOutText(final String text) {
		this.text = text;
	}

	public List<BackReferenceLocation> getQueryStringBackReferences() {
		return queryStringBackReferences;
	}

	public void setQueryStringBackReferences(final List<BackReferenceLocation> queryStringBackReferences) {
		this.queryStringBackReferences = queryStringBackReferences;
	}

}
