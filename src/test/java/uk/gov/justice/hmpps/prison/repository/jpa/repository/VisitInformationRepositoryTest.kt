@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
class VisitInformationRepositoryTest(
  @Autowired private val repository: VisitInformationRepository,
) {
  @Test
  fun findAll() {
    val pageable = PageRequest.of(0, 20)
    val visits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).build(), pageable)

    assertThat(visits).hasSize(15)
    assertThat(visits)
      .extracting<Long, RuntimeException> { it.visitId }
      .containsExactly(-3L, -2L, -4L, -5L, -1L, -6L, -8L, -7L, -10L, -9L, -13L, -14L, -12L, -11L, -15L)
    assertThat(visits)
      .extracting<String, RuntimeException> { it.cancellationReason }
      .containsExactly(
        "NSHOW",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "VISCANC",
        null,
        null,
        null,
        null,
        null,
        "NSHOW",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.cancelReasonDescription }
      .containsExactly(
        "Visitor Did Not Arrive",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "Visitor Cancelled",
        null,
        null,
        null,
        null,
        null,
        "Visitor Did Not Arrive",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.eventStatus }
      .containsExactly(
        "CANC",
        "SCH",
        "SCH",
        "SCH",
        "SCH",
        "SCH",
        "EXP",
        "SCH",
        "CANC",
        "COMP",
        "SCH",
        "SCH",
        "SCH",
        "SCH",
        "CANC",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.eventStatusDescription }
      .containsExactly(
        "Cancelled",
        "Scheduled (Approved)",
        "Scheduled (Approved)",
        "Scheduled (Approved)",
        "Scheduled (Approved)",
        "Scheduled (Approved)",
        "Expired",
        "Scheduled (Approved)",
        "Cancelled",
        "Completed",
        "Scheduled (Approved)",
        "Scheduled (Approved)",
        "Scheduled (Approved)",
        "Scheduled (Approved)",
        "Cancelled",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.eventOutcome }
      .containsExactly(
        "ABS",
        "ATT",
        "ATT",
        "ATT",
        "ATT",
        "ATT",
        "ATT",
        "ATT",
        "ABS",
        "ATT",
        "ATT",
        "ATT",
        "ATT",
        "ATT",
        "ABS",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.eventOutcomeDescription }
      .containsExactly(
        "Absence",
        "Attended",
        "Attended",
        "Attended",
        "Attended",
        "Attended",
        "Attended",
        "Attended",
        "Absence",
        "Attended",
        "Attended",
        "Attended",
        "Attended",
        "Attended",
        "Absence",
      )
    assertThat(visits)
      .extracting<LocalDateTime, RuntimeException> { it.startTime }
      .containsExactly(
        LocalDateTime.parse("2017-12-10T14:30"),
        LocalDateTime.parse("2017-11-13T14:30"),
        LocalDateTime.parse("2017-10-13T14:30"),
        LocalDateTime.parse("2017-09-15T14:00"),
        LocalDateTime.parse("2017-09-12T14:30"),
        LocalDateTime.parse("2017-09-10T14:30"),
        LocalDateTime.parse("2017-08-10T14:30"),
        LocalDateTime.parse("2017-07-10T14:30"),
        LocalDateTime.parse("2017-06-10T14:30"),
        LocalDateTime.parse("2017-05-10T14:30"),
        LocalDateTime.parse("2017-04-10T14:30"),
        LocalDateTime.parse("2017-03-10T14:30"),
        LocalDateTime.parse("2017-02-10T14:30"),
        LocalDateTime.parse("2017-01-10T14:30"),
        LocalDateTime.parse("2016-12-11T14:30"),
      )
    assertThat(visits)
      .extracting<LocalDateTime, RuntimeException> { it.endTime }
      .containsExactly(
        LocalDateTime.parse("2017-12-10T15:30"),
        LocalDateTime.parse("2017-11-13T15:30"),
        LocalDateTime.parse("2017-10-13T15:30"),
        LocalDateTime.parse("2017-09-15T16:00"),
        LocalDateTime.parse("2017-09-12T15:30"),
        LocalDateTime.parse("2017-09-10T15:30"),
        LocalDateTime.parse("2017-08-10T15:30"),
        LocalDateTime.parse("2017-07-10T15:30"),
        LocalDateTime.parse("2017-06-10T15:30"),
        LocalDateTime.parse("2017-05-10T16:30"),
        LocalDateTime.parse("2017-04-10T15:30"),
        LocalDateTime.parse("2017-03-10T16:30"),
        LocalDateTime.parse("2017-02-10T15:30"),
        LocalDateTime.parse("2017-01-10T15:30"),
        LocalDateTime.parse("2016-12-11T15:30"),
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.location }
      .containsExactly(
        "Visiting Room",
        "Visiting Room",
        "Visiting Room",
        "Chapel",
        "Visiting Room",
        "Visiting Room",
        "Visiting Room",
        "Visiting Room",
        "Visiting Room",
        "Chapel",
        "Visiting Room",
        "Chapel",
        "Visiting Room",
        "Visiting Room",
        "Visiting Room",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.visitType }
      .containsExactly(
        "SCON",
        "SCON",
        "SCON",
        "OFFI",
        "SCON",
        "SCON",
        "SCON",
        "SCON",
        "SCON",
        "OFFI",
        "SCON",
        "OFFI",
        "SCON",
        "SCON",
        "SCON",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.visitTypeDescription }
      .containsExactly(
        "Social Contact",
        "Social Contact",
        "Social Contact",
        "Official Visit",
        "Social Contact",
        "Social Contact",
        "Social Contact",
        "Social Contact",
        "Social Contact",
        "Official Visit",
        "Social Contact",
        "Official Visit",
        "Social Contact",
        "Social Contact",
        "Social Contact",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.leadVisitor }
      .containsExactly("JESSY SMITH1", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "JESSY SMITH1")
    assertThat(visits)
      .extracting<String, RuntimeException> { it.visitStatus }
      .containsExactly(
        "SCH",
        "SCH",
        "SCH",
        "SCH",
        "SCH",
        "SCH",
        "SCH",
        "SCH",
        "CANC",
        "VDE",
        "SCH",
        "SCH",
        "SCH",
        "SCH",
        "SCH",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.visitStatusDescription }
      .containsExactly(
        "Scheduled",
        "Scheduled",
        "Scheduled",
        "Scheduled",
        "Scheduled",
        "Scheduled",
        "Scheduled",
        "Scheduled",
        "Cancelled",
        "Visitor Declined Entry",
        "Scheduled",
        "Scheduled",
        "Scheduled",
        "Scheduled",
        "Scheduled",
      )
    assertThat(visits)
      .extracting<String, RuntimeException> { it.searchType }
      .containsExactly(null, null, null, null, null, null, null, null, null, null, null, null, null, "RUB_A", null)
    assertThat(visits)
      .extracting<String, RuntimeException> { it.searchTypeDescription }
      .containsExactly(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "Rubdown Level A",
        null,
      )
  }

  @Test
  fun findAll_filterByType() {
    val pageable = PageRequest.of(0, 20)
    val visits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).visitType("OFFI").build(), pageable)

    assertThat(visits).hasSize(3)
    assertThat(visits)
      .extracting<Long, RuntimeException> { it.visitId }
      .containsExactly(-5L, -9L, -14L)
    assertThat(visits)
      .extracting<String, RuntimeException> { it.eventOutcomeDescription }
      .containsExactly("Attended", "Attended", "Attended")
    assertThat(visits)
      .extracting<String, RuntimeException> { it.visitType }
      .allMatch { it == "OFFI" }
  }

  @Test
  fun findAll_filterByDates() {
    val pageable = PageRequest.of(0, 20)
    val visits = repository.findAll(
      VisitInformationFilter.builder().bookingId(-1L).fromDate(
        LocalDate.of(2017, 9, 1),
      ).toDate(LocalDate.of(2017, 10, 1)).build(),
      pageable,
    )

    assertThat(visits).hasSize(3)
    assertThat(visits)
      .extracting<Long, RuntimeException> { it.visitId }
      .containsExactly(-5L, -1L, -6L)
    assertThat(visits)
      .extracting<String, RuntimeException> { it.eventOutcome }
      .containsExactly("ATT", "ATT", "ATT")
    assertThat(visits)
      .extracting<String, RuntimeException> { it.eventOutcomeDescription }
      .containsExactly("Attended", "Attended", "Attended")
    assertThat(visits)
      .extracting<String, RuntimeException> { it.visitType }
      .containsExactly("OFFI", "SCON", "SCON")
    assertThat(visits)
      .extracting<String, RuntimeException> { it.visitTypeDescription }
      .containsExactly("Official Visit", "Social Contact", "Social Contact")
  }

  @Test
  fun findAll_filterByStatus() {
    val pageable = PageRequest.of(0, 20)

    val scheduledVisits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).visitStatus("SCH").build(), pageable)
    assertThat(scheduledVisits).hasSize(13)
    assertThat(scheduledVisits)
      .extracting<String, RuntimeException> { it.visitStatus }
      .allMatch { it == "SCH" }

    val cancelledVisits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).visitStatus("CANC").build(), pageable)
    assertThat(cancelledVisits).hasSize(1)
    assertThat(cancelledVisits)
      .extracting<String, RuntimeException> { it.visitStatus }
      .allMatch { it == "CANC" }
  }

  @Test
  fun findAll_filterByAgency() {
    val pageable = PageRequest.of(0, 20)

    val leicesterVisits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).prisonId("LEI").build(), pageable)
    assertThat(leicesterVisits).hasSize(13)
    assertThat(leicesterVisits)
      .extracting<String, RuntimeException> { it.prisonDescription }
      .allMatch { it == "LEEDS" }

    val moorlandVisits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).prisonId("MDI").build(), pageable)
    assertThat(moorlandVisits).hasSize(1)
    assertThat(moorlandVisits)
      .extracting<String, RuntimeException> { it.prisonDescription }
      .allMatch { it == "MOORLAND" }
  }

  @Test
  fun findByBookingIdGroupByPrisonId() {
    val prisons = repository.findByBookingIdGroupByPrisonId(-1)
    assertThat(prisons)
      .extracting<String, RuntimeException> { it.getPrisonId() }
      .containsExactly("LEI", "MDI", "BXI")
    assertThat(prisons)
      .extracting<String, RuntimeException> { it.getPrisonDescription() }
      .containsExactly("LEEDS", "MOORLAND", "BRIXTON")
  }

  @Nested
  inner class countByBookingId {
    @Test
    fun countByBookingId() {
      val visits = repository.countByBookingId(-1L)
      assertThat(visits).isEqualTo(15)
    }

    @Test
    fun countByBookingId_notfound() {
      val visits = repository.countByBookingId(-12345L)
      assertThat(visits).isEqualTo(0)
    }

    @Test
    fun countByBookingId_other() {
      val visits = repository.countByBookingId(-2L)
      assertThat(visits).isEqualTo(1)
    }
  }
}
