package org.baessie.socket

import org.slf4j.{Marker, Logger}
import java.lang.{Throwable, String}

class LoggerMock extends Logger {

  def debug(msg: String) {}

  def getName = ""

  def isTraceEnabled = false

  def trace(msg: String) {}

  def trace(format: String, arg: AnyRef) {}

  def trace(format: String, arg1: AnyRef, arg2: AnyRef) {}

  def trace(format: String, argArray: Array[AnyRef]) {}

  def trace(msg: String, t: Throwable) {}

  def isTraceEnabled(marker: Marker) = false

  def trace(marker: Marker, msg: String) {}

  def trace(marker: Marker, format: String, arg: AnyRef) {}

  def trace(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) {}

  def trace(marker: Marker, format: String, argArray: Array[AnyRef]) {}

  def trace(marker: Marker, msg: String, t: Throwable) {}

  def isDebugEnabled = false

  def debug(format: String, arg: AnyRef) {}

  def debug(format: String, arg1: AnyRef, arg2: AnyRef) {}

  def debug(format: String, argArray: Array[AnyRef]) {}

  def debug(msg: String, t: Throwable) {}

  def isDebugEnabled(marker: Marker) = false

  def debug(marker: Marker, msg: String) {}

  def debug(marker: Marker, format: String, arg: AnyRef) {}

  def debug(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) {}

  def debug(marker: Marker, format: String, argArray: Array[AnyRef]) {}

  def debug(marker: Marker, msg: String, t: Throwable) {}

  def isInfoEnabled = false

  def info(msg: String) {}

  def info(format: String, arg: AnyRef) {}

  def info(format: String, arg1: AnyRef, arg2: AnyRef) {}

  def info(format: String, argArray: Array[AnyRef]) {}

  def info(msg: String, t: Throwable) {}

  def isInfoEnabled(marker: Marker) = false

  def info(marker: Marker, msg: String) {}

  def info(marker: Marker, format: String, arg: AnyRef) {}

  def info(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) {}

  def info(marker: Marker, format: String, argArray: Array[AnyRef]) {}

  def info(marker: Marker, msg: String, t: Throwable) {}

  def isWarnEnabled = false

  def warn(msg: String) {}

  def warn(format: String, arg: AnyRef) {}

  def warn(format: String, argArray: Array[AnyRef]) {}

  def warn(format: String, arg1: AnyRef, arg2: AnyRef) {}

  def warn(msg: String, t: Throwable) {}

  def isWarnEnabled(marker: Marker) = false

  def warn(marker: Marker, msg: String) {}

  def warn(marker: Marker, format: String, arg: AnyRef) {}

  def warn(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) {}

  def warn(marker: Marker, format: String, argArray: Array[AnyRef]) {}

  def warn(marker: Marker, msg: String, t: Throwable) {}

  def isErrorEnabled = false

  def error(msg: String) {}

  def error(format: String, arg: AnyRef) {}

  def error(format: String, arg1: AnyRef, arg2: AnyRef) {}

  def error(format: String, argArray: Array[AnyRef]) {}

  def error(msg: String, t: Throwable) {}

  def isErrorEnabled(marker: Marker) = false

  def error(marker: Marker, msg: String) {}

  def error(marker: Marker, format: String, arg: AnyRef) {}

  def error(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) {}

  def error(marker: Marker, format: String, argArray: Array[AnyRef]) {}

  def error(marker: Marker, msg: String, t: Throwable) {}
}
