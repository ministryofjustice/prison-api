@file:Suppress("TestFunctionName")

package uk.gov.justice.hmpps.prison.repository.jpa.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.ADM
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.REL
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TAP
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TRN
import java.time.LocalDate
import java.time.LocalDateTime

class OffenderBookingTest {
  companion object {
    private val ACTIVE_COURT_CASE: OffenderCourtCase = OffenderCourtCase.builder()
      .id(1L)
      .caseStatus(CaseStatus("A", "Active"))
      .build()
    private val INACTIVE_COURT_CASE: OffenderCourtCase = OffenderCourtCase.builder()
      .id(2L)
      .caseStatus(CaseStatus("I", "Inactive"))
      .build()
  }

  @Nested
  internal inner class Active {
    @Test
    fun isActive_is_not_active_by_default() {
      assertThat(OffenderBooking.builder().build().isActive).isFalse
    }

    @Test
    fun isActive_is_not_active_when_active_flag_n() {
      assertThat(OffenderBooking.builder().active(false).build().isActive).isFalse
    }

    @Test
    fun isActive_is_active_when_active_flag_y() {
      assertThat(OffenderBooking.builder().active(true).build().isActive).isTrue
    }

    @Test
    fun isActive_is_not_active_when_booking_sequence_not_set_to_one() {
      assertThat(OffenderBooking.builder().bookingSequence(2).build().isActive).isFalse
    }
  }

  @Nested
  internal inner class CourtCases {
    @Test
    fun courtCaseBy_empty_when_no_matching_case_id() {
      assertThat(OffenderBooking.builder().build().getCourtCaseBy(1L)).isEmpty
    }

    @Test
    fun courtCaseBy_returns_matching_case() {
      assertThat(
        OffenderBooking.builder().courtCases(listOf(ACTIVE_COURT_CASE)).build().getCourtCaseBy(
          ACTIVE_COURT_CASE.id,
        ),
      ).hasValue(ACTIVE_COURT_CASE)
    }

    @Test
    fun courtCases_returns_all_court_cases() {
      val booking = OffenderBooking.builder().courtCases(listOf(ACTIVE_COURT_CASE, INACTIVE_COURT_CASE)).build()
      assertThat(booking.courtCases).containsExactly(ACTIVE_COURT_CASE, INACTIVE_COURT_CASE)
    }

    @Test
    fun activeCourtCases_returns_active_cases_only() {
      val booking = OffenderBooking.builder().courtCases(listOf(ACTIVE_COURT_CASE, INACTIVE_COURT_CASE)).build()
      assertThat(booking.activeCourtCases).containsExactly(ACTIVE_COURT_CASE)
    }

    @Test
    fun returnsEmptyList_whenCourtCasesAreNull() {
      val booking = OffenderBooking.builder().build()
      assertThat(booking.activeCourtCases).isEqualTo(emptyList<Any>())
    }

    @Test
    fun handleNullCourtCasesEntries() {
      val courtCases = ArrayList<OffenderCourtCase?>()
      courtCases.add(ACTIVE_COURT_CASE)
      courtCases.add(INACTIVE_COURT_CASE)
      courtCases.add(null)
      val booking = OffenderBooking.builder().courtCases(courtCases).build()
      assertThat(booking.activeCourtCases).containsExactly(ACTIVE_COURT_CASE)
      assertThat(booking.getCourtCaseBy(9999L)).isEmpty
    }
  }

