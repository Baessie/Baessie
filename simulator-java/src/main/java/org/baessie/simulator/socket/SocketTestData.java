package org.baessie.simulator.socket;

public class SocketTestData {
	private final String id;
	private final String request;
	private final String response;
	private final Integer maxCallCount;
	private final boolean closeAfterResponse;

	public SocketTestData(final String id, final String request, final String response, final Integer maxCallCount, final boolean closeAfterResponse) {
		this.id = id;
		this.request = request;
		this.response = response;
		this.maxCallCount = maxCallCount;
		this.closeAfterResponse = closeAfterResponse;
	}

	protected boolean matches(final SocketTestData sought) {
		boolean matches = false;
		if (sought != null) {
			matches = sought.request.equals(request);
		}

		return matches;
	}

	public String getResponse() {
		return response;
	}

	public boolean isCloseAfterResponse() {
		return closeAfterResponse;
	}

	public String getId() {
		return id;
	}

	public boolean hasReachedMaximumCallCount(final int currentCallCount) {
		if (maxCallCount == null) {
			return false;
		}
		return maxCallCount <= currentCallCount;
	}
}
