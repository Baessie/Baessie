package org.baessie.services

import org.apache.tapestry5.ioc.OrderedConfiguration
import org.apache.tapestry5.services.{ComponentEventRequestParameters, ComponentRequestHandler, PageRenderRequestParameters, ComponentRequestFilter}

object TestModule {
  def contributeComponentRequestHandler(configuration: OrderedConfiguration[ComponentRequestFilter]) {
    configuration.overrideInstance("WSRequest", classOf[DummyFilter], "after:PageRender");
  }
}

class TestModule

class DummyFilter extends ComponentRequestFilter {
  def handleComponentEvent(parameters: ComponentEventRequestParameters, handler: ComponentRequestHandler) {
    handler.handleComponentEvent(parameters)
  }

  def handlePageRender(parameters: PageRenderRequestParameters, handler: ComponentRequestHandler) {
    handler.handlePageRender(parameters)
  }
}
