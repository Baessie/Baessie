package org.baessie.socket

import java.net.Socket
import scala.actors.DaemonActor

trait SocketDispatcher extends DaemonActor {
  def terminate
}
