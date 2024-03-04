package uk.gov.justice.hmpps.prison.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.hmpps.prison.api.support.TimeSlot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.stream.Stream

class CalcDateRangesTest {
  @Test
  fun testDateRangeIsNullForNoDates() {
    val testClass = CalcDateRanges(null, null, null, TEN_YEARS)
    assertThat(testClass.dateFrom).isNull()
    assertThat(testClass.dateTo).isNull()
  }

  @Test
  fun testFromAndToDateSetForDobOnly() {
    val dob = LocalDate.now().atStartOfDay().toLocalDate()
    val testClass = CalcDateRanges(dob, null, null, TEN_YEARS)
    assertThat(testClass.dateFrom).isEqualTo(dob)
    assertThat(testClass.dateTo).isEqualTo(dob)
  }

  @Test
  fun testDateRangeCalcLessThan10Years() {
    val fromDate = LocalDate.now().atStartOfDay().toLocalDate()
    val toDate = fromDate.plusYears(3)
    val testClass = CalcDateRanges(null, fromDate, toDate, TEN_YEARS)
    assertThat(testClass.dateFrom).isEqualTo(fromDate)
    assertThat(testClass.dateTo).isEqualTo(toDate)
  }

  @Test
  fun testDateRangeCalcEqual10Years() {
    val fromDate = LocalDate.now().atStartOfDay().toLocalDate()
    val toDate = fromDate.plusYears(TEN_YEARS.toLong())
    val testClass = CalcDateRanges(null, fromDate, toDate, TEN_YEARS)
    assertThat(testClass.dateFrom).isEqualTo(fromDate)
    assertThat(testClass.dateTo).isEqualTo(toDate)
  }

  @Test
  fun testDateRangeCalcMoreThan10Years() {
    val fromDate = LocalDate.now().atStartOfDay().toLocalDate()
    val toDate = fromDate.plusYears(30)
    val testClass = CalcDateRanges(null, fromDate, toDate, TEN_YEARS)
    assertThat(testClass.dateFrom).isEqualTo(fromDate)
    assertThat(testClass.dateTo).isNotEqualTo(toDate)
    assertThat(testClass.dateTo).isEqualTo(fromDate.plusYears(TEN_YEARS.toLong()))
  }

  @Test
  fun testDateRangeCalcOnlyFromSpecified() {
    val fromDate = LocalDate.now().atStartOfDay().toLocalDate()
    val testClass = CalcDateRanges(null, fromDate, null, TEN_YEARS)
    assertThat(testClass.dateFrom).isEqualTo(fromDate)
    assertThat(testClass.dateTo).isEqualTo(fromDate.plusYears(TEN_YEARS.toLong()))
  }

  @Test
  fun testDateRangeCalcOnlyToSpecified() {
    val toDate = LocalDate.now().atStartOfDay().toLocalDate()
    val testClass = CalcDateRanges(null, null, toDate, TEN_YEARS)
    assertThat(testClass.dateTo).isEqualTo(toDate)
    assertThat(testClass.dateFrom).isEqualTo(toDate.minusYears(TEN_YEARS.toLong()))
  }

  @Test
  fun testStartTimeToTimeSlot() {
    val dummy = LocalDate.of(2018, 10, 5)
    assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(0, 0))))
      .isEqualTo(TimeSlot.AM)
    assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(11, 59))))
      .isEqualTo(TimeSlot.AM)
    assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(12, 0))))
      .isEqualTo(TimeSlot.PM)
    assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(16, 59))))
      .isEqualTo(TimeSlot.PM)
    assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(17, 0))))
      .isEqualTo(TimeSlot.ED)
    assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(23, 59))))
      .isEqualTo(TimeSlot.ED)
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  inner class SecureEndpoints {
    @ParameterizedTest
    @MethodSource("eventStartsInTimeslot")
    fun testEventStartsInTimeslot(data: EventStartsInTimeslotData) {
      assertThat(CalcDateRanges.eventStartsInTimeslot(data.startDateTime, data.timeSlot))
        .isEqualTo(data.result)
    }

    @ParameterizedTest
    @MethodSource("eventStartsInTimeslots")
    fun testEventStartsInTimeslots(data: EventStartsInTimeslotsData) {
      assertThat(CalcDateRanges.eventStartsInTimeslots(data.startDateTime, data.timeSlots))
        .isEqualTo(data.result)
    }

    private fun eventStartsInTimeslot(): Stream<EventStartsInTimeslotData> = Stream.of(
      EventStartsInTimeslotData(0, 0, null, true),
      EventStartsInTimeslotData(0, 0, TimeSlot.AM, true),
      EventStartsInTimeslotData(11, 59, TimeSlot.AM, true),
      EventStartsInTimeslotData(12, 0, TimeSlot.PM, true),
      EventStartsInTimeslotData(16, 59, TimeSlot.PM, true),
      EventStartsInTimeslotData(17, 0, TimeSlot.ED, true),
      EventStartsInTimeslotData(23, 59, TimeSlot.ED, true),
      EventStartsInTimeslotData(0, 0, TimeSlot.PM, false),
      EventStartsInTimeslotData(0, 0, TimeSlot.ED, false),
      EventStartsInTimeslotData(11, 59, TimeSlot.PM, false),
      EventStartsInTimeslotData(11, 59, TimeSlot.ED, false),
      EventStartsInTimeslotData(12, 0, TimeSlot.ED, false),
      EventStartsInTimeslotData(12, 0, TimeSlot.AM, false),
      EventStartsInTimeslotData(16, 59, TimeSlot.ED, false),
      EventStartsInTimeslotData(16, 59, TimeSlot.AM, false),
      EventStartsInTimeslotData(17, 0, TimeSlot.PM, false),
      EventStartsInTimeslotData(17, 0, TimeSlot.AM, false),
      EventStartsInTimeslotData(23, 59, TimeSlot.PM, false),
      EventStartsInTimeslotData(23, 59, TimeSlot.AM, false),
    )

    private fun eventStartsInTimeslots(): Stream<EventStartsInTimeslotsData> = Stream.concat(
      eventStartsInTimeslot().map { (hour, minute, timeSlot, result): EventStartsInTimeslotData ->
        EventStartsInTimeslotsData(
          hour,
          minute,
          if (timeSlot == null) null else setOf(timeSlot),
          result,
        )
      },
      Stream.of(
        EventStartsInTimeslotsData(0, 0, setOf(), true),
        EventStartsInTimeslotsData(11, 59, setOf(TimeSlot.AM, TimeSlot.PM), true),
        EventStartsInTimeslotsData(11, 59, setOf(TimeSlot.PM, TimeSlot.ED), false),
        EventStartsInTimeslotsData(17, 59, setOf(TimeSlot.PM, TimeSlot.ED), true),
      ),
    )
  }

  data class EventStartsInTimeslotData(val hour: Int, val minute: Int, val timeSlot: TimeSlot?, val result: Boolean) {
    val startDateTime: LocalDateTime
      get() = LocalDateTime.of(2018, 10, 5, hour, minute, 0)
  }

  class EventStartsInTimeslotsData(val hour: Int, val minute: Int, val timeSlots: Set<TimeSlot>?, val result: Boolean) {
    val startDateTime: LocalDateTime
      get() = LocalDateTime.of(2018, 10, 5, hour, minute, 0)
  }

  companion object {
    private const val TEN_YEARS = 10
  }
}
