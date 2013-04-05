package org.baessie.simulator.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler implements Runnable {

   private static final Logger LOG = LoggerFactory.getLogger(SocketHandler.class);
   private final Socket socket;
   private final SocketSimulator socketSimulator;

   public SocketHandler(final Socket socket, final SocketSimulator socketSimulator) {
      this.socket = socket;
      this.socketSimulator = socketSimulator;
   }

   @Override
   public void run() {
      try {
         final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
         final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

         final byte[] buff = new byte[1024];
         while (!socket.isInputShutdown()) {
            final int readBytes = dataInputStream.read(buff);
            if (readBytes > 0) {
               final String request = new String(buff, 0, readBytes);
               LOG.info("Got request: " + request);
               final SocketTestData response = socketSimulator.findResponseToSendBack(request);
               dataOutputStream.write(response.getResponse().getBytes());
               dataOutputStream.flush();
               if (response.isCloseAfterResponse()) {
                  socket.shutdownInput();
               }
            } else {
               LOG.info("SocketHandler got EOF. Closing down.");
               socket.shutdownInput();
            }
         }
      } catch (final IOException e) {
         LOG.warn(e.getMessage(), e);
      } finally {
         try {
            socket.close();
         } catch (final IOException e) {
            LOG.warn(e.getMessage(), e);
         }
      }
   }
}
