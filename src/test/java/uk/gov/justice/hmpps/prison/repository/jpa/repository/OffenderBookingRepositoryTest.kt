@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.iterable.ThrowingExtractor
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPropertyContainer
import uk.gov.justice.hmpps.prison.repository.jpa.model.PropertyContainer
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.LocalDate
import java.util.*

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
@WithMockUser
class OffenderBookingRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderBookingRepository

  @Autowired
  private lateinit var agencyLocationRepository: AgencyLocationRepository

  @Autowired
  private lateinit var militaryBranchRepository: ReferenceCodeRepository<MilitaryBranch>

  @Autowired
  private lateinit var disciplinaryActionRepository: ReferenceCodeRepository<DisciplinaryAction>

  @Autowired
  private lateinit var agencyInternalLocationRepository: AgencyInternalLocationRepository

  @Autowired
  private lateinit var entityManager: TestEntityManager

  @Test
  fun militaryRecords() {
    val booking = repository.findById(-1L).orElseThrow()
    assertThat(booking.militaryRecords).containsExactly(
      OffenderMilitaryRecord.builder()
        .bookingAndSequence(BookingAndSequence(booking, 1))
        .startDate(LocalDate.parse("2000-01-01"))
        .endDate(LocalDate.parse("2020-10-17"))
        .militaryDischarge(MilitaryDischarge("DIS", "Dishonourable"))
        .warZone(WarZone("AFG", "Afghanistan"))
        .militaryBranch(MilitaryBranch("ARM", "Army"))
        .description("Some Description Text")
        .unitNumber("auno")
        .enlistmentLocation("Somewhere")
        .militaryRank(MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"))
        .serviceNumber("asno")
        .disciplinaryAction(DisciplinaryAction("CM", "Court Martial"))
        .dischargeLocation("Sheffield")
        .build(),
      OffenderMilitaryRecord.builder()
        .bookingAndSequence(BookingAndSequence(booking, 2))
        .startDate(LocalDate.parse("2001-01-01"))
        .militaryBranch(MilitaryBranch("NAV", "Navy"))
        .description("second record")
        .build(),
    )
  }

  @Test
  fun saveMilitaryRecords() {
    val booking = repository.findById(-2L).orElseThrow()
    val militaryRecords = booking.militaryRecords
    assertThat(militaryRecords).isEmpty()
    booking.add(
      OffenderMilitaryRecord.builder()
        .startDate(LocalDate.parse("2000-01-01"))
        .militaryBranch(militaryBranchRepository.findById(MilitaryBranch.ARMY).orElseThrow())
        .dischargeLocation("Somewhere")
        .disciplinaryAction(disciplinaryActionRepository.findById(DisciplinaryAction.COURT_MARTIAL).orElseThrow())
        .build(),
    )
    repository.save(booking)
    entityManager.flush()
    val persistedBooking = repository.findById(-2L).orElseThrow()
    assertThat(persistedBooking.militaryRecords).containsExactly(
      OffenderMilitaryRecord.builder()
        .bookingAndSequence(BookingAndSequence(booking, 1))
        .startDate(LocalDate.parse("2000-01-01"))
        .militaryBranch(militaryBranchRepository.findById(MilitaryBranch.ARMY).orElseThrow())
        .dischargeLocation("Somewhere")
        .disciplinaryAction(DisciplinaryAction("CM", "Court Martial"))
        .build(),
    )

    // also check auditing
    assertThat(persistedBooking.militaryRecords[0]).extracting("createUserId").isEqualTo("user")
  }

  @Test
  fun saveMultipleMilitaryRecords() {
    val booking = repository.findById(-3L).orElseThrow()
    val militaryRecords = booking.militaryRecords
    assertThat(militaryRecords).isEmpty()
    booking.add(
      OffenderMilitaryRecord.builder()
        .startDate(LocalDate.parse("2000-01-01"))
        .militaryBranch(militaryBranchRepository.findById(MilitaryBranch.ARMY).orElseThrow())
        .description("First record")
        .build(),
    )
    booking.add(
      OffenderMilitaryRecord.builder()
        .startDate(LocalDate.parse("2000-01-01"))
        .militaryBranch(militaryBranchRepository.findById(MilitaryBranch.ARMY).orElseThrow())
        .description("Second record")
        .build(),
    )
    repository.save(booking)
    entityManager.flush()
    val persistedBooking = repository.findById(-3L).orElseThrow()
    assertThat(persistedBooking.militaryRecords)
      .extracting<String, RuntimeException> { obj: OffenderMilitaryRecord -> obj.description }
      .containsExactly("First record", "Second record")
  }

  @Test
  fun offendersCourtCase() {
    assertThat(repository.findById(-1L).orElseThrow().courtCases).flatExtracting(
      ThrowingExtractor<OffenderCourtCase, Any, RuntimeException> { it.id },
      ThrowingExtractor<OffenderCourtCase, Any, RuntimeException> { it.caseStatus },
      ThrowingExtractor<OffenderCourtCase, Any, RuntimeException> { it.legalCaseType },
      ThrowingExtractor<OffenderCourtCase, Any, RuntimeException> { it.agencyLocation },
    )
      .containsExactly(
        -1L,
        Optional.of(CaseStatus("A", "Active")),
        Optional.of(LegalCaseType("A", "Adult")),
        AgencyLocation.builder()
          .id("COURT1")
          .description("Court 1")
          .type(AgencyLocationType.COURT_TYPE)
          .active(true)
          .build(),
      )
  }

  @Test
  fun saveOffenderCourtCases() {
    val booking = repository.findById(-2L).orElseThrow()
    assertThat(booking.courtCases)
      .extracting<Int> { it.caseSeq }
      .containsOnly(1)
    booking.add(offenderCourtCase(2))
    booking.add(offenderCourtCase(3))
    repository.save(booking)
    entityManager.flush()
    val persistedBooking = repository.findById(-2L).orElseThrow()
    assertThat(persistedBooking.courtCases)
      .extracting<Int> { it.caseSeq }
      .contains(1, 2, 3)
  }

  private fun offenderCourtCase(sequence: Int): OffenderCourtCase = OffenderCourtCase.builder()
    .beginDate(LocalDate.EPOCH)
    .caseSeq(sequence)
    .agencyLocation(agencyLocationRepository.findById("COURT1").orElseThrow())
    .build()

  @Test
  fun updateLivingUnit() {
    val offenderBooking = repository.findById(-1L).orElseThrow()
    val livingUnit = agencyInternalLocationRepository.findById(-4L).orElseThrow()
    offenderBooking.assignedLivingUnit = livingUnit
    repository.save(offenderBooking)
    entityManager.flush()
    val result = repository.findById(-1L).orElseThrow()
    assertThat(result.assignedLivingUnit.locationId).isEqualTo(-4L)
  }

  @Test
  fun offenderPropertyContainers() {
    val parentParentLocation = AgencyInternalLocation.builder().locationId(-1L).locationType("WING").agencyId("LEI")
      .currentOccupancy(null).operationalCapacity(13).description("LEI-A").userDescription("Block A").capacity(14)
      .certifiedFlag(true).locationCode("A").active(true).build()
    AgencyInternalLocation.builder().locationId(-2L).locationType("LAND").agencyId("LEI").capacity(14)
      .currentOccupancy(null).operationalCapacity(13).description("LEI-A-1").parentLocation(parentParentLocation)
      .userDescription("Landing A/1")
      .certifiedFlag(true).locationCode("1").active(true).build()
    assertThat(repository.findById(-1L).orElseThrow().propertyContainers).flatExtracting(
      ThrowingExtractor<OffenderPropertyContainer, Any, RuntimeException> { it.containerId },
      ThrowingExtractor<OffenderPropertyContainer, Any, RuntimeException> { it.sealMark },
      ThrowingExtractor<OffenderPropertyContainer, Any, RuntimeException> { it.internalLocation },
      ThrowingExtractor<OffenderPropertyContainer, Any, RuntimeException> { it.isActive },
      ThrowingExtractor<OffenderPropertyContainer, Any, RuntimeException> { it.containerType },
    )
      .containsAnyOf(
        -1L,
        "TEST10",
        AgencyInternalLocation.builder()
          .locationId(-10L)
          .active(true)
          .certifiedFlag(true)
          .locationType("CELL")
          .agencyId("LEI")
          .description("LEI-A-1-8")
          .userDescription(null)
          .locationCode("8")
          .build(),
        "Y",
        PropertyContainer("BULK", "Bulk"),
      )
  }

  @Nested
  inner class findByOffenderNomsIdAndActive {
    @Test
    fun `test find active booking`() {
      val optionalOffenderBooking = repository.findByOffenderNomsIdAndActive("A1234AA", true)
      assertThat(optionalOffenderBooking).get().extracting(
        { it.bookingId },
        { it.rootOffender.id },
      ).containsExactly(-1L, -1001L)
    }

    @Test
    fun `test find inactive booking`() {
      val optionalOffenderBooking = repository.findByOffenderNomsIdAndActive("A1234AA", false)
      assertThat(optionalOffenderBooking).isEmpty()
    }
  }

  @Nested
  inner class findLatestOffenderBookingByNomsId {
    @Test
    fun `test find latest booking`() {
      val optionalOffenderBooking = repository.findLatestOffenderBookingByNomsId("A1234AA")
      assertThat(optionalOffenderBooking).get().extracting { it.bookingId }.isEqualTo(-1L)
    }

    @Test
    fun `test find latest booking kotlin null`() {
      val offenderBooking = repository.findLatestOffenderBookingByNomsIdOrNull("A1234AA")
      assertThat(offenderBooking).extracting { it?.bookingId }.isEqualTo(-1L)
    }

    @Test
    fun `test find latest booking when no bookings exist`() {
      val optionalOffenderBooking = repository.findLatestOffenderBookingByNomsId("UNKNOWN")
      assertThat(optionalOffenderBooking).isEmpty
    }

    @Test
    fun `test find latest booking when no bookings exist kotlin null`() {
      val offenderBooking = repository.findLatestOffenderBookingByNomsIdOrNull("UNKNOWN")
      assertThat(offenderBooking).isNull()
    }

    @Test
    fun `test find latest booking with multiple bookings for offender`() {
      val optionalOffenderBooking = repository.findLatestOffenderBookingByNomsId("A1234AL").orElseThrow()
      assertThat(optionalOffenderBooking).extracting { it.bookingId }.isEqualTo(-12L)

      // sanity check that there are multiple bookings
      val secondBooking = repository.findByOffenderNomsIdAndBookingSequence("A1234AL", 2).orElseThrow()
      assertThat(secondBooking).extracting { it.bookingId }.isEqualTo(-13L)
    }
  }
}
