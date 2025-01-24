package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecord
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderMilitaryRecordRepository

@Service
@Transactional
class OffenderMilitaryRecordService(
  private val repository: OffenderMilitaryRecordRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
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

  fun createMilitaryRecord(offenderNo: String, militaryRecord: MilitaryRecord) {
    val booking = getLatestOffenderBooking(offenderNo)
    val nextMilitarySeq = booking.militaryRecords.size + 1

    val offenderMilitaryRecord = OffenderMilitaryRecord.builder()
      .bookingAndSequence(OffenderMilitaryRecord.BookingAndSequence(booking, nextMilitarySeq))
      .warZone(militaryRecord.warZoneCode?.let { WarZone(it, militaryRecord.warZoneDescription) })
      .startDate(militaryRecord.startDate)
      .endDate(militaryRecord.endDate)
      .militaryDischarge(militaryRecord.militaryDischargeCode?.let { MilitaryDischarge(it, militaryRecord.militaryDischargeDescription) })
      .militaryBranch(MilitaryBranch(militaryRecord.militaryBranchCode, militaryRecord.militaryBranchDescription))
      .description(militaryRecord.description)
      .unitNumber(militaryRecord.unitNumber)
      .enlistmentLocation(militaryRecord.enlistmentLocation)
      .dischargeLocation(militaryRecord.dischargeLocation)
      .selectiveServicesFlag(militaryRecord.selectiveServicesFlag)
      .militaryRank(militaryRecord.militaryRankCode?.let { MilitaryRank(it, militaryRecord.militaryRankDescription) })
      .serviceNumber(militaryRecord.serviceNumber)
      .disciplinaryAction(militaryRecord.disciplinaryActionCode?.let { DisciplinaryAction(it, militaryRecord.disciplinaryActionDescription) })
      .build()

    repository.save(offenderMilitaryRecord)
  }

  fun updateMilitaryRecord(militaryRecord: MilitaryRecord) {
    val record = repository.findByBookingIdAndMilitarySeqWithLock(militaryRecord.bookingId, militaryRecord.militarySeq)
      ?: throw RuntimeException("Military record not found")

    with(record) {
      warZone = militaryRecord.warZoneCode?.let { WarZone(it, militaryRecord.warZoneDescription) }
      startDate = militaryRecord.startDate
      endDate = militaryRecord.endDate
      militaryDischarge = militaryRecord.militaryDischargeCode?.let { MilitaryDischarge(it, militaryRecord.militaryDischargeDescription) }
      militaryBranch = MilitaryBranch(militaryRecord.militaryBranchCode, militaryRecord.militaryBranchDescription)
      description = militaryRecord.description
      unitNumber = militaryRecord.unitNumber
      enlistmentLocation = militaryRecord.enlistmentLocation
      dischargeLocation = militaryRecord.dischargeLocation
      selectiveServicesFlag = militaryRecord.selectiveServicesFlag
      militaryRank = militaryRecord.militaryRankCode?.let { MilitaryRank(it, militaryRecord.militaryRankDescription) }
      serviceNumber = militaryRecord.serviceNumber
      disciplinaryAction = militaryRecord.disciplinaryActionCode?.let { DisciplinaryAction(it, militaryRecord.disciplinaryActionDescription) }
    }

    repository.save(record)
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
}
