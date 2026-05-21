package uk.gov.justice.hmpps.prison.api.model.bulkappointments

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class RepeatPeriodTest {
  @Test
  fun assertValidTestData() {
    assertThat(SATURDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY)
    assertThat(SUNDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY)
  }

  @Test
  fun endDateTimeNotDefinedForSaturdayStart() {
    assertThatThrownBy { RepeatPeriod.WEEKDAYS.endDateTime(SATURDAY, 0) }
      .isInstanceOf(ValidationException::class.java)
      .hasMessage("Weekend starts not allowed for WEEKDAY repeat period, but 2019-01-05T01:01 is a Saturday or Sunday")
  }

  @Test
  fun endDateTimeNotDefinedForSundayStart() {
    assertThatThrownBy { RepeatPeriod.WEEKDAYS.endDateTime(SUNDAY, 0) }
      .isInstanceOf(ValidationException::class.java)
      .hasMessage("Weekend starts not allowed for WEEKDAY repeat period, but 2019-01-06T01:01 is a Saturday or Sunday")
  }

  companion object {
    private val SATURDAY: LocalDateTime = LocalDateTime.of(2019, 1, 5, 1, 1)
    private val SUNDAY: LocalDateTime = LocalDateTime.of(2019, 1, 6, 1, 1)
  }
}
