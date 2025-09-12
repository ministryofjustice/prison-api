package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.PrisonerProfileSummaryDto

@Service
class PrisonerProfilePersonService(
  private val prisonerProfileUpdateService: PrisonerProfileUpdateService,
  private val offenderPhonesService: OffenderPhonesService,
  private val offenderEmailsService: OffenderEmailsService,
  private val offenderAddressService: OffenderAddressService,
  private val offenderMilitaryRecordService: OffenderMilitaryRecordService,
  private val distinguishingMarkService: DistinguishingMarkService,
) {
  fun getPrisonerProfileSummary(prisonerNumber: String): PrisonerProfileSummaryDto = try {
    PrisonerProfileSummaryDto(
      aliases = prisonerProfileUpdateService.getAliases(prisonerNumber),
      addresses = offenderAddressService.getAddressesByOffenderNo(prisonerNumber),
      phones = offenderPhonesService.getPhoneNumbersByOffenderNo(prisonerNumber),
      emails = offenderEmailsService.getEmailsByPrisonerNumber(prisonerNumber),
      militaryRecord = offenderMilitaryRecordService.getMilitaryRecords(prisonerNumber),
      physicalAttributes = prisonerProfileUpdateService.getPhysicalAttributes(prisonerNumber),
      distinguishingMarks = distinguishingMarkService.findMarksForLatestBooking(prisonerNumber),
    )
  } catch (ex: NoSuchElementException) {
    throw EntityNotFoundException("Prisoner with prisonerNumber $prisonerNumber not found")
  }
}
