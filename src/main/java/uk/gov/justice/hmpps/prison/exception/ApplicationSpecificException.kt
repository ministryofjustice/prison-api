package uk.gov.justice.hmpps.prison.exception

import java.lang.RuntimeException

open class ApplicationSpecificException(message: String?) : RuntimeException(message) {
  var errorCode: Int? = null
}
