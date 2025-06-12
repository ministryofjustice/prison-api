package uk.gov.justice.hmpps.prison.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.springframework.test.web.reactive.server.JsonPathAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Suppress("ktlint:standard:function-naming")
// add more months when required
infix fun Int.JAN(year: Int): LocalDate = LocalDate.of(year, 1, this)
infix fun LocalDate.at(time: String): LocalDateTime = LocalTime.parse(time).let { this.atTime(it) }
infix fun LocalDate.at(time: TimePoints): LocalDateTime = when (time) {
  TimePoints.midnight -> this.atStartOfDay()
  TimePoints.midday -> this.atTime(12, 0)
}
infix fun Int.h(minutes: Int): LocalTime = LocalTime.of(this, minutes)
enum class TimePoints {
  @Suppress("ktlint:standard:enum-entry-name-case")
  midnight,

  @Suppress("ktlint:standard:enum-entry-name-case")
  midday,
}

fun JsonPathAssertions.isAboutNow(): WebTestClient.BodyContentSpec = this.value<String> {
  assertThat(LocalDateTime.parse(it)).isCloseTo(LocalDateTime.now(), within(30, ChronoUnit.SECONDS))
}
