package org.baessie.services

import org.apache.tapestry5.ioc.{ObjectLocator, OrderedConfiguration, MappedConfiguration, ServiceBinder}
import org.apache.tapestry5.SymbolConstants
import org.slf4j.Logger
import org.apache.tapestry5.services._
import org.baessie.common.TestDataManager
import org.baessie.ws.services.WsModule
import org.apache.tapestry5.ioc.annotations.SubModule

@SubModule(Array(classOf[org.baessie.ws.services.WsModule]))
object AppModule {
  def bind(binder: ServiceBinder) {
    binder.bind(classOf[TestDataManager], classOf[TestDataManagerImpl])
  }

  def contributeApplicationDefaults(configuration: MappedConfiguration[String, String]) {
    configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");
    configuration.add(SymbolConstants.PRODUCTION_MODE, "false");
  }

  def buildTimingFilter(log: Logger) = {
    new RequestFilter() {
      override def service(request: Request, response: Response, handler: RequestHandler): Boolean = {
        var startTime = System.currentTimeMillis()
        try {
          handler.service(request, response)
        } finally {
          val elapsed = System.currentTimeMillis() - startTime
          log.info("Request time: %d ms".format(elapsed))
        }
      }
    }
  }

  def contributeRequestHandler(configuration: OrderedConfiguration[RequestFilter],
                               locator: ObjectLocator) {
    configuration.add("Timing", locator.getService("timingFilter", classOf[RequestFilter]))
  }
}
