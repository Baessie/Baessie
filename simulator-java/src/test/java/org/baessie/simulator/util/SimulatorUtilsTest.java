package org.baessie.simulator.util;

import java.util.Map;

import org.baessie.simulator.util.SimulatorUtils;
import org.junit.Assert;
import org.junit.Test;

public class SimulatorUtilsTest {

   @Test
   public void readHeaders() {
      final String key1 = "content-type";
      final String value1 = "text/xml;charset=\"utf-8\"";
      final String key2 = "connection";
      final String value2 = "Keep-Alive";
      final String key3 = "Date";
      final String value3 = "Mon, 14 Feb 2011 11:47:58 GMT";
      final String key4 = "Keep-Alive";
      final String value4 = "timeout=5, max=100";

      final String[] strs = new String[] { key1 + "=" + value1, key2 + "=" + value2, key3 + "=" + value3, key4 + "=" + value4 };
      final Map<String, String> headers = SimulatorUtils.readHeaders(strs);

      Assert.assertEquals("kvp 1", value1, headers.get(key1));
      Assert.assertEquals("kvp 2", value2, headers.get(key2));
      Assert.assertEquals("kvp 3", value3, headers.get(key3));
      Assert.assertEquals("kvp 4", value4, headers.get(key4));
   }

}
