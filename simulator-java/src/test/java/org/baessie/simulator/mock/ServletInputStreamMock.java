package org.baessie.simulator.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletInputStream;

public class ServletInputStreamMock extends ServletInputStream {

   private ByteArrayInputStream bais;
   private int length;

   public ServletInputStreamMock(String content) {
      this(content, "UTF-8");
   }

   public ServletInputStreamMock(String content, String charset) {
      try {
         byte[] bytes = content.getBytes(charset);
         bais = new ByteArrayInputStream(bytes);
         length = bytes.length;
      } catch (Exception e) {
         bais = new ByteArrayInputStream("".getBytes());
         length = 0;
      }
   }

   @Override
   public int read() throws IOException {
      return bais.read();
   }

   public int getLength() {
      return length;
   }

}
