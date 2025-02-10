package uk.gov.justice.hmpps.prison.service

import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

  fun createMilitaryRecord(offenderNo: String, militaryRecordRequest: MilitaryRecordRequest) {
    val booking = getLatestOffenderBooking(offenderNo)
    val nextMilitarySeq = booking.militaryRecords.size + 1

    val offenderMilitaryRecord = OffenderMilitaryRecord.builder()
      .bookingAndSequence(OffenderMilitaryRecord.BookingAndSequence(booking, nextMilitarySeq))
      .warZone(
        militaryRecordRequest.warZoneCode?.let {
          warZoneRepository.findById(ReferenceCode.Pk("MLTY_WZONE", militaryRecordRequest.warZoneCode)).orElseThrow(
            { EntityNotFoundException.withMessage("War zone code ${militaryRecordRequest.warZoneCode} not found") },
          )
        },
      )
      .startDate(militaryRecordRequest.startDate)
      .endDate(militaryRecordRequest.endDate)
      .militaryDischarge(
        militaryRecordRequest.militaryDischargeCode?.let {
          militaryDischargeRepository.findById(ReferenceCode.Pk("MLTY_DSCHRG", militaryRecordRequest.militaryDischargeCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Military discharge code ${militaryRecordRequest.militaryDischargeCode} not found") },
          )
        },
      )
      .militaryBranch(
        militaryBranchRepository.findById(ReferenceCode.Pk("MLTY_BRANCH", militaryRecordRequest.militaryBranchCode)).orElseThrow(
          { EntityNotFoundException.withMessage("Military branch code ${militaryRecordRequest.militaryBranchCode} not found") },
        ),
      )
      .description(militaryRecordRequest.description)
      .unitNumber(militaryRecordRequest.unitNumber)
      .enlistmentLocation(militaryRecordRequest.enlistmentLocation)
      .dischargeLocation(militaryRecordRequest.dischargeLocation)
      .selectiveServicesFlag(militaryRecordRequest.selectiveServicesFlag)
      .militaryRank(
        militaryRecordRequest.militaryRankCode?.let {
          militaryRankRepository.findById(ReferenceCode.Pk("MLTY_RANK", militaryRecordRequest.militaryRankCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Military rank code ${militaryRecordRequest.militaryRankCode} not found") },
          )
        },
      )
      .serviceNumber(militaryRecordRequest.serviceNumber)
      .disciplinaryAction(
        militaryRecordRequest.disciplinaryActionCode?.let {
          disciplinaryActionRepository.findById(ReferenceCode.Pk("MLTY_DISCP", militaryRecordRequest.disciplinaryActionCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Disciplinary action code ${militaryRecordRequest.disciplinaryActionCode} not found") },
          )
        },
      )
      .build()

    repository.save(offenderMilitaryRecord)
  }

  fun updateMilitaryRecord(offenderNo: String, militarySeq: Int, militaryRecordRequest: MilitaryRecordRequest) {
    val booking = getLatestOffenderBooking(offenderNo)
    try {
      val record = repository.findByBookingIdAndMilitarySeqWithLock(booking.bookingId, militarySeq)
        ?: throw EntityNotFoundException("Military record not found for prisoner number $offenderNo and military sequence $militarySeq")

      with(record) {
        warZone = militaryRecordRequest.warZoneCode?.let {
          warZoneRepository.findById(ReferenceCode.Pk("MLTY_WZONE", militaryRecordRequest.warZoneCode)).orElseThrow(
            { EntityNotFoundException.withMessage("War zone code ${militaryRecordRequest.warZoneCode} not found") },
          )
        }
        startDate = militaryRecordRequest.startDate
        endDate = militaryRecordRequest.endDate
        militaryDischarge = militaryRecordRequest.militaryDischargeCode?.let {
          militaryDischargeRepository.findById(ReferenceCode.Pk("MLTY_DSCHRG", militaryRecordRequest.militaryDischargeCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Military discharge code ${militaryRecordRequest.militaryDischargeCode} not found") },
          )
        }
        militaryBranch = militaryBranchRepository.findById(ReferenceCode.Pk("MLTY_BRANCH", militaryRecordRequest.militaryBranchCode)).orElseThrow(
          { EntityNotFoundException.withMessage("Military branch code ${militaryRecordRequest.militaryBranchCode} not found") },
        )
        description = militaryRecordRequest.description
        unitNumber = militaryRecordRequest.unitNumber
        enlistmentLocation = militaryRecordRequest.enlistmentLocation
        dischargeLocation = militaryRecordRequest.dischargeLocation
        selectiveServicesFlag = militaryRecordRequest.selectiveServicesFlag
        militaryRank = militaryRecordRequest.militaryRankCode?.let {
          militaryRankRepository.findById(ReferenceCode.Pk("MLTY_RANK", militaryRecordRequest.militaryRankCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Military rank code ${militaryRecordRequest.militaryRankCode} not found") },
          )
        }
        serviceNumber = militaryRecordRequest.serviceNumber
        disciplinaryAction = militaryRecordRequest.disciplinaryActionCode?.let {
          disciplinaryActionRepository.findById(ReferenceCode.Pk("MLTY_DISCP", militaryRecordRequest.disciplinaryActionCode)).orElseThrow(
            { EntityNotFoundException.withMessage("Disciplinary action code ${militaryRecordRequest.disciplinaryActionCode} not found") },
          )
        }
      }

      repository.save(record)
    } catch (error: CannotAcquireLockException) {
      log.error("Failed to acquire lock when updating military record", error)
      throw if (true == error.cause?.message?.contains("ORA-30006")) {
        DatabaseRowLockedException("Failed to get OFFENDER_MILITARY_RECORD row lock for booking id ${booking.bookingId} and military sequence $militarySeq")
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
