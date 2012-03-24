package org.baessie.socket.services

import org.apache.tapestry5.services.LibraryMapping
import org.apache.tapestry5.ioc.Configuration
import org.baessie.common.TestDataManager
import org.slf4j.Logger
import org.apache.tapestry5.ioc.annotations.EagerLoad
import org.baessie.socket.{SocketDispatcherImpl, SocketSimulator}
import org.apache.tapestry5.ioc.services.RegistryShutdownHub

class SocketModule {}

object SocketModule {
  def contributeComponentClassResolver(configuration: Configuration[LibraryMapping]) {
    configuration.add(new LibraryMapping("socket", "org.baessie.socket"));
  }

  @EagerLoad
  def buildSocketSimulator(testDataManager: TestDataManager, logger: Logger, shutdownHub: RegistryShutdownHub): SocketSimulator = {
    val dispatcher = new SocketDispatcherImpl(testDataManager, logger)
    dispatcher.start()
    val socketSimulator = new SocketSimulator(dispatcher)
    shutdownHub.addRegistryShutdownListener(socketSimulator)
    return socketSimulator
  }
}
