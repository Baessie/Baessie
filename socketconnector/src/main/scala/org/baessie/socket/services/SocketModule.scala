package org.baessie.socket.services

import org.apache.tapestry5.services.LibraryMapping
import org.apache.tapestry5.ioc.Configuration
import org.baessie.common.TestDataManager
import org.slf4j.Logger
import org.apache.tapestry5.ioc.annotations.EagerLoad
import org.baessie.socket.{SocketDispatcherImpl, SocketSimulator}

class SocketModule {}

object SocketModule {
  def contributeComponentClassResolver(configuration: Configuration[LibraryMapping]) {
    configuration.add(new LibraryMapping("socket", "org.baessie.socket"));
  }

  @EagerLoad
  def buildSocketSimulator(testDataManager: TestDataManager, logger: Logger): SocketSimulator = {
    val dispatcher = new SocketDispatcherImpl(testDataManager, logger)
    dispatcher.start()
    return new SocketSimulator(dispatcher)
  }
}