  @Nested
  internal inner class PrisonPeriod {
    @Test
    fun `test returns a list of prison periods`() {
      val offender = Offender.builder().nomsId("A1234AA").build().also { it.rootOffender = it }
      OffenderBooking.builder()
        .bookingId(12345L)
        .bookNumber("R1234K")
        .build().also {
          offender.addBooking(it)

          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("B", "Recall"))
              .movementDate(LocalDate.of(2019, 1, 4))
              .movementTime(LocalDateTime.of(2019, 1, 4, 9, 30))
              .toAgency(AgencyLocation.builder().id("WWI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("TRN", "Admission"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("NOTR", "Transfer"))
              .movementDate(LocalDate.of(2019, 1, 5))
              .movementTime(LocalDateTime.of(2019, 1, 5, 9, 30))
              .fromAgency(AgencyLocation.builder().id("WWI").build())
              .toAgency(AgencyLocation.builder().id("BXI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("INT", "Transfer"))
              .movementDate(LocalDate.of(2019, 1, 5))
              .movementTime(LocalDateTime.of(2019, 1, 5, 10, 30))
              .fromAgency(AgencyLocation.builder().id("WWI").build())
              .toAgency(AgencyLocation.builder().id("BXI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("REL", "Release"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("CR", "Conditional Release"))
              .movementDate(LocalDate.of(2019, 2, 28))
              .movementTime(LocalDateTime.of(2019, 2, 28, 15, 30))
              .fromAgency(AgencyLocation.builder().id("BXI").build())
              .build(),
          )
        }

      OffenderBooking.builder()
        .bookingId(12346L)
        .bookNumber("R1234T")
        .build().also {
          offender.addBooking(it)

          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("25", "Awaiting Sentence"))
              .movementDate(LocalDate.of(2020, 1, 4))
              .movementTime(LocalDateTime.of(2020, 1, 4, 9, 30))
              .toAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("TAP", "Temp Ab"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("C4", "Wedding"))
              .movementDate(LocalDate.of(2020, 1, 15))
              .movementTime(LocalDateTime.of(2020, 1, 15, 9, 30))
              .fromAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("TAP", "Temp Ab"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("C4", "Wedding"))
              .movementDate(LocalDate.of(2020, 1, 15))
              .movementTime(LocalDateTime.of(2020, 1, 15, 15, 30))
              .toAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("REL", "Release"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("BL", "Bailed"))
              .movementDate(LocalDate.of(2020, 2, 28))
              .movementTime(LocalDateTime.of(2020, 2, 28, 15, 30))
              .fromAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
        }
      OffenderBooking.builder()
        .bookingId(12347L)
        .bookNumber("R1234Q")
        .build().also {
          offender.addBooking(it)

          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("B", "Recall"))
              .movementDate(LocalDate.of(2021, 1, 4))
              .movementTime(LocalDateTime.of(2021, 1, 4, 9, 30))
              .toAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("CRT", "Court"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("CRT", "Court Appearance"))
              .movementDate(LocalDate.of(2021, 1, 15))
              .movementTime(LocalDateTime.of(2021, 1, 15, 9, 30))
              .fromAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("TRNCRT", "Transfer via Court"))
              .movementDate(LocalDate.of(2021, 1, 15))
              .movementTime(LocalDateTime.of(2021, 1, 15, 15, 30))
              .toAgency(AgencyLocation.builder().id("FSI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("REL", "Release"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("HP", "Hospital"))
              .movementDate(LocalDate.of(2021, 2, 28))
              .movementTime(LocalDateTime.of(2021, 2, 28, 15, 30))
              .fromAgency(AgencyLocation.builder().id("FSI").build())
              .build(),
          )
        }

      with(offender.prisonerInPrisonSummary) {
        assertThat(prisonPeriod).hasSize(3)

        assertThat(prisonPeriod).extracting<String> { it.bookNumber }.containsExactly("R1234K", "R1234T", "R1234Q")
        assertThat(prisonPeriod).extracting<Int> { it.movementDates.size }.containsExactly(1, 2, 1)
        assertThat(prisonPeriod).extracting<List<String>> { it.prisons }.containsExactly(listOf("WWI", "BXI"), listOf("MDI"), listOf("MDI", "FSI"))

        assertThat(prisonPeriod[0].entryDate).isEqualTo(LocalDateTime.of(2019, 1, 4, 9, 30))
        assertThat(prisonPeriod[0].releaseDate).isEqualTo(LocalDateTime.of(2019, 2, 28, 15, 30))
        assertThat(prisonPeriod[0].movementDates[0].admittedIntoPrisonId).isEqualTo("WWI")
        assertThat(prisonPeriod[0].movementDates[0].releaseFromPrisonId).isEqualTo("BXI")
        assertThat(prisonPeriod[1].entryDate).isEqualTo(LocalDateTime.of(2020, 1, 4, 9, 30))
        assertThat(prisonPeriod[1].releaseDate).isEqualTo(LocalDateTime.of(2020, 2, 28, 15, 30))
        assertThat(prisonPeriod[1].movementDates[0].admittedIntoPrisonId).isEqualTo("MDI")
        assertThat(prisonPeriod[1].movementDates[0].releaseFromPrisonId).isEqualTo("MDI")
        assertThat(prisonPeriod[2].entryDate).isEqualTo(LocalDateTime.of(2021, 1, 4, 9, 30))
        assertThat(prisonPeriod[2].releaseDate).isEqualTo(LocalDateTime.of(2021, 2, 28, 15, 30))
        assertThat(prisonPeriod[2].movementDates[0].admittedIntoPrisonId).isEqualTo("MDI")
        assertThat(prisonPeriod[2].movementDates[0].releaseFromPrisonId).isEqualTo("FSI")
      }
    }

    @Test
    fun `cope with bad data and no admission`() {
      val offender = Offender.builder().nomsId("A1234AA").build().also { it.rootOffender = it }

      OffenderBooking.builder()
        .bookingId(12345L)
        .bookNumber("R1234K")
        .build().also {
          offender.addBooking(it)

          // booking has a single release - no admission
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("REL", "Release"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("CR", "Conditional Release"))
              .movementDate(LocalDate.of(2019, 2, 28))
              .movementTime(LocalDateTime.parse("2019-02-28T15:30"))
              .build(),
          )
        }
      OffenderBooking.builder()
        .bookingId(22345L)
        .bookNumber("R2234K")
        .build().also {
          offender.addBooking(it)

          // booking has a single release - no admission
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("REL", "Release"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("CR", "Conditional Release"))
              .movementDate(LocalDate.of(2019, 2, 28))
              .movementTime(LocalDateTime.parse("2019-02-28T16:30"))
              .build(),
          )
        }

      with(offender.prisonerInPrisonSummary) {
        assertThat(prisonPeriod).hasSize(2)
        assertThat(prisonPeriod).extracting<String> { it.bookNumber }.containsExactly("R1234K", "R2234K")
        assertThat(prisonPeriod[0].movementDates).hasSize(1)
        assertThat(prisonPeriod[0].entryDate).isEqualTo(LocalDateTime.parse("2019-02-28T15:30"))
        assertThat(prisonPeriod[0].releaseDate).isEqualTo(LocalDateTime.parse("2019-02-28T15:30"))
      }
    }

    @Test
    fun `cope with bad data and admission not as first record`() {
      val offender = Offender.builder().nomsId("A1234AA").build().also { it.rootOffender = it }
      OffenderBooking.builder()
        .bookingId(12345L)
        .bookNumber("R1234K")
        .build().also {
          offender.addBooking(it)

          // first movement is a temporary absence
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("TAP", "Temp Ab"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("C4", "Wedding"))
              .movementDate(LocalDate.of(2019, 1, 3))
              .movementTime(LocalDateTime.parse("2019-01-03T09:30"))
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("TAP", "Temp Ab"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("C4", "Wedding"))
              .movementDate(LocalDate.of(2019, 1, 3))
              .movementTime(LocalDateTime.parse("2019-01-03T14:30"))
              .build(),
          )
          // this movement is the admission so expect the start date for this one
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("B", "Recall"))
              .movementDate(LocalDate.of(2019, 1, 4))
              .movementTime(LocalDateTime.parse("2019-01-04T09:30"))
              .toAgency(AgencyLocation.builder().id("WWI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("REL", "Release"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("CR", "Conditional Release"))
              .movementDate(LocalDate.of(2019, 2, 28))
              .movementTime(LocalDateTime.parse("2019-02-28T15:30"))
              .build(),
          )
        }
      OffenderBooking.builder()
        .bookingId(12346L)
        .bookNumber("R1234T")
        .build().also {
          offender.addBooking(it)

          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("25", "Awaiting Sentence"))
              .movementDate(LocalDate.of(2020, 1, 4))
              .movementTime(LocalDateTime.parse("2020-01-04T09:30"))
              .toAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
        }

