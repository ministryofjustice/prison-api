package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier as Identifier

@Service
@Transactional(readOnly = true)
class OffenderIdentifierService(private val offenderIdentifierRepository: OffenderIdentifierRepository) {
  fun getOffenderIdentifiers(prisonerNumber: String, includeAliases: Boolean?): List<Identifier> {
    val offenderIdentifiers = offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(prisonerNumber)
    return offenderIdentifiers.filter { includeAliases == true || it.rootOffenderId == it.offender.id }.map(this::transformOffenderIdentifier)
  }

  private fun transformOffenderIdentifier(identifier: OffenderIdentifier): Identifier = Identifier(
    identifier.identifierType, identifier.identifier, identifier.offender.nomsId,
    null, identifier.issuedAuthorityText,
    identifier.issuedDate, identifier.caseloadType, identifier.createDateTime, identifier.offender.id,
    identifier.rootOffenderId,
  )
}
