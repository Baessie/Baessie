package org.baessie.socket

import org.baessie.common.TestDataManager
import org.slf4j.Logger
import java.net.{SocketTimeoutException, Socket}
import scala.actors.DaemonActor
import java.io.{IOException, DataOutputStream, DataInputStream}

class SocketWorker(val id: Int, val dispatcher: SocketDispatcher, val testDataManager: TestDataManager, val logger: Logger) extends DaemonActor {
  @volatile
  var running = true

  def act() {
    while (running) {
      react {
        case Connection(id, socket) =>
          handleConnection(socket)
          dispatcher ! Idle(this)
      }
    }
  }

  override def hashCode(): Int = id

  override def equals(other: Any): Boolean =
    other match {
      case that: SocketWorker => this.id == that.id
      case _
      => false
    }

  def handleConnection(socket: Socket) {
    socket.setSoTimeout(1000)
    val dataInputStream = new DataInputStream(socket.getInputStream());
    val dataOutputStream = new DataOutputStream(socket.getOutputStream());

    val buff = new Array[Byte](10240)
    while (!socket.isInputShutdown && running) {
      try {
        val readBytes = dataInputStream.read(buff)
        if (readBytes > 0) {
          val request = new String(buff, 0, readBytes).trim
          logger.debug("Got request: " + request)
          val result = testDataManager.findMatching(new SocketTestData("", request, "", 0, false))
          if (result.isDefined) {
            result.get.incrementCallCount
            val socketTestData = result.get.asInstanceOf[SocketTestData]
            dataOutputStream.write(socketTestData.response.getBytes());
            dataOutputStream.flush();
            if (socketTestData.closeAfterResponse) {
              socket.shutdownInput();
            }
          } else {
            // TODO: Fix error output
          }
        } else {
          socket.shutdownInput()
        }
      } catch {
        case ex: SocketTimeoutException => {}
        case ex: IOException => {}
      }
    }
    socket.close()
  }

  def terminate {
    running = false
  }
}
