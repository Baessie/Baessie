package org.baessie.socket

import java.net.Socket

case class Idle(worker: SocketWorker)

case class Connection(id: Int, socket: Socket)

