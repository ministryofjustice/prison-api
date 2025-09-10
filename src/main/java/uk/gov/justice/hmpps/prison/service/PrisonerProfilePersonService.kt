package uk.gov.justice.hmpps.prison.service

import org.springframework.dao.CannotAcquireLockException
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.PrisonerProfileSummaryDto
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException

@Service
class PrisonerProfilePersonService(
  private val prisonerProfileUpdateService: PrisonerProfileUpdateService,
  private val offenderPhonesService: OffenderPhonesService,
  private val offenderEmailsService: OffenderEmailsService,
  private val offenderAddressService: OffenderAddressService,
  private val offenderMilitaryRecordService: OffenderMilitaryRecordService,
  private val distinguishingMarkService: DistinguishingMarkService,
) {
  fun getPerson(prisonerNumber: String): PrisonerProfileSummaryDto {
    val militaryRecord = try {
      offenderMilitaryRecordService.getMilitaryRecords(prisonerNumber)
    } catch (e: EntityNotFoundException) {
      null
    }

    val physicalAttributes = try {
      prisonerProfileUpdateService.getPhysicalAttributes(prisonerNumber)
    } catch (ex: Exception) {
      when (ex) {
        is EntityNotFoundException,
        is CannotAcquireLockException,
        is DatabaseRowLockedException,
        -> null
        else -> throw ex
      }
    }

    val addresses = try {
      offenderAddressService.getAddressesByOffenderNo(prisonerNumber)
    } catch (ex: NoSuchElementException) {
      throw EntityNotFoundException("Prisoner with prisonerNumber $prisonerNumber not found")
    }

    return PrisonerProfileSummaryDto(
      aliases = prisonerProfileUpdateService.getAliases(prisonerNumber),
      addresses = addresses,
      phones = offenderPhonesService.getPhoneNumbersByOffenderNo(prisonerNumber),
      emails = offenderEmailsService.getEmailsByPrisonerNumber(prisonerNumber),
      militaryRecord = militaryRecord,
      physicalAttributes = physicalAttributes,
      distinguishingMarks = distinguishingMarkService.findMarksForLatestBooking(prisonerNumber),
    )
  }
}