      with(offender.prisonerInPrisonSummary) {
        assertThat(prisonPeriod).hasSize(2)
        assertThat(prisonPeriod).extracting<String> { it.bookNumber }.containsExactly("R1234K", "R1234T")
        assertThat(prisonPeriod).extracting<Int> { it.movementDates.size }.containsExactly(2, 1)
        assertThat(prisonPeriod[0].entryDate).isEqualTo(LocalDateTime.parse("2019-01-04T09:30"))
        assertThat(prisonPeriod[0].releaseDate).isEqualTo(LocalDateTime.parse("2019-02-28T15:30"))
        assertThat(prisonPeriod[1].entryDate).isEqualTo(LocalDateTime.parse("2020-01-04T09:30"))
        assertThat(prisonPeriod[1].releaseDate).isNull()
      }
    }

    @Test
    fun `prison periods include transferred prisons`() {
      val offender = Offender.builder().nomsId("A1234AA").build().also { it.rootOffender = it }

      OffenderBooking.builder()
        .bookingId(12345L)
        .bookNumber("R1234K")
        .build().also {
          offender.addBooking(it)

          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("B", "Recall"))
              .movementDate(LocalDate.of(2019, 1, 4))
              .movementTime(LocalDateTime.of(2019, 1, 4, 9, 30))
              .toAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("TRN", "Transfer"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("NOTR", "Transfer"))
              .movementDate(LocalDate.of(2019, 1, 5))
              .movementTime(LocalDateTime.of(2019, 1, 5, 12, 15))
              .fromAgency(AgencyLocation.builder().id("MDI").build())
              .toAgency(AgencyLocation.builder().id("WWI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("INT", "Transfer"))
              .movementDate(LocalDate.of(2019, 1, 7))
              .movementTime(LocalDateTime.of(2019, 1, 7, 10, 0))
              .fromAgency(AgencyLocation.builder().id("MDI").build())
              .toAgency(AgencyLocation.builder().id("WWI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("REL", "Release"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("CR", "Conditional Release"))
              .movementDate(LocalDate.of(2019, 2, 28))
              .movementTime(LocalDateTime.of(2019, 2, 28, 15, 30))
              .fromAgency(AgencyLocation.builder().id("WWI").build())
              .toAgency(AgencyLocation.builder().id("OUT").build())
              .build(),
          )
        }

      with(offender.prisonerInPrisonSummary) {
        assertThat(prisonPeriod[0].bookNumber).isEqualTo("R1234K")
        assertThat(prisonPeriod[0].movementDates).hasSize(1)
        assertThat(prisonPeriod[0].entryDate).isEqualTo(LocalDateTime.of(2019, 1, 4, 9, 30))
        assertThat(prisonPeriod[0].releaseDate).isEqualTo(LocalDateTime.of(2019, 2, 28, 15, 30))
        assertThat(prisonPeriod[0].prisons).containsExactly("MDI", "WWI")
      }
    }

    @Test
    fun `handles TAPs with dates out of sync`() {
      val offender = Offender.builder().nomsId("A1234AA").build().also { it.rootOffender = it }

      OffenderBooking.builder()
        .bookingId(54321L)
        .bookNumber("R4312K")
        .build().also {
          offender.addBooking(it)

          // A straight forward booking and release - needed to have >1 bookings to trigger the comparator
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("I", "In"))
              .movementDate(LocalDate.of(2018, 1, 4))
              .movementTime(LocalDateTime.of(2018, 1, 4, 9, 30))
              .toAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("REL", "Release"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("AR", "Actual Release"))
              .movementDate(LocalDate.of(2018, 2, 28))
              .movementTime(LocalDateTime.of(2018, 2, 28, 15, 30))
              .fromAgency(AgencyLocation.builder().id("MDI").build())
              .toAgency(AgencyLocation.builder().id("OUT").build())
              .build(),
          )
        }

      OffenderBooking.builder()
        .bookingId(12345L)
        .bookNumber("R1234K")
        .build().also {
          offender.addBooking(it)

          // This booking has the TAP dates out of order - as seen in real data
          // Previously this caused null PrisonPeriod.entryDate which blows up the comparator at the end of OffenderBooking#getPrisonerInPrisonSummary
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("ADM", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("I", "In"))
              .movementDate(LocalDate.of(2019, 1, 4))
              .movementTime(LocalDateTime.of(2019, 1, 4, 9, 30))
              .toAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("TAP", "Temporary Absence"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("C5", "C5"))
              .movementDate(LocalDate.of(2019, 1, 7))
              .movementTime(LocalDateTime.of(2019, 1, 7, 12, 15))
              .fromAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("TAP", "Admission"))
              .movementDirection(MovementDirection.IN)
              .movementReason(MovementReason("C5", "C5"))
              .movementDate(LocalDate.of(2019, 1, 5))
              .movementTime(LocalDateTime.of(2019, 1, 5, 10, 0))
              .toAgency(AgencyLocation.builder().id("MDI").build())
              .build(),
          )
          it.addExternalMovement(
            ExternalMovement.builder()
              .movementType(MovementType("REL", "Release"))
              .movementDirection(MovementDirection.OUT)
              .movementReason(MovementReason("AR", "Actual Release"))
              .movementDate(LocalDate.of(2019, 2, 28))
              .movementTime(LocalDateTime.of(2019, 2, 28, 15, 30))
              .fromAgency(AgencyLocation.builder().id("MDI").build())
              .toAgency(AgencyLocation.builder().id("OUT").build())
              .build(),
          )
        }

      with(offender.prisonerInPrisonSummary) {
        assertThat(prisonPeriod[1].entryDate).isNotNull
        assertThat(prisonPeriod[1].releaseDate).isNotNull
      }
    }
  }

  @Nested
  internal inner class Location {
    private val outside = AgencyLocation.builder().id("OUT").description("OUTSIDE").build()
    private val moorland = AgencyLocation.builder().id("MDI").description("MOORLAND (HMP & YOI)").build()
    private val brixton = AgencyLocation.builder().id("BXI").description("BRIXTON (HMP)").build()
    private val transfer = AgencyLocation.builder().id("TRN").description("TRANSFER").build()
    private val cellInMoorland = AgencyInternalLocation.builder().location(moorland).build()

    @Test
    fun `IN should be agency description`() {
      val booking = OffenderBooking.builder()
        .inOutStatus("IN")
        .statusReason("ADM-INT")
        .location(moorland)
        .assignedLivingUnit(cellInMoorland)
        .externalMovements(
          listOf(
            ExternalMovement.builder()
              .movementDirection(MovementDirection.IN)
              .movementType(MovementType.of(ADM))
              .toAgency(moorland)
              .build(),
          ),
        )
        .build()

      assertThat(booking.locationDescription).isEqualTo("Moorland (HMP & YOI)")
      assertThat(booking.latestLocationId).isEqualTo("MDI")
    }

    @Test
    fun `OUT and released should be Outside - released from`() {
      val booking = OffenderBooking.builder()
        .inOutStatus("OUT")
        .statusReason("REL-CR")
        .location(outside)
        .assignedLivingUnit(null)
        .externalMovements(
          listOf(
            ExternalMovement.builder()
              .movementDirection(MovementDirection.OUT)
              .movementType(MovementType.of(REL))
              .fromAgency(moorland)
              .build(),
          ),
        )
        .build()

      assertThat(booking.locationDescription).isEqualTo("Outside - released from Moorland (HMP & YOI)")
      assertThat(booking.latestLocationId).isEqualTo("MDI")
    }

    @Test
    fun `OUT and last movement not from an agency should be outside`() {
      val booking = OffenderBooking.builder()
        .inOutStatus("OUT")
        .statusReason("REL-CR")
        .location(outside)
        .assignedLivingUnit(null)
        .externalMovements(
          listOf(
            ExternalMovement.builder()
              .movementDirection(MovementDirection.OUT)
              .movementType(MovementType.of(REL))
              .fromAgency(null)
              .build(),
          ),
        )
        .assignedLivingUnit(AgencyInternalLocation.builder().description("Outside").build())
        .build()

      assertThat(booking.locationDescription).isEqualTo("Outside")
      assertThat(booking.latestLocationId).isEqualTo("OUT")
    }

    @Test
    fun `OUT on a TAP should be agency description`() {
      val booking = OffenderBooking.builder()
        .inOutStatus("OUT")
        .statusReason("TAP-ELR")
        .location(moorland)
        .assignedLivingUnit(cellInMoorland)
        .externalMovements(
          listOf(
            ExternalMovement.builder()
              .movementDirection(MovementDirection.OUT)
              .movementType(MovementType.of(TAP))
              .fromAgency(moorland)
              .build(),
          ),
        )
        .build()

      assertThat(booking.locationDescription).isEqualTo("Moorland (HMP & YOI)")
      assertThat(booking.latestLocationId).isEqualTo("MDI")
    }

    @Test
    fun `TRN should be transfer`() {
      val booking = OffenderBooking.builder()
        .inOutStatus("TRN")
        .statusReason("TRN-NOTR")
        .location(transfer)
        .assignedLivingUnit(null)
        .externalMovements(
          listOf(
            ExternalMovement.builder()
              .movementDirection(MovementDirection.OUT)
              .movementType(MovementType.of(TRN))
              .fromAgency(moorland)
              .toAgency(brixton)
              .build(),
          ),
        )
        .build()

      assertThat(booking.locationDescription).isEqualTo("Transfer")
      assertThat(booking.latestLocationId).isEqualTo("MDI")
    }

    @Test
    fun `TRN but status doesn't match last movement should be outside, movement type description`() {
      val booking = OffenderBooking.builder()
        .inOutStatus("TRN")
        .statusReason("REL-NG")
        .location(transfer)
        .assignedLivingUnit(null)
        .externalMovements(
          listOf(
            ExternalMovement.builder()
              .movementDirection(MovementDirection.OUT)
              .movementType(MovementType("TRN", "Transfers"))
              .fromAgency(moorland)
              .toAgency(brixton)
              .build(),
          ),
        )
        .build()

      assertThat(booking.locationDescription).isEqualTo("Outside - Transfers")
      assertThat(booking.latestLocationId).isEqualTo("MDI")
    }
  }
}
