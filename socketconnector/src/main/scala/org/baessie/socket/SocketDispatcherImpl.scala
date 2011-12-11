package org.baessie.socket

import scala.collection.mutable.ListBuffer
import org.baessie.common.TestDataManager
import scala.util.Random
import org.slf4j.Logger

class SocketDispatcherImpl(testDataManager: TestDataManager, logger: Logger) extends SocketDispatcher {
  val allWorkers  = new ListBuffer[SocketWorker]
  val idleWorkers = new ListBuffer[SocketWorker]
  var busyWorkers = Map[Int, SocketWorker]()
  @volatile
  var running     = true

  for (i <- 1 to Runtime.getRuntime.availableProcessors() * 4) {
    val worker = new SocketWorker(i, this, testDataManager, logger)
    worker.start()
    allWorkers += worker
    idleWorkers += worker
  }

  def act() {
    while (running) {
      react {
        case Idle(worker) =>
          busyWorkers -= worker.id
          idleWorkers += worker

        case connection: Connection =>
          val worker =
            if (idleWorkers.length == 0)
              busyWorkers.get(Random.nextInt(busyWorkers.size)).get
            else {
              val w = idleWorkers.remove(0)
              busyWorkers += w.id -> w
              w
            }
          worker ! connection
      }
    }
  }

  def terminate() {
    running = false
    allWorkers.foreach(_.terminate)
  }
}
