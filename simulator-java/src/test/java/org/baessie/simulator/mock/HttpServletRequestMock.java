package org.baessie.simulator.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class HttpServletRequestMock implements HttpServletRequest {

	private final String requestURI;
	private final String charset;
	private final String contentType;
	private final ServletInputStreamMock in;
	private final Map<String, String> parameters;

	public HttpServletRequestMock(final String requestURI, final Map<String, String> parameters, final String content, final String charset, final String contentType) {
		this.requestURI = requestURI;
		this.contentType = contentType;
		this.charset = charset;
		in = new ServletInputStreamMock(content, charset);
		Map<String, String> actualParameters = parameters;
		if (actualParameters == null) {
			actualParameters = new HashMap<String, String>();
		}
		this.parameters = actualParameters;

	}

	@Override
	public String getCharacterEncoding() {
		return charset;
	}

	@Override
	public int getContentLength() {
		return in.getLength();
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return in;
	}

	@Override
	public String getParameter(final String key) {
		return parameters.get(key);
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	@Override
	public String[] getParameterValues(final String key) {
		final String values = parameters.get(key);
		if (values != null) {
			return values.split(",");
		} else {
			return null;
		}
	}

	// OTHER

	@Override
	public Object getAttribute(final String arg0) {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		return null;
	}

	@Override
	public String getQueryString() {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getLocales() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Map getParameterMap() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getParameterNames() {
		return null;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Override
	public String getRealPath(final String arg0) {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return null;
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(final String arg0) {
		return null;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public int getServerPort() {
		return 0;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(final String arg0) {
	}

	@Override
	public void setAttribute(final String arg0, final Object arg1) {
	}

	@Override
	public void setCharacterEncoding(final String arg0) throws UnsupportedEncodingException {
	}

	@Override
	public String getAuthType() {
		return null;
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		return new Cookie[0];
	}

	@Override
	public long getDateHeader(final String arg0) {
		return 0;
	}

	@Override
	public String getHeader(final String arg0) {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getHeaderNames() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getHeaders(final String arg0) {
		return null;
	}

	@Override
	public int getIntHeader(final String arg0) {
		return 0;
	}

	@Override
	public String getMethod() {
		return null;
	}

	@Override
	public String getPathInfo() {
		return null;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getServletPath() {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public HttpSession getSession(final boolean arg0) {
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isUserInRole(final String arg0) {
		return false;
	}
}
