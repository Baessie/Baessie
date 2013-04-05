package org.baessie.simulator.mock;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServletResponseMock implements HttpServletResponse {

   private static final Logger LOG = LoggerFactory.getLogger(HttpServletResponseMock.class);

   private ServletOutputStreamMock out;
   private Map<String, String> headers = new HashMap<String, String>();

   public HttpServletResponseMock() {
      out = new ServletOutputStreamMock();
   }

   public ServletOutputStreamMock getOutputStream() {
      return out;
   }

   public void setCharacterEncoding(String charset) {
      out.setCharset(charset);
   }

   public void flushBuffer() throws IOException {
   }

   public int getBufferSize() {
      return 0;
   }

   public String getCharacterEncoding() {
      return null;
   }

   public String getContentType() {
      return null;
   }

   public Locale getLocale() {
      return null;
   }

   public PrintWriter getWriter() throws IOException {
      return null;
   }

   public boolean isCommitted() {
      return false;
   }

   public void reset() {
   }

   public void resetBuffer() {
   }

   public void setBufferSize(int arg0) {
   }

   public void setContentLength(int arg0) {
   }

   public void setContentType(String arg0) {
   }

   public void setLocale(Locale arg0) {
   }

   public void addCookie(Cookie arg0) {
   }

   public void addDateHeader(String arg0, long arg1) {
   }

   public void addHeader(String arg0, String arg1) {
      String oldValue = headers.get(arg0);
      if (oldValue != null) {
         LOG.warn("added on already existing header - overwriting " + arg0 + " - old value " + oldValue + " - with " + arg1);
      }
      headers.put(arg0, arg1);
   }

   public void addIntHeader(String arg0, int arg1) {
   }

   public boolean containsHeader(String arg0) {
      return false;
   }

   public String encodeRedirectURL(String arg0) {
      return null;
   }

   public String encodeRedirectUrl(String arg0) {
      return null;
   }

   public String encodeURL(String arg0) {
      return null;
   }

   public String encodeUrl(String arg0) {
      return null;
   }

   public void sendError(int arg0) throws IOException {
   }

   public void sendError(int arg0, String arg1) throws IOException {
   }

   public void sendRedirect(String arg0) throws IOException {
   }

   public void setDateHeader(String arg0, long arg1) {
   }

   public void setHeader(String arg0, String arg1) {
      headers.put(arg0, arg1);
   }

   public void setIntHeader(String arg0, int arg1) {
   }

   public void setStatus(int arg0) {
   }

   public void setStatus(int arg0, String arg1) {
   }

   public Map<String, String> getHeaders() {
      return Collections.unmodifiableMap(headers);
   }

}
