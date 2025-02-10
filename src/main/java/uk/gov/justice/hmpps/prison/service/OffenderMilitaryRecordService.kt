package uk.gov.justice.hmpps.prison.service

import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.CreateMilitaryRecord
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecord
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords
import uk.gov.justice.hmpps.prison.api.model.UpdateMilitaryRecord
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

@Service
@Transactional
class OffenderMilitaryRecordService(
  private val repository: OffenderMilitaryRecordRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val warZoneRepository: ReferenceCodeRepository<WarZone>,
  private val militaryRankRepository: ReferenceCodeRepository<MilitaryRank>,
  private val militaryBranchRepository: ReferenceCodeRepository<MilitaryBranch>,
  private val militaryDischargeRepository: ReferenceCodeRepository<MilitaryDischarge>,
  private val disciplinaryActionRepository: ReferenceCodeRepository<DisciplinaryAction>,
) {

  fun getMilitaryRecords(bookingId: Long): MilitaryRecords = repository.findAllByBookingId(bookingId)
    .map { mapToDto(it) }
    .let { MilitaryRecords(it) }

  fun getMilitaryRecords(offenderNo: String): MilitaryRecords = try {
    val booking = getLatestOffenderBooking(offenderNo)
    getMilitaryRecords(booking.bookingId)
  } catch (e: EntityNotFoundException) {
    // Rethrow against the offender number rather than the booking id
    throw EntityNotFoundException.withId(offenderNo)
  }

  fun createMilitaryRecord(offenderNo: String, militaryRecord: CreateMilitaryRecord) {
    val booking = getLatestOffenderBooking(offenderNo)
    val nextMilitarySeq = booking.militaryRecords.size + 1

    val offenderMilitaryRecord = OffenderMilitaryRecord.builder()
      .bookingAndSequence(OffenderMilitaryRecord.BookingAndSequence(booking, nextMilitarySeq))
      .warZone(
        militaryRecord.warZoneCode?.let {
          warZoneRepository.findById(ReferenceCode.Pk("MLTY_WZONE", militaryRecord.warZoneCode)).orElseThrow(
            { EntityNotFoundException.withMessage("War zone code ${militaryRecord.warZoneCode} not found") },
          )
        },
      )
      .startDate(militaryRecord.startDate)
      .endDate(militaryRecord.endDate)
      .militaryDischarge(
        militaryRecord.militaryDischargeCode?.let {
          militaryDischargeRepository.findById(ReferenceCode.Pk("MLTY_DSCHRG", militaryRecord.militaryDischargeCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Military discharge code ${militaryRecord.militaryDischargeCode} not found") },
          )
        },
      )
      .militaryBranch(
        militaryBranchRepository.findById(ReferenceCode.Pk("MLTY_BRANCH", militaryRecord.militaryBranchCode)).orElseThrow(
          { EntityNotFoundException.withMessage("Military branch code ${militaryRecord.militaryBranchCode} not found") },
        ),
      )
      .description(militaryRecord.description)
      .unitNumber(militaryRecord.unitNumber)
      .enlistmentLocation(militaryRecord.enlistmentLocation)
      .dischargeLocation(militaryRecord.dischargeLocation)
      .selectiveServicesFlag(militaryRecord.selectiveServicesFlag)
      .militaryRank(
        militaryRecord.militaryRankCode?.let {
          militaryRankRepository.findById(ReferenceCode.Pk("MLTY_RANK", militaryRecord.militaryRankCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Military rank code ${militaryRecord.militaryRankCode} not found") },
          )
        },
      )
      .serviceNumber(militaryRecord.serviceNumber)
      .disciplinaryAction(
        militaryRecord.disciplinaryActionCode?.let {
          disciplinaryActionRepository.findById(ReferenceCode.Pk("MLTY_DISCP", militaryRecord.disciplinaryActionCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Disciplinary action code ${militaryRecord.disciplinaryActionCode} not found") },
          )
        },
      )
      .build()

    repository.save(offenderMilitaryRecord)
  }

  fun updateMilitaryRecord(offenderNo: String, militaryRecord: UpdateMilitaryRecord) {
    val booking = getLatestOffenderBooking(offenderNo)
    try {
      val record = repository.findByBookingIdAndMilitarySeqWithLock(booking.bookingId, militaryRecord.militarySeq)
        ?: throw EntityNotFoundException("Military record not found for prisoner number $offenderNo and military sequence ${militaryRecord.militarySeq}")

      with(record) {
        warZone = militaryRecord.warZoneCode?.let {
          warZoneRepository.findById(ReferenceCode.Pk("MLTY_WZONE", militaryRecord.warZoneCode)).orElseThrow(
            { EntityNotFoundException.withMessage("War zone code ${militaryRecord.warZoneCode} not found") },
          )
        }
        startDate = militaryRecord.startDate
        endDate = militaryRecord.endDate
        militaryDischarge = militaryRecord.militaryDischargeCode?.let {
          militaryDischargeRepository.findById(ReferenceCode.Pk("MLTY_DSCHRG", militaryRecord.militaryDischargeCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Military discharge code ${militaryRecord.militaryDischargeCode} not found") },
          )
        }
        militaryBranch = militaryBranchRepository.findById(ReferenceCode.Pk("MLTY_BRANCH", militaryRecord.militaryBranchCode)).orElseThrow(
          { EntityNotFoundException.withMessage("Military branch code ${militaryRecord.militaryBranchCode} not found") },
        )
        description = militaryRecord.description
        unitNumber = militaryRecord.unitNumber
        enlistmentLocation = militaryRecord.enlistmentLocation
        dischargeLocation = militaryRecord.dischargeLocation
        selectiveServicesFlag = militaryRecord.selectiveServicesFlag
        militaryRank = militaryRecord.militaryRankCode?.let {
          militaryRankRepository.findById(ReferenceCode.Pk("MLTY_RANK", militaryRecord.militaryRankCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Military rank code ${militaryRecord.militaryRankCode} not found") },
          )
        }
        serviceNumber = militaryRecord.serviceNumber
        disciplinaryAction = militaryRecord.disciplinaryActionCode?.let {
          disciplinaryActionRepository.findById(ReferenceCode.Pk("MLTY_DISCP", militaryRecord.disciplinaryActionCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Disciplinary action code ${militaryRecord.disciplinaryActionCode} not found") },
          )
        }
      }

      repository.save(record)
    } catch (error: CannotAcquireLockException) {
      log.error("Failed to acquire lock when updating military record", error)
      throw if (true == error.cause?.message?.contains("ORA-30006")) {
        DatabaseRowLockedException("Failed to get OFFENDER_MILITARY_RECORD row lock for booking id ${booking.bookingId} and military sequence ${militaryRecord.militarySeq}")
      } else {
        error
      }
    }
  }

  private fun mapToDto(record: OffenderMilitaryRecord): MilitaryRecord = MilitaryRecord(
    bookingId = record.bookingAndSequence.offenderBooking.bookingId,
    militarySeq = record.bookingAndSequence.sequence,
    warZoneCode = record.warZone?.code,
    warZoneDescription = record.warZone?.description,
    startDate = record.startDate,
    endDate = record.endDate,
    militaryDischargeCode = record.militaryDischarge?.code,
    militaryDischargeDescription = record.militaryDischarge?.description,
    militaryBranchCode = record.militaryBranch.code,
    militaryBranchDescription = record.militaryBranch.description,
    description = record.description,
    unitNumber = record.unitNumber,
    enlistmentLocation = record.enlistmentLocation,
    dischargeLocation = record.dischargeLocation,
    selectiveServicesFlag = record.selectiveServicesFlag,
    militaryRankCode = record.militaryRank?.code,
    militaryRankDescription = record.militaryRank?.description,
    serviceNumber = record.serviceNumber,
    disciplinaryActionCode = record.disciplinaryAction?.code,
    disciplinaryActionDescription = record.disciplinaryAction?.description,
  )

  private fun getLatestOffenderBooking(offenderNo: String): OffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, 1)
    .orElseThrow { EntityNotFoundException.withMessage("No bookings found for prisoner number $offenderNo") }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
