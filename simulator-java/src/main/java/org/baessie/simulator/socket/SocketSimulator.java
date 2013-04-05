package org.baessie.simulator.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletResponse;

import org.baessie.simulator.SimulatorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketSimulator implements Runnable {
   private static final Logger LOG = LoggerFactory.getLogger(SocketSimulator.class);
   private static final int PORT = 12345;
   private final ServerSocket serverSocket;
   private boolean running = true;
   private final Object monitor = new Object();
   private final Map<String, SocketTestData> data = new ConcurrentHashMap<String, SocketTestData>();
   private final Map<String, AtomicInteger> calledTests = new ConcurrentHashMap<String, AtomicInteger>();
   private Thread serverThread;

   private int port = -1;

   public SocketSimulator(final int port) {
      try {
         this.port = port;
         serverSocket = new ServerSocket(port);
         serverThread = new Thread(this);
         serverThread.start();
      } catch (final IOException e) {
         throw new IllegalStateException(e);
      }
   }

   public SocketSimulator() {
      this(PORT);
   }

   @Override
   public void run() {
      try {
         while (isRunning()) {
            final Socket socket = serverSocket.accept();
            if (isRunning()) {
               new Thread(new SocketHandler(socket, this)).start();
            }
         }
      } catch (final IOException e) {
         e.printStackTrace();
      } finally {
         try {
            serverSocket.close();
         } catch (final IOException e) {
            e.printStackTrace();
         }
      }
   }

   private boolean isRunning() {
      synchronized (monitor) {
         return running;
      }
   }

   public void terminate() {
      synchronized (monitor) {
         running = false;
      }
      try {
         final Socket socket = new Socket(serverSocket.getInetAddress(), serverSocket.getLocalPort());
         socket.close();
         serverThread.join();
      } catch (final IOException e) {
         e.printStackTrace();
      } catch (final InterruptedException e) {
         e.printStackTrace();
      }
   }

   public String setupTest(final SimulatorRequest simulatorRequest) {
      LOG.info("Trying to add testdata");
      final String request = simulatorRequest.getParameter("request");
      String response = simulatorRequest.getParameter("response");
      final String testId = simulatorRequest.getParameter("testId");
      Integer maxCallcount = null;
      if (simulatorRequest.getParameters().containsKey("maxCallcount")) {
         maxCallcount = Integer.parseInt(simulatorRequest.getParameter("maxCallcount"));
      }
      if (testId == null || request == null || response == null) {
         reportNullParam("request", request, "response", response, "testId", testId);
      }
      final boolean closeAfterResponse = Boolean.parseBoolean(simulatorRequest.getParameter("closeAfterResponse"));

      response = response.replaceAll("\\n", "\n");

      final SocketTestData testData = new SocketTestData(testId, request, response, maxCallcount, closeAfterResponse);
      data.put(testId, testData);
      printSetupTestData(testId, request, response);
      calledTests.put(testId, new AtomicInteger(0));
      return testId;
   }

   public SocketTestData findResponseToSendBack(final String request) {
      final SocketTestData sought = new SocketTestData(null, request, null, 0, false);
      final SocketTestData response = matchRequest(sought);
      return response;
   }

   public void verify(final SimulatorRequest simulatorRequest, final HttpServletResponse response) throws IOException {
      final String testId = simulatorRequest.getParameter("testId");
      if (testId != null) {
         final String msg = "Callcount: " + calledTests.get(testId);
         response.getOutputStream().write(msg.getBytes());
      } else {
         throw new IllegalArgumentException("testId must be specified");
      }
   }

   private void reportNullParam(final String... strings) {
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < strings.length; i += 2) {
         if (strings[i + 1] == null) {
            if (sb.length() > 0) {
               sb.append(", ");
            }
            sb.append(strings[i]);
         }
      }
      throw new IllegalStateException("parameter was null: " + sb.toString());
   }

   private void printSetupTestData(final String testId, final String request, final String response) {
      final StringBuilder sb = new StringBuilder();
      sb.append("\n============================\n");
      sb.append("\nTestdata with id:{" + testId + "} created");
      sb.append("\nSetup with Request:\n" + request);
      sb.append("\nSetup with Response:\n" + response);
      sb.append("\n\n============================\n");
      LOG.debug(sb.toString());
   }

   private SocketTestData matchRequest(final SocketTestData sought) {
      for (final SocketTestData testData : data.values()) {
         final AtomicInteger atomicInteger = calledTests.get(testData.getId());
         if (!testData.hasReachedMaximumCallCount(atomicInteger.get()) && testData.matches(sought)) {
            LOG.info("Matched incoming request with testdata id: {}", testData.getId());
            calledTests.get(testData.getId()).incrementAndGet();

            return testData;
         }
      }
      return null;
   }

   public void removeTest(final String testId) {
      if (testId != null) {
         data.remove(testId);
      }
   }

   public static Logger getLog() {
      return LOG;
   }

   public ServerSocket getServerSocket() {
      return serverSocket;
   }

   public Object getMonitor() {
      return monitor;
   }

   public Map<String, SocketTestData> getData() {
      return data;
   }

   public Map<String, AtomicInteger> getCalledTests() {
      return calledTests;
   }

   public Thread getServerThread() {
      return serverThread;
   }

   public int getPort() {
      return port;
   }
}
