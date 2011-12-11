package org.baessie.socket

import java.lang.Thread
import org.apache.tapestry5.ioc.services.{RegistryShutdownHub, RegistryShutdownListener}
import org.apache.tapestry5.ioc.annotations.PostInjection
import java.net.{Socket, SocketTimeoutException, ServerSocket}
import java.io.IOException

class SocketSimulator(val dispatcher: SocketDispatcher, val port: Int = 12345) extends Runnable with RegistryShutdownListener {
  val serverSocket = new ServerSocket(port)
  serverSocket.setSoTimeout(1000)

  val thread = new Thread(this, "SocketSimulator")
  thread.setDaemon(true)
  thread.start

  @volatile
  var running = true

  def run() {
    var i = 0

    while (running) {
      try {
        val socket = serverSocket.accept()
        i += 1
        dispatcher ! Connection(i, socket)
      } catch {
        case ex: SocketTimeoutException => {}
      }
    }

    serverSocket.close()
  }

  def terminate {
    running = false
    dispatcher.terminate
    while (stillRunning) {
      Thread.sleep(200)
    }
  }

  @PostInjection
  def startupService(shutdownHub: RegistryShutdownHub) {
    shutdownHub.addRegistryShutdownListener(this)
  }

  def registryDidShutdown() {
    terminate
  }

  def stillRunning(): Boolean = {
    try {
      new Socket("localhost", port).close
      true
    } catch {
      case ex: IOException => {
        false
      }
    }
  }
}
