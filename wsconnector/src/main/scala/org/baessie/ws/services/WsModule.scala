package org.baessie.ws.services

import org.apache.tapestry5.ioc.{Configuration, OrderedConfiguration}
import org.apache.tapestry5.services.{LibraryMapping, ComponentRequestFilter}

class WsModule {}

object WsModule {
  def contributeComponentRequestHandler(configuration: OrderedConfiguration[ComponentRequestFilter]) {
    configuration.addInstance("WSRequest", classOf[WSRequestFilter], "after:PageRender");
  }

  def contributeComponentClassResolver(configuration: Configuration[LibraryMapping]) {
      configuration.add(new LibraryMapping("ws", "org.baessie.ws"));
  }
}
