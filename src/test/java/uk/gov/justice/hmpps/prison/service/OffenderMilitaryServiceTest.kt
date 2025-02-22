package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.hibernate.exception.LockTimeoutException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.CannotAcquireLockException
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecord
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecordRequest
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderMilitaryRecordRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import java.sql.SQLException
import java.time.LocalDate
import java.util.Optional

class OffenderMilitaryServiceTest {

  private val repository: OffenderMilitaryRecordRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val warZoneRepository: ReferenceCodeRepository<WarZone> = mock()
  private val militaryRankRepository: ReferenceCodeRepository<MilitaryRank> = mock()
  private val militaryBranchRepository: ReferenceCodeRepository<MilitaryBranch> = mock()
  private val militaryDischargeRepository: ReferenceCodeRepository<MilitaryDischarge> = mock()
  private val disciplinaryActionRepository: ReferenceCodeRepository<DisciplinaryAction> = mock()

  private val offenderMilitaryRecordService = OffenderMilitaryRecordService(
    repository,
    offenderBookingRepository,
    warZoneRepository,
    militaryRankRepository,
    militaryBranchRepository,
    militaryDischargeRepository,
    disciplinaryActionRepository,
  )

  private fun stubSuccessfulRepositoryCall() {
    whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(anyString(), anyInt())).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .bookingId(-1L)
          .militaryRecords(listOf())
          .build(),
      ),
    )

    whenever(repository.findAllByBookingId(anyLong())).thenReturn(
      listOf(
        OFFENDER_MILITARY_RECORD,
        OffenderMilitaryRecord.builder()
          .bookingAndSequence(OffenderMilitaryRecord.BookingAndSequence(OffenderBooking.builder().bookingId(-1L).build(), 2))
          .startDate(LocalDate.parse("2001-01-01"))
          .militaryBranch(MilitaryBranch("NAV", "Navy"))
          .description("second record")
          .build(),
      ),
    )

    whenever(warZoneRepository.findById(ReferenceCode.Pk("MLTY_WZONE", "AFG"))).thenReturn(
      Optional.of(WarZone("AFG", "Afghanistan")),
    )

    whenever(militaryRankRepository.findById(ReferenceCode.Pk("MLTY_RANK", "LCPL_RMA"))).thenReturn(
      Optional.of(MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)")),
    )

    whenever(militaryBranchRepository.findById(ReferenceCode.Pk("MLTY_BRANCH", "ARM"))).thenReturn(
      Optional.of(MilitaryBranch("ARM", "Army")),
    )

    whenever(militaryBranchRepository.findById(ReferenceCode.Pk("MLTY_BRANCH", "NAV"))).thenReturn(
      Optional.of(MilitaryBranch("NAV", "Navy")),
    )

    whenever(militaryDischargeRepository.findById(ReferenceCode.Pk("MLTY_DSCHRG", "DIS"))).thenReturn(
      Optional.of(MilitaryDischarge("DIS", "Dishonourable")),
    )

    whenever(disciplinaryActionRepository.findById(ReferenceCode.Pk("MLTY_DISCP", "CM"))).thenReturn(
      Optional.of(DisciplinaryAction("CM", "Court Martial")),
    )
  }

  private fun stubNotFoundRepositoryCall(offenderNo: String) {
    whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(eq(offenderNo), anyInt()))
      .thenThrow(EntityNotFoundException("No bookings found for prisoner number $offenderNo"))
  }

  @Test
  fun `get OffenderMilitaryRecords by bookingId success`() {
    stubSuccessfulRepositoryCall()

    val militaryRecords = offenderMilitaryRecordService.getMilitaryRecords(-1L)

    assertThat(militaryRecords).usingRecursiveComparison().isEqualTo(MILITARY_RECORDS)
  }

  @Test
  fun `get OffenderMilitaryRecords by offenderNo should throw exception if offender not found`() {
    stubNotFoundRepositoryCall("Z9999ZZ")

    val exception = assertThrows<EntityNotFoundException> {
      offenderMilitaryRecordService.getMilitaryRecords("Z9999ZZ")
    }
    assertThat(exception.message).isEqualTo("Resource with id [Z9999ZZ] not found.")
  }

  @Test
  fun `get OffenderMilitaryRecords by offenderNo success`() {
    stubSuccessfulRepositoryCall()

    val militaryRecords = offenderMilitaryRecordService.getMilitaryRecords("A1234AA")

    assertThat(militaryRecords).usingRecursiveComparison().isEqualTo(MILITARY_RECORDS)
  }

  @Test
  fun `createMilitaryRecord should save new record`() {
    stubSuccessfulRepositoryCall()

    val offenderNo = "A1234AA"
    val militaryRecord = CREATE_MILITARY_RECORD

    offenderMilitaryRecordService.createMilitaryRecord(offenderNo, militaryRecord)

    verify(repository).save(OFFENDER_MILITARY_RECORD)
  }

  @Test
  fun `updateMilitaryRecord should update existing record`() {
    stubSuccessfulRepositoryCall()

    val militaryRecord = UPDATE_MILITARY_RECORD_TWO

    val existingRecord = OffenderMilitaryRecord.builder()
      .bookingAndSequence(OffenderMilitaryRecord.BookingAndSequence(OffenderBooking.builder().bookingId(1L).build(), 1))
      .startDate(LocalDate.parse("2000-01-01"))
      .endDate(LocalDate.parse("2020-10-17"))
      .militaryDischarge(MilitaryDischarge("DIS", "Dishonourable"))
      .warZone(WarZone("AFG", "Afghanistan"))
      .militaryBranch(MilitaryBranch("ARM", "Army"))
      .description("left")
      .unitNumber("auno")
      .enlistmentLocation("Somewhere")
      .militaryRank(MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"))
      .serviceNumber("asno")
      .disciplinaryAction(DisciplinaryAction("CM", "Court Martial"))
      .dischargeLocation("Sheffield")
      .build()

    whenever(repository.findByBookingIdAndMilitarySeqWithLock(anyLong(), anyInt())).thenReturn(existingRecord)

    offenderMilitaryRecordService.updateMilitaryRecord("A1234AA", 1, militaryRecord)

    verify(repository).save(existingRecord)
  }

  @Test
  fun `updateMilitaryRecord should throw exception if offender not found`() {
    stubSuccessfulRepositoryCall()

    val militaryRecord = UPDATE_MILITARY_RECORD_ONE

    whenever(repository.findByBookingIdAndMilitarySeqWithLock(anyLong(), anyInt())).thenReturn(null)

    assertThrows<RuntimeException> {
      offenderMilitaryRecordService.updateMilitaryRecord("A1234AA", 1, militaryRecord)
    }
  }

  @Test
  fun `updateMilitaryRecord should throw DatabaseRowLockedException`() {
    stubSuccessfulRepositoryCall()

    val militaryRecord = UPDATE_MILITARY_RECORD_ONE

    whenever(repository.findByBookingIdAndMilitarySeqWithLock(anyLong(), anyInt()))
      .thenThrow(CannotAcquireLockException("test", LockTimeoutException("[ORA-30006]", SQLException())))

    val exception = assertThrows<DatabaseRowLockedException> {
      offenderMilitaryRecordService.updateMilitaryRecord("A1234AA", 1, militaryRecord)
    }

    assertThat(exception.message).isEqualTo("Resource locked, possibly in use in P-Nomis.")
    assertThat(exception.developerMessage).isEqualTo("Failed to get OFFENDER_MILITARY_RECORD row lock for booking id -1 and military sequence 1")
  }

  companion object {
    private val MILITARY_RECORD_ONE = MilitaryRecord(
      bookingId = -1L,
      militarySeq = 1,
      warZoneCode = "AFG",
      warZoneDescription = "Afghanistan",
      startDate = LocalDate.parse("2000-01-01"),
      endDate = LocalDate.parse("2020-10-17"),
      militaryBranchCode = "ARM",
      militaryBranchDescription = "Army",
      description = "left",
      unitNumber = "auno",
      enlistmentLocation = "Somewhere",
      militaryRankCode = "LCPL_RMA",
      militaryRankDescription = "Lance Corporal  (Royal Marines)",
      serviceNumber = "asno",
      disciplinaryActionCode = "CM",
      disciplinaryActionDescription = "Court Martial",
      dischargeLocation = "Sheffield",
      militaryDischargeCode = "DIS",
      militaryDischargeDescription = "Dishonourable",
      selectiveServicesFlag = false,
    )
    private val MILITARY_RECORD_TWO = MilitaryRecord(
      bookingId = -1L,
      militarySeq = 2,
      startDate = LocalDate.parse("2001-01-01"),
      militaryBranchCode = "NAV",
      militaryBranchDescription = "Navy",
      description = "second record",
      selectiveServicesFlag = false,
    )
    private val CREATE_MILITARY_RECORD = MilitaryRecordRequest(
      warZoneCode = "AFG",
      startDate = LocalDate.parse("2000-01-01"),
      endDate = LocalDate.parse("2020-10-17"),
      militaryBranchCode = "ARM",
      description = "left",
      unitNumber = "auno",
      enlistmentLocation = "Somewhere",
      militaryRankCode = "LCPL_RMA",
      serviceNumber = "asno",
      disciplinaryActionCode = "CM",
      dischargeLocation = "Sheffield",
      militaryDischargeCode = "DIS",
      selectiveServicesFlag = false,
    )
    private val UPDATE_MILITARY_RECORD_ONE = MilitaryRecordRequest(
      warZoneCode = "AFG",
      startDate = LocalDate.parse("2000-01-01"),
      endDate = LocalDate.parse("2020-10-17"),
      militaryBranchCode = "ARM",
      description = "left",
      unitNumber = "auno",
      enlistmentLocation = "Somewhere",
      militaryRankCode = "LCPL_RMA",
      serviceNumber = "asno",
      disciplinaryActionCode = "CM",
      dischargeLocation = "Sheffield",
      militaryDischargeCode = "DIS",
      selectiveServicesFlag = false,
    )
    private val UPDATE_MILITARY_RECORD_TWO = MilitaryRecordRequest(
      startDate = LocalDate.parse("2001-01-01"),
      militaryBranchCode = "NAV",
      description = "second record",
      selectiveServicesFlag = false,
    )

    private val OFFENDER_MILITARY_RECORD = OffenderMilitaryRecord.builder()
      .bookingAndSequence(OffenderMilitaryRecord.BookingAndSequence(OffenderBooking.builder().bookingId(-1L).build(), 1))
      .startDate(LocalDate.parse("2000-01-01"))
      .endDate(LocalDate.parse("2020-10-17"))
      .militaryDischarge(MilitaryDischarge("DIS", "Dishonourable"))
      .warZone(WarZone("AFG", "Afghanistan"))
      .militaryBranch(MilitaryBranch("ARM", "Army"))
      .description("left")
      .unitNumber("auno")
      .enlistmentLocation("Somewhere")
      .militaryRank(MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"))
      .serviceNumber("asno")
      .disciplinaryAction(DisciplinaryAction("CM", "Court Martial"))
      .dischargeLocation("Sheffield")
      .build()

    private val MILITARY_RECORDS = MilitaryRecords(
      listOf(
        MILITARY_RECORD_ONE,
        MILITARY_RECORD_TWO,
      ),
    )
  }
}
