package uk.gov.justice.hmpps.prison.util

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory

fun <T> findLogAppender(javaClass: Class<in T>): ListAppender<ILoggingEvent> {
  val logger = LoggerFactory.getLogger(javaClass) as Logger
  val listAppender = ListAppender<ILoggingEvent>()
  listAppender.start()
  logger.addAppender(listAppender)
  return listAppender
}

infix fun List<ILoggingEvent>?.hasLogMessageContaining(partialMessage: String) =
  this?.find { logEvent -> logEvent.formattedMessage.contains(partialMessage) } != null
