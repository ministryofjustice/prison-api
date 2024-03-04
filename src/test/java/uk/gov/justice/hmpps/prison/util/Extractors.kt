@file:Suppress("UNCHECKED_CAST")

package uk.gov.justice.hmpps.prison.util

import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDate
import java.util.function.Function

object Extractors {
  @JvmStatic
  fun extractString(field: String?): Function<Any, String?> = Function { m: Any ->
    (m as Map<String?, String?>)[field]
  }

  @JvmStatic
  fun extractInteger(field: String?): Function<Any, Int> = Function { m: Any ->
    (m as Map<String?, BigDecimal>)[field]!!.toInt()
  }

  @JvmStatic
  fun extractLong(field: String?): Function<Any, Long> = Function { m: Any ->
    (m as Map<String?, BigDecimal>)[field]!!.toLong()
  }

  @JvmStatic
  fun extractDate(field: String?): Function<Any, LocalDate> = Function { m: Any ->
    (m as Map<String?, Timestamp>)[field]!!.toLocalDateTime().toLocalDate()
  }
}
