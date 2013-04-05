package org.baessie.simulator.socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.baessie.simulator.SimulatorRequest;
import org.baessie.simulator.mock.HttpServletRequestMock;
import org.baessie.simulator.socket.SocketSimulator;
import org.junit.After;
import org.junit.Test;

public class SocketSimulatorTest {
   private final SocketSimulator simulator = new SocketSimulator(65123);

   @After
   public void tearDown() {
      simulator.terminate();
   }

   @Test
   public void simpleCallIsHandledCorrectly() throws Exception {
      String requestString = "GET subscriber?identity=0001&phonenumber=040-123456";
      String expectedResponse = "identity=0001&status=00";
      SimulatorRequest request = new SimulatorRequest();
      request.addParameter("testId", "1");
      request.addParameter("request", requestString);
      request.addParameter("response", expectedResponse);
      simulator.setupTest(request);

      SocketClient socketClient = generateSocketClient();

      socketClient.write(requestString);
      String response = socketClient.read();
      assertEquals("response", expectedResponse, response);
   }

   @Test
   public void socketIsClosedIfCloseAfterResponseIsTrue() throws Exception {
      SimulatorRequest request = new SimulatorRequest();
      request.addParameter("testId", "1");
      String requestString = "GET subscriber?identity=0001&phonenumber=040-123456";
      request.addParameter("request", requestString);
      String expectedResponse = "identity=0001&status=00";
      request.addParameter("response", expectedResponse);
      request.addParameter("closeAfterResponse", "true");
      simulator.setupTest(request);

      SocketClient socketClient = generateSocketClient();

      socketClient.write(requestString);
      socketClient.read();

      Thread.sleep(100);

      assertTrue("socket closed", socketClient.isSocketClosed());
   }

   @Test
   public void endOfFileClosesSocket() throws Exception {
      SimulatorRequest request = new SimulatorRequest();
      request.addParameter("testId", "1");
      String requestString = "GET subscriber?identity=0001&phonenumber=040-123456";
      request.addParameter("request", requestString);
      String expectedResponse = "identity=0001&status=00";
      request.addParameter("response", expectedResponse);
      simulator.setupTest(request);

      SocketClient socketClient = generateSocketClient();

      socketClient.write(requestString);
      socketClient.shutdownOutput();
      socketClient.read();

      Thread.sleep(100);

      assertTrue("socket closed", socketClient.isSocketClosed());
   }

   private SocketClient generateSocketClient() throws Exception {
      return new SocketClient(simulator.getPort());
   }

   @Test
   public void multipleCallsAreHandledCorrectly() throws Exception {
      SimulatorRequest request = new SimulatorRequest();
      request.addParameter("testId", "1");
      String requestString = "LOGIN";
      request.addParameter("request", requestString);
      String expectedResponse = "GNURRF";
      request.addParameter("response", expectedResponse);
      simulator.setupTest(request);

      SimulatorRequest request2 = new SimulatorRequest();
      request2.addParameter("testId", "2");
      String requestString2 = "GET subscriber?identity=0001&phonenumber=040-123456";
      request2.addParameter("request", requestString2);
      String expectedResponse2 = "identity=0001&status=00";
      request2.addParameter("response", expectedResponse2);
      simulator.setupTest(request2);

      SocketClient socketClient = generateSocketClient();

      socketClient.write(requestString);
      String response = socketClient.read();
      assertEquals("response", expectedResponse, response);

      socketClient.write(requestString2);
      String response2 = socketClient.read();
      assertEquals("response2", expectedResponse2, response2);
   }

   @Test
   public void spindelCallsAreHandledCorrectly() throws Exception {
      SimulatorRequest request = new SimulatorRequest();
      request.addParameter("testId", "1");
      String requestString = "LOGIN";
      request.addParameter("request", requestString);
      String expectedResponse = "OPEN";
      request.addParameter("response", expectedResponse);
      simulator.setupTest(request);

      SimulatorRequest request2 = new SimulatorRequest();
      request2.addParameter("testId", "2");
      String requestString2 = "GET subscriber?identity=0001&phonenumber=040-123456";
      request2.addParameter("request", requestString2);
      String expectedResponse2 = "identity=0001&status=00";
      request2.addParameter("response", expectedResponse2);
      simulator.setupTest(request2);

      SocketClient socketClient = generateSocketClient();

      socketClient.write(requestString);
      String response = socketClient.read();
      assertEquals("response", expectedResponse, response);

      socketClient.write(requestString2);
      String response2 = socketClient.read();
      assertEquals("response2", expectedResponse2, response2);
   }

   @Test(expected = IllegalStateException.class)
   public void setupWithoutTestidThrowsIllegalStateException() {
      SimulatorRequest request = new SimulatorRequest();
      request.addParameter("request", "LOGIN");
      request.addParameter("response", "OPEN");
      simulator.setupTest(request);
   }

   @Test(expected = IllegalStateException.class)
   public void setupWithoutRequestThrowsIllegalStateException() {
      SimulatorRequest request = new SimulatorRequest();
      request.addParameter("testId", "1");
      request.addParameter("response", "OPEN");
      simulator.setupTest(request);
   }

   @Test(expected = IllegalStateException.class)
   public void setupWithoutResponseThrowsIllegalStateException() {
      SimulatorRequest request = new SimulatorRequest();
      request.addParameter("testId", "1");
      request.addParameter("request", "LOGIN");
      simulator.setupTest(request);
   }

   private static class SocketClient {
      private Socket socket;
      private DataOutputStream dataOutputStream;
      private DataInputStream dataInputStream;

      public SocketClient(int port) throws Exception {
         socket = new Socket("localhost", port);
         socket.setSoTimeout(300000);
         dataOutputStream = new DataOutputStream(socket.getOutputStream());
         dataInputStream = new DataInputStream(socket.getInputStream());
      }

      public void shutdownOutput() throws IOException {
         socket.shutdownOutput();
      }

      public boolean isSocketClosed() throws IOException {
         return read() == null;
      }

      public String read() throws IOException {
         byte[] buff = new byte[1024];
         int readBytes = dataInputStream.read(buff);
         if (readBytes >= 0) {
            return new String(buff, 0, readBytes);
         }
         return null;
      }

      public void write(String requestString) throws IOException {
         dataOutputStream.write(requestString.getBytes());
         dataOutputStream.flush();
      }
   }
}
