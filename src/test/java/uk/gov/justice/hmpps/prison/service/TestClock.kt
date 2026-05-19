package uk.gov.justice.hmpps.prison.service

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@TestConfiguration
class TestClock {
  @Bean
  fun clock(): Clock = Clock.fixed(
    LocalDateTime.of(2020, 1, 2, 3, 4, 5).atZone(ZoneId.systemDefault()).toInstant(),
    ZoneId.systemDefault(),
  )
}
