package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance
import uk.gov.justice.hmpps.prison.api.model.VisitBalances
import uk.gov.justice.hmpps.prison.api.model.VisitDetails
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.support.PayableAttendanceOutcomeDto
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser("ITAG_USER")
class BookingRepositoryTest(
  @Autowired private val repository: BookingRepository,
  @Autowired private val scheduleRepository: ScheduleRepository,
) {

  @Test
  fun testGetBookingVisitNextSameDay() {
    val visit = repository.getBookingVisitNext(-1L, LocalDateTime.parse("2016-12-11T14:00")).orElseThrow()

    assertVisitDetails(visit)
  }

  @Test
  fun testGetBookingVisitNextDifferentDay() {
    val visit = repository.getBookingVisitNext(-1L, LocalDateTime.parse("2016-12-10T17:00")).orElseThrow()

    assertVisitDetails(visit)
  }

  @Test
  fun testGetBookingVisitNextMultipleCandidates() {
    val visit = repository.getBookingVisitNext(-1L, LocalDateTime.parse("2017-11-12T00:00")).orElseThrow()

    assertThat(visit).isNotNull()
    assertThat(visit.startTime.toString()).isEqualTo("2017-11-13T14:30")
    assertThat(visit.endTime.toString()).isEqualTo("2017-11-13T15:30")
    assertThat(visit.leadVisitor).isNull()
    assertThat(visit.relationship).isNull()
  }

  @Test
  fun testGetBookingVisitNextNonexistentBooking() {
    val visit = repository.getBookingVisitNext(-99L, LocalDateTime.parse("2016-12-11T16:00:00"))

    assertThat(visit).isEmpty()
  }

  @Test
  fun testGetBookingVisitNextLateDate() {
    val visit = repository.getBookingVisitNext(-1L, LocalDateTime.parse("2021-12-11T16:00:00"))

    assertThat(visit).isEmpty()
  }

  @Test
  fun findBalancesForVisitOrdersAndPrivilegeVisitOrders() {
    val visitBalances = repository.getBookingVisitBalances(-1L)

    assertThat(visitBalances.get()).isEqualTo(
      VisitBalances.builder().remainingVo(25).remainingPvo(2).latestIepAdjustDate(LocalDate.parse("2021-09-22"))
        .latestPrivIepAdjustDate(
          LocalDate.parse("2021-10-22"),
        ).build(),
    )
  }

  @Test
  fun testGetBookingActivities() {
    val results = repository.getBookingActivities(-2L, LocalDate.parse("2011-12-11"), LocalDate.now(), null, null)

    assertThat(results).hasSize(8)
    assertThat(results).extracting("eventId", "payRate")
      .contains(Tuple(-11L, BigDecimal("1.000")))
    assertThat(results).extracting("locationCode").contains("CARP")
  }

  @Test
  fun testThatEventLocationIdIsPresent() {
    val results = repository.getBookingActivities(-2L, LocalDate.parse("2011-12-11"), LocalDate.now(), null, null)

    assertThat(results).hasSize(8)
    assertThat(results).extracting("eventId", "eventLocation", "eventLocationId")
      .contains(Tuple(-11L, "Carpentry Workshop", -26L))
  }

  @Test
  fun testGetLatestBookingByBookingIdInvalidBookingId() {
    val response = repository.getLatestBookingByBookingId(99999L)

    assertThat(response.isPresent).isFalse()
  }

  @Test
  fun testGetLatestBookingByBookingIdHavingActiveBooking() {
    val bookingIdForActiveBooking = -5L

    val response = repository.getLatestBookingByBookingId(bookingIdForActiveBooking)

    assertThat(response.isPresent).isTrue()

    val summary = response.get()

    assertThat(summary.offenderNo).isEqualTo("A1234AE")
    assertThat(summary.firstName).isEqualTo("DONALD")
    assertThat(summary.middleNames).isEqualTo("JEFFREY ROBERT")
    assertThat(summary.lastName).isEqualTo("MATTHEWS")
    assertThat(summary.bookingId).isEqualTo(bookingIdForActiveBooking)
    assertThat(summary.agencyLocationId).isEqualTo("LEI")
    assertThat(summary.currentlyInPrison).isEqualTo("Y")
  }

  @Test
  fun testGetLatestBookingByBookingIdHavingInactiveBooking() {
    val bookingIdForInactiveBooking = -20L

    val response = repository.getLatestBookingByBookingId(bookingIdForInactiveBooking)

    assertThat(response.isPresent).isTrue()

    val summary = response.get()

    assertThat(summary.offenderNo).isEqualTo("Z0020ZZ")
    assertThat(summary.firstName).isEqualTo("BURT")
    assertThat(summary.middleNames).isNullOrEmpty()
    assertThat(summary.lastName).isEqualTo("REYNOLDS")
    assertThat(summary.bookingId).isEqualTo(bookingIdForInactiveBooking)
    assertThat(summary.agencyLocationId).isEqualTo("OUT")
    assertThat(summary.currentlyInPrison).isEqualTo("N")
  }

  @Test
  fun testGetLatestBookingByBookingIdHavingLaterActiveBooking() {
    val bookingIdForInactiveBooking = -15L

    val response = repository.getLatestBookingByBookingId(bookingIdForInactiveBooking)

    assertThat(response.isPresent).isTrue()

    val summary = response.get()

    assertThat(summary.offenderNo).isEqualTo("A1234AI")
    assertThat(summary.firstName).isEqualTo("CHESTER")
    assertThat(summary.middleNames).isEqualTo("JAMES")
    assertThat(summary.lastName).isEqualTo("THOMPSON")
    assertThat(summary.bookingId).isNotEqualTo(bookingIdForInactiveBooking)
    assertThat(summary.agencyLocationId).isEqualTo("LEI")
    assertThat(summary.currentlyInPrison).isEqualTo("Y")
  }

  @Test
  fun testGetLatestBookingByOffenderNoInvalidOffenderNo() {
    val response = repository.getLatestBookingByOffenderNo("X9999XX")

    assertThat(response.isPresent).isFalse()
  }

  @Test
  fun testGetLatestBookingByOffenderNoHavingActiveBooking() {
    val offenderNoWithActiveBooking = "A1234AA"

    val response = repository.getLatestBookingByOffenderNo(offenderNoWithActiveBooking)

    assertThat(response.isPresent).isTrue()

    val summary = response.get()

    assertThat(summary.offenderNo).isEqualTo(offenderNoWithActiveBooking)
    assertThat(summary.firstName).isEqualTo("ARTHUR")
    assertThat(summary.middleNames).isEqualTo("BORIS")
    assertThat(summary.lastName).isEqualTo("ANDERSON")
    assertThat(summary.bookingId).isEqualTo(-1L)
    assertThat(summary.agencyLocationId).isEqualTo("LEI")
    assertThat(summary.currentlyInPrison).isEqualTo("Y")
  }

  @Test
  fun testGetLatestBookingByOffenderNoHavingInactiveBooking() {
    val offenderNoWithInactiveBooking = "Z0023ZZ"

    val response = repository.getLatestBookingByOffenderNo(offenderNoWithInactiveBooking)

    assertThat(response.isPresent).isTrue()

    val summary = response.get()

    assertThat(summary.offenderNo).isEqualTo(offenderNoWithInactiveBooking)
    assertThat(summary.firstName).isEqualTo("RICHARD")
    assertThat(summary.middleNames).isNullOrEmpty()
    assertThat(summary.lastName).isEqualTo("GRAYSON")
    assertThat(summary.bookingId).isEqualTo(-23L)
    assertThat(summary.agencyLocationId).isEqualTo("OUT")
    assertThat(summary.currentlyInPrison).isEqualTo("N")
  }

  @Test
  fun testUpdateAttendance() {
    val updateAttendance = UpdateAttendance.builder()
      .eventOutcome("Great")
      .performance("Poor")
      .outcomeComment("Hi there")
      .build()

    repository.updateAttendance(-3L, -1L, updateAttendance, true, true)

    val prisonerSchedules = scheduleRepository.getActivitiesAtLocation(-26L, null, null, null, null, false)
    val first = prisonerSchedules.first { it.eventId != null && it.eventId == -1L }
    assertThat(first.eventOutcome).isEqualTo("Great")
    assertThat(first.performance).isEqualTo("Poor")
    assertThat(first.outcomeComment).isEqualTo("Hi there")
    assertThat(first.paid).isTrue()
  }

  @Test
  fun testUpdateAttendanceInvalidActivityId() {
    val ua = UpdateAttendance.builder()
      .eventOutcome("Great")
      .build()
    try {
      repository.updateAttendance(-3L, -111L, ua, false, false)
      fail("No exception thrown")
    } catch (e: EntityNotFoundException) {
      assertThat(e.message).isEqualTo("Activity with booking Id -3 and activityId -111 not found")
    }
  }

  @Test
  fun testUpdateAttendanceInvalidBookingId() {
    val ua = UpdateAttendance.builder()
      .eventOutcome("Great")
      .build()
    try {
      repository.updateAttendance(-333L, -1L, ua, false, false)
      fail("No exception thrown")
    } catch (e: EntityNotFoundException) {
      assertThat(e.message).isEqualTo("Activity with booking Id -333 and activityId -1 not found")
    }
  }

  @Test
  fun testGetAttendanceEventDate() {
    assertThat(repository.getAttendanceEventDate(-1L)).isEqualTo("2017-09-11")
    assertThat(repository.getAttendanceEventDate(-2L)).isEqualTo("2017-09-12")
    assertThat(repository.getAttendanceEventDate(-3L)).isEqualTo("2017-09-13")
    assertThat(repository.getAttendanceEventDate(-4L)).isEqualTo("2017-09-14")
    assertThat(repository.getAttendanceEventDate(-5L)).isEqualTo("2017-09-15")
    assertThat(repository.getAttendanceEventDate(-101L)).isNull()
  }

  @Test
  fun testGetPayableAttendanceOutcomes() {
    assertThat(repository.getPayableAttendanceOutcome("PRISON_ACT", "NREQ"))
      .isEqualTo(
        PayableAttendanceOutcomeDto.builder()
          .payableAttendanceOutcomeId(23L)
          .eventType("PRISON_ACT")
          .outcomeCode("NREQ")
          .paid(true)
          .authorisedAbsence(false)
          .build(),
      )
    assertThat(repository.getPayableAttendanceOutcome("PRISON_ACT", "COURT"))
      .isEqualTo(
        PayableAttendanceOutcomeDto.builder()
          .payableAttendanceOutcomeId(77L)
          .eventType("PRISON_ACT")
          .outcomeCode("COURT")
          .paid(false)
          .authorisedAbsence(true)
          .build(),
      )
  }

  @Test
  fun testGetAlertCodesForBookingsFuture() {
    val resultsFuture = repository.getAlertCodesForBookings(
      listOf(-1L, -2L, -16L),
      LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(12, 0)),
    )

    assertThat(resultsFuture.get(-1L)).containsExactly("XA", "HC", "XTACT")
    assertThat(resultsFuture.get(-2L)).containsExactly("HA", "XTACT")
    assertThat(resultsFuture.get(-16L)).isNull()
  }

  @Test
  fun testGetAlertCodesForBookingsPast() {
    val resultsPast = repository.getAlertCodesForBookings(
      listOf(-1L, -2L, -16L),
      LocalDateTime.of(LocalDate.now().plusDays(-1), LocalTime.of(12, 0)),
    )

    assertThat(resultsPast.get(-16L)).containsExactly("OIOM")
  }

  @Test
  fun testGetAlertCodesForBookingsEmpty() {
    val resultsPast = repository.getAlertCodesForBookings(
      listOf<Long>(),
      LocalDateTime.now(),
    )

    assertThat(resultsPast).isEmpty()
  }

  @Test
  fun getOffenderSentenceCalculationsForPrisoner() {
    val sentenceCalculations = repository.getOffenderSentenceCalculationsForPrisoner("Z0024ZZ", true)
    assertThat(sentenceCalculations).isNotNull()
    assertThat(sentenceCalculations.size).isEqualTo(1)
    assertThat(sentenceCalculations.first().offenderNo).isEqualTo("Z0024ZZ")
    assertThat(sentenceCalculations.first().agencyLocationId).isEqualTo("LEI")
    assertThat(sentenceCalculations.first().calculationReason).isEqualTo("New Sentence")
    assertThat(sentenceCalculations.first().calculatedByUserId).isEqualTo("PRISON_API_USER")
    assertThat(sentenceCalculations.first().calculatedByFirstName).isEqualTo("PRISON")
    assertThat(sentenceCalculations.first().calculatedByLastName).isEqualTo("USER")
  }

  @Test
  fun getOffenderSentenceCalculationsForPrisoner_includingInactive() {
    val sentenceCalculations = repository.getOffenderSentenceCalculationsForPrisoner("Z0024ZZ", false)
    assertThat(sentenceCalculations).isNotNull()
    assertThat(sentenceCalculations.size).isEqualTo(1)
    assertThat(sentenceCalculations.first().offenderNo).isEqualTo("Z0024ZZ")
    assertThat(sentenceCalculations.first().agencyLocationId).isEqualTo("LEI")
    assertThat(sentenceCalculations.first().calculationReason).isEqualTo("New Sentence")
  }

  companion object {
    private fun assertVisitDetails(visitDetails: VisitDetails) {
      assertThat(visitDetails).isNotNull()

      assertThat(visitDetails.startTime.toString()).isEqualTo("2016-12-11T14:30")
      assertThat(visitDetails.endTime.toString()).isEqualTo("2016-12-11T15:30")
      assertThat(visitDetails.leadVisitor).isEqualTo("JESSY SMITH1")
      assertThat(visitDetails.relationship).isEqualTo("FRI")
      assertThat(visitDetails.relationshipDescription).isEqualTo("Friend")
      assertThat(visitDetails.location).isEqualTo("Visiting Room")
      assertThat(visitDetails.eventStatus).isEqualTo("CANC")
      assertThat(visitDetails.eventStatusDescription).isEqualTo("Cancelled")
      assertThat(visitDetails.visitType).isEqualTo("SCON")
      assertThat(visitDetails.visitTypeDescription).isEqualTo("Social Contact")
    }
  }
}
