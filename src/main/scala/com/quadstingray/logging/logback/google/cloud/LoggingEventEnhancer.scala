package com.quadstingray.logging.logback.google.cloud

import ch.qos.logback.classic.spi.ILoggingEvent
import com.google.cloud.logging.LogEntry

trait LoggingEventEnhancer {
  def enhanceLogEntry(builder: LogEntry.Builder, e: ILoggingEvent)
}
