@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PrisonerScheduleDtoTest {
  @Nested
  inner class programHasntEnded {
    @Test
    internal fun programStillActive() {
      assertThat(dto.copy(programStatus = "ALLOC").programHasntEnded()).isTrue
    }

    @Test
    internal fun programEndedNoEndDate() {
      assertThat(dto.copy(programStatus = "END", programEndDate = null).programHasntEnded()).isTrue
    }

    @Test
    internal fun programEndedNoScheduleDate() {
      assertThat(
        dto.copy(
          programStatus = "END",
          programEndDate = LocalDate.parse("2022-05-01"),
          scheduleDate = null,
        )
          .programHasntEnded()
      ).isTrue
    }

    @Test
    internal fun programEndedEndDateOnScheduleDate() {
      assertThat(
        dto.copy(
          programStatus = "END",
          programEndDate = LocalDate.parse("2022-05-01"),
          scheduleDate = LocalDate.parse("2022-05-01"),
        ).programHasntEnded()
      ).isTrue
    }

    @Test
    internal fun programEndedEndDateAfterScheduleDate() {
      assertThat(
        dto.copy(
          programStatus = "END",
          programEndDate = LocalDate.parse("2022-05-02"),
          scheduleDate = LocalDate.parse("2022-05-01"),
        ).programHasntEnded()
      ).isTrue
    }

    @Test
    internal fun programEndedEndDateBeforeScheduleDate() {
      assertThat(
        dto.copy(
          programStatus = "END",
          programEndDate = LocalDate.parse("2022-05-01"),
          scheduleDate = LocalDate.parse("2022-05-02"),
        ).programHasntEnded()
      ).isFalse
    }
  }

  private val dto: PrisonerScheduleDto = PrisonerScheduleDto(
    offenderNo = "A1234",
    eventId = null,
    bookingId = null,
    locationId = null,
    firstName = null,
    lastName = null,
    cellLocation = null,
    event = null,
    eventType = null,
    eventDescription = null,
    eventLocation = null,
    eventLocationId = null,
    eventStatus = null,
    comment = null,
    startTime = null,
    endTime = null,
    eventOutcome = null,
    performance = null,
    outcomeComment = null,
    paid = null,
    payRate = null,
    excluded = null,
    timeSlot = null,
    locationCode = null,
    suspended = null,
    programStatus = null,
    programEndDate = null,
    scheduleDate = null,
  )
}
