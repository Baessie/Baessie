package org.baessie.socket

import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{OneInstancePerTest, Spec}
import java.io.{DataInputStream, DataOutputStream}
import java.net.{SocketException, Socket}

@RunWith(classOf[JUnitRunner])
class SocketSimulatorSpec extends Spec with ShouldMatchers with OneInstancePerTest {
  val testDataManager = new TestDataManagerMock
  val logger          = new LoggerMock
  val dispatcher      = new SocketDispatcherImpl(testDataManager, logger)
  dispatcher.start()

  describe("SocketSimulator") {
    it("should handle simple calls") {
      val simulator = new SocketSimulator(dispatcher, 65123)

      val request = "GET subscriber?chnr=0001&abnr=040-123456"
      val expectedResponse = "chnr=0001&status=00"
      testDataManager.add(new SocketTestData("1", request, expectedResponse, 1, false))

      val socketClient = new SocketClient(simulator.port)
      socketClient.write(request)
      val response = socketClient.read()

      response should be(Some(expectedResponse))

      simulator terminate
    }

    it("should close it's socket if CloseAfterResponse is true") {
      val simulator = new SocketSimulator(dispatcher, 65123)

      val request = "GET subscriber?chnr=0001&abnr=040-123456"
      val expectedResponse = "chnr=0001&status=00"
      testDataManager.add(new SocketTestData("1", request, expectedResponse, 1, true))

      val socketClient = new SocketClient(simulator.port)
      socketClient.write(request)
      socketClient.read()

      socketClient.socketClosed() should be(true)

      simulator terminate
    }

    it("should close it's socket at EOF") {
      val simulator = new SocketSimulator(dispatcher, 65123)

      val request = "GET subscriber?chnr=0001&abnr=040-123456"
      val expectedResponse = "chnr=0001&status=00"
      testDataManager.add(new SocketTestData("1", request, expectedResponse, 1, false))

      val socketClient = new SocketClient(simulator.port)
      socketClient.write(request)
      socketClient.shutdownOutput
      socketClient.read()

      socketClient.socketClosed() should be(true)

      simulator terminate
    }

    it("should handle multiple calls on one connection") {
      val simulator = new SocketSimulator(dispatcher, 65123)

      val request1 = "LOGIN"
      val expectedResponse1 = "GNURRF"
      testDataManager.add(new SocketTestData("1", request1, expectedResponse1, 1, false))
      val request2 = "GET subscriber?chnr=0001&abnr=040-123456"
      val expectedResponse2 = "chnr=0001&status=00"
      testDataManager.add(new SocketTestData("2", request2, expectedResponse2, 1, false))

      val socketClient = new SocketClient(simulator.port)
      socketClient.write(request1)
      var response = socketClient.read()

      response should be(Some(expectedResponse1))

      socketClient.write(request2)
      response = socketClient.read()

      response should be(Some(expectedResponse2))

      simulator terminate
    }
  }
}

class SocketClient(val port: Int) {
  val socket = new Socket("localhost", port)
  socket.setSoTimeout(30000)
  val dataOutputStream = new DataOutputStream(socket.getOutputStream());
  val dataInputStream  = new DataInputStream(socket.getInputStream());

  def write(request: String) {
    dataOutputStream.write(request.getBytes())
    dataOutputStream.flush()
  }

  def read(): Option[String] = {
    try {
      val buff = new Array[Byte](10240)
      val readBytes = dataInputStream.read(buff)
      if (readBytes > 0) Some(new String(buff, 0, readBytes)) else None
    } catch {
      case ex: SocketException => None
    }
  }

  def socketClosed(): Boolean = {
    read.isEmpty
  }

  def shutdownOutput {
    socket.shutdownOutput()
  }
}
