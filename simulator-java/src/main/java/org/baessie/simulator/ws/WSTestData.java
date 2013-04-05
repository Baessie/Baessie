package org.baessie.simulator.ws;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

public class WSTestData {

   private String testId;
   private Document inControlDocument;
   private Document outControlDocument;
   private List<BackReferenceLocation> inBackReferences;
   private List<BackReferenceLocation> outBackReferences;
   private Integer delay;
   private int callCount;
   private Map<String, String> responseHeaders;

   private Map<String, String> inNameSpaces;
   private Map<String, String> outNameSpaces;

   public String getTestId() {
      return testId;
   }

   public void setTestId(final String testId) {
      this.testId = testId;
   }

   public Document getInControlDocument() {
      return inControlDocument;
   }

   public void setInControlDocument(final Document inControlDocument) {
      this.inControlDocument = inControlDocument;
   }

   public Document getOutControlDocument() {
      return outControlDocument;
   }

   public void setOutControlDocument(final Document outControlDocument) {
      this.outControlDocument = outControlDocument;
   }

   public List<BackReferenceLocation> getInBackReferences() {
      return inBackReferences;
   }

   public void setInBackReferences(final List<BackReferenceLocation> inBackReferences) {
      this.inBackReferences = inBackReferences;
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

   public void setResponseHeaders(final Map<String, String> headers) {
      responseHeaders = headers;
   }

   public Map<String, String> getInNameSpaces() {
      return inNameSpaces;
   }

   public void setInNameSpaces(final Map<String, String> inNameSpaces) {
      this.inNameSpaces = inNameSpaces;
   }

   public Map<String, String> getOutNameSpaces() {
      return outNameSpaces;
   }

   public void setOutNameSpaces(final Map<String, String> outNameSpaces) {
      this.outNameSpaces = outNameSpaces;
   }

}
