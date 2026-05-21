package uk.gov.justice.hmpps.prison.api.model.bulkappointments

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RepeatTest {
  @Test
  fun singleElementStream() {
    assertThat(
      Repeat.builder()
        .count(1)
        .repeatPeriod(RepeatPeriod.DAILY)
        .build()
        .dateTimeStream(WEDNESDAY_START)
        .toList(),
    )
      .containsExactly(WEDNESDAY_START)
  }

  @Test
  fun multiDay() {
    assertThat(
      Repeat.builder()
        .count(7)
        .repeatPeriod(RepeatPeriod.DAILY)
        .build()
        .dateTimeStream(WEDNESDAY_START)
        .toList(),
    )
      .containsExactly(
        WEDNESDAY_START, // Wednesday
        WEDNESDAY_START.plusDays(1), // Thursday
        WEDNESDAY_START.plusDays(2), // Friday
        WEDNESDAY_START.plusDays(3), // Saturday
        WEDNESDAY_START.plusDays(4), // Sunday
        WEDNESDAY_START.plusDays(5), // Monday
        WEDNESDAY_START.plusDays(6), // Tuesday
      )
  }

  @Test
  fun multiWeekday() {
    assertThat(
      Repeat.builder()
        .count(14)
        .repeatPeriod(RepeatPeriod.WEEKDAYS)
        .build()
        .dateTimeStream(WEDNESDAY_START)
        .toList(),
    )
      .containsExactly(
        WEDNESDAY_START, // Wednesday
        WEDNESDAY_START.plusDays(1), // Thursday
        WEDNESDAY_START.plusDays(2), // Friday
        WEDNESDAY_START.plusDays(5), // Monday
        WEDNESDAY_START.plusDays(6), // Tuesday
        WEDNESDAY_START.plusDays(7), // Wednesday
        WEDNESDAY_START.plusDays(8), // Thursday
        WEDNESDAY_START.plusDays(9), // Friday
        WEDNESDAY_START.plusDays(12), // Monday
        WEDNESDAY_START.plusDays(13), // Tuesday
        WEDNESDAY_START.plusDays(14), // Wednesday
        WEDNESDAY_START.plusDays(15), // Thursday
        WEDNESDAY_START.plusDays(16), // Friday
        WEDNESDAY_START.plusDays(19), // Monday
      )
  }

  @Test
  fun multiWeek() {
    assertThat(
      Repeat.builder()
        .count(7)
        .repeatPeriod(RepeatPeriod.WEEKLY)
        .build()
        .dateTimeStream(WEDNESDAY_START)
        .toList(),
    )
      .containsExactly(
        WEDNESDAY_START,
        WEDNESDAY_START.plusDays(7),
        WEDNESDAY_START.plusDays((7 * 2).toLong()),
        WEDNESDAY_START.plusDays((7 * 3).toLong()),
        WEDNESDAY_START.plusDays((7 * 4).toLong()),
        WEDNESDAY_START.plusDays((7 * 5).toLong()),
        WEDNESDAY_START.plusDays((7 * 6).toLong()),
      )
  }

  @Test
  fun multiFortnight() {
    assertThat(
      Repeat.builder()
        .count(7)
        .repeatPeriod(RepeatPeriod.FORTNIGHTLY)
        .build()
        .dateTimeStream(WEDNESDAY_START)
        .toList(),
    )
      .containsExactly(
        WEDNESDAY_START,
        WEDNESDAY_START.plusDays(14),
        WEDNESDAY_START.plusDays((14 * 2).toLong()),
        WEDNESDAY_START.plusDays((14 * 3).toLong()),
        WEDNESDAY_START.plusDays((14 * 4).toLong()),
        WEDNESDAY_START.plusDays((14 * 5).toLong()),
        WEDNESDAY_START.plusDays((14 * 6).toLong()),
      )
  }

  @Test
  fun multiMonth() {
    assertThat(
      Repeat.builder()
        .count(7)
        .repeatPeriod(RepeatPeriod.MONTHLY)
        .build()
        .dateTimeStream(JAN_31_2019_START)
        .toList(),
    )
      .containsExactly(
        JAN_31_2019_START,
        LocalDateTime.of(LocalDate.of(2019, 2, 28), START_TIME),
        LocalDateTime.of(LocalDate.of(2019, 3, 31), START_TIME),
        LocalDateTime.of(LocalDate.of(2019, 4, 30), START_TIME),
        LocalDateTime.of(LocalDate.of(2019, 5, 31), START_TIME),
        LocalDateTime.of(LocalDate.of(2019, 6, 30), START_TIME),
        LocalDateTime.of(LocalDate.of(2019, 7, 31), START_TIME),
      )
  }

  @Test
  fun noArgsConstructor() {
    Repeat()
  }

  @Test
  fun allArgsConstructor() {
    Repeat(RepeatPeriod.DAILY, 1)
  }

  companion object {
    private val START_TIME: LocalTime = LocalTime.of(13, 50)
    private val WEDNESDAY: LocalDate = LocalDate.of(2019, 2, 27)
    private val WEDNESDAY_START: LocalDateTime = LocalDateTime.of(WEDNESDAY, START_TIME)
    private val JAN_31_2019: LocalDate = LocalDate.of(2019, 1, 31)
    private val JAN_31_2019_START: LocalDateTime = LocalDateTime.of(JAN_31_2019, START_TIME)
  }
}
