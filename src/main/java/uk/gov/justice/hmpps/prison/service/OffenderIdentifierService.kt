package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifierCreateRequest
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifierUpdateRequest
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier as Identifier

@Service
@Transactional(readOnly = true)
class OffenderIdentifierService(private val offenderIdentifierRepository: OffenderIdentifierRepository, private val offenderRepository: OffenderRepository, private val referenceDomainService: ReferenceDomainService) {
  fun getOffenderIdentifiers(prisonerNumber: String, includeAliases: Boolean?): List<Identifier> {
    val offenderIdentifiers = offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(prisonerNumber)
    return offenderIdentifiers.filter { includeAliases == true || it.rootOffenderId == it.offender.id }.map(this::transformOffenderIdentifier)
  }

  fun getOffenderIdentifier(prisonerNumber: String, offenderIdSeq: Long): Identifier {
    val offenderIdentifier = offenderIdentifierRepository.findByPrisonerNumberAndOffenderIdSeq(prisonerNumber, offenderIdSeq)
      .orElseThrow { EntityNotFoundException.withMessage("Offender identifier for prisoner $prisonerNumber with sequence $offenderIdSeq not found") }
    return transformOffenderIdentifier(offenderIdentifier)
  }

  @Transactional
  fun addOffenderIdentifiers(prisonerNumber: String, offenderIdentifierRequests: List<OffenderIdentifierCreateRequest>): List<Identifier> {
    val offender = offenderRepository.findLinkedToLatestBooking(prisonerNumber)
      .orElseThrow { EntityNotFoundException.withMessage("Offender with prisoner number $prisonerNumber not found") }

    val maxSeq = offender.identifiers.maxOfOrNull { it.offenderIdentifierPK.offenderIdSeq } ?: 0

    val newIdentifiers = offenderIdentifierRequests.mapIndexed { index, request ->

      if (!validateIdentifier(prisonerNumber, request.identifierType, request.identifier, offender.identifiers)) {
        throw BadRequestException.withMessage("Identifier ${request.identifier} is not valid")
      }

      OffenderIdentifier.builder()
        .offenderIdentifierPK(OffenderIdentifier.OffenderIdentifierPK(offender.id, maxSeq + index + 1))
        .offender(offender)
        .identifierType(request.identifierType)
        .identifier(request.identifier)
        .issuedAuthorityText(request.issuedAuthorityText)
        .rootOffenderId(offender.rootOffenderId)
        .build()
    }

    return offenderIdentifierRepository.saveAll(newIdentifiers).map(this::transformOffenderIdentifier)
  }

  @Transactional
  fun updateOffenderIdentifier(prisonerNumber: String, offenderIdSeq: Long, offenderIdentifierRequest: OffenderIdentifierUpdateRequest) {
    val offenderIdentifier = offenderIdentifierRepository.findByPrisonerNumberAndOffenderIdSeq(prisonerNumber, offenderIdSeq)
      .orElseThrow { EntityNotFoundException.withMessage("Offender identifier for prisoner $prisonerNumber with sequence $offenderIdSeq not found") }

    val existingIdentifiers = offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(prisonerNumber)

    if (!validateIdentifier(prisonerNumber, offenderIdentifier.identifierType, offenderIdentifierRequest.identifier, existingIdentifiers)) {
      throw BadRequestException.withMessage("Identifier ${offenderIdentifierRequest.identifier} is not valid")
    }

    offenderIdentifier.identifier = offenderIdentifierRequest.identifier
    offenderIdentifier.issuedAuthorityText = offenderIdentifierRequest.issuedAuthorityText

    offenderIdentifierRepository.save(offenderIdentifier)
  }

  private fun validateIdentifier(prisonerNumber: String, identifierType: String, identifier: String, existingIdentifiers: List<OffenderIdentifier>): Boolean {
    if (!referenceDomainService.isReferenceCodeActive("ID_TYPE", identifierType)) {
      throw BadRequestException.withMessage("Identifier type $identifierType is not valid")
    }

    if (existingIdentifiers.any { it.identifier == identifier && it.identifierType == identifierType }) {
      throw BadRequestException.withMessage("${if (identifierType == "PNC") "PNC number" else "Identifier"} $identifier already exists for prisoner $prisonerNumber")
    }

    return when (identifierType) {
      "PNC" -> isValidPncFormat(identifier)
      "CRO" -> isValidCroFormat(identifier)
      else -> {
        // Add more validation logic for other identifier types if needed
        true
      }
    }
  }

  private fun isValidPncFormat(pnc: String): Boolean {
    val pncPattern = """^\d{0,2}(\d{2})/(\d{1,7})([A-Z])$""".toRegex()
    val match = pncPattern.matchEntire(pnc) ?: return false

    val year = match.groupValues[1]
    val serial = match.groupValues[2]
    val checkChar = match.groupValues[3]

    val numericPart = "$year${serial.padStart(7, '0')}"
    val numericInt = numericPart.toIntOrNull() ?: return false
    val expectedCheckChar = VALID_LETTERS[numericInt % VALID_LETTERS.length]

    return checkChar == expectedCheckChar.toString()
  }

  private fun isValidCroFormat(cro: String): Boolean {
    val croPattern = """^(?<serial>\d{1,6})/(?<year>\d{2})(?<check>[A-Z])${'$'}""".toRegex()
    val croSfPattern = """^SF(?<year>\d{2})/(?<serial>\d{1,6})(?<check>[A-Z])${'$'}""".toRegex()

    val match = croPattern.matchEntire(cro) ?: croSfPattern.matchEntire(cro) ?: return false

    val croSf = croSfPattern.matches(cro)
    val year = match.groups["year"]?.value ?: return false
    val serial = match.groups["serial"]?.value ?: return false
    val checkChar = match.groups["check"]?.value ?: return false

    val numericPart = "$year${if (croSf) serial else serial.padStart(6, '0')}"
    val numericInt = numericPart.toIntOrNull() ?: return false
    val expectedCheckChar = VALID_LETTERS[numericInt % VALID_LETTERS.length]

    return checkChar == expectedCheckChar.toString()
  }

  private fun transformOffenderIdentifier(identifier: OffenderIdentifier): Identifier = Identifier(
    identifier.identifierType, identifier.identifier, identifier.offender.nomsId,
    null, identifier.issuedAuthorityText,
    identifier.issuedDate, identifier.caseloadType, identifier.createDateTime, identifier.offender.id,
    identifier.rootOffenderId, identifier.offenderIdentifierPK.offenderIdSeq,
  )

  companion object {
    private const val VALID_LETTERS = "ZABCDEFGHJKLMNPQRTUVWXY"
  }
}
