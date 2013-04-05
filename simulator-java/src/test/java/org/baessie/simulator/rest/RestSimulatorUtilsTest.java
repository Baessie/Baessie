package org.baessie.simulator.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.baessie.simulator.SimulatorException;
import org.baessie.simulator.rest.RestSimulatorUtils;
import org.junit.Assert;
import org.junit.Test;

public class RestSimulatorUtilsTest {

   @Test
   public void getQueryStringParametersFromRequestWithoutSpecialCharacters() {
      // SETUP
      final Map<String, String[]> inputParameters = new HashMap<String, String[]>();
      inputParameters.put("username", createValue("u1303287911047"));
      inputParameters.put("postal_code", createValue("43496"));
      final Map<String, String> expectedParameters = new HashMap<String, String>();
      expectedParameters.put("username", "u1303287911047");
      expectedParameters.put("postal_code", "43496");

      // EXECUTE
      final Map<String, String> resultParameters = RestSimulatorUtils.getQueryStringParametersFromRequest(inputParameters);

      // VERIFY
      verify(expectedParameters, resultParameters);
   }

   private void verify(final Map<String, String> expectedParameters, final Map<String, String> resultParameters) {
      for (final Map.Entry<String, String> entry : resultParameters.entrySet()) {
         final String key = entry.getKey();
         final String value = entry.getValue();
         Assert.assertEquals(String.format("Verify parameter map  EXPECTED VALUE %s RECEIVED VALUE %s USING KEY %s ", expectedParameters.get(key), value, key),
               expectedParameters.get(key), value);
      }
   }

   private String[] createValue(final String value) {
      final String[] values = new String[1];
      values[0] = value;
      return values;
   }

   @Test
   public void getQueryStringParametersFromRequestWithSpecialParameters() {
      // SETUP
      final Map<String, String[]> inputParameters = new HashMap<String, String[]>();
      inputParameters.put("passw%40rd", createValue("pwd123"));
      inputParameters.put("email", createValue("name%40site.com"));
      final Map<String, String> expectedParameters = new HashMap<String, String>();
      expectedParameters.put("passw@rd", "pwd123");
      expectedParameters.put("email", "name@site.com"); // Mind
      // that
      // at-sign
      // changed
      // to
      // %40

      // EXECUTE
      final Map<String, String> resultParameters = RestSimulatorUtils.getQueryStringParametersFromRequest(inputParameters);

      verify(expectedParameters, resultParameters);
   }

   private byte[] createQueryString() {
      String s = "username=u1303287911047&password=pwd123";
      return s.getBytes(Charset.defaultCharset());
   }

   @Test
   public void buildQueryStringFromInputStream() throws SimulatorException {
      // SETUP
      ByteArrayInputStream stream = new ByteArrayInputStream(createQueryString());
      String expectedQueryString = "username=u1303287911047&password=pwd123";

      // EXECUTE
      String resultQueryString = RestSimulatorUtils.buildQueryStringFromInputStream(stream);
      resultQueryString = resultQueryString.trim();
      Assert.assertEquals(String.format("Verify QueryString  EXPECTED VALUE \'%s\' RECEIVED VALUE \'%s\'.", expectedQueryString, resultQueryString),
            expectedQueryString, resultQueryString);
   }

}
