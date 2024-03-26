package uk.gov.justice.hmpps.prison.dsl

import org.apache.commons.codec.language.Soundex
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import java.time.LocalDate

@Component
class AliasBuilderRepository(
  private val offenderRepository: OffenderRepository,
) {
  fun save(
    offenderId: OffenderId,
    lastName: String,
    firstName: String,
    birthDate: LocalDate,
  ): AliasId = offenderRepository.findOffenderByNomsId(offenderId.offenderNo).orElseThrow().let {
    offenderRepository.save(
      Offender.builder()
        .nomsId(it.nomsId)
        .rootOffender(it.rootOffender)
        .birthDate(birthDate)
        .lastName(lastName)
        .firstName(firstName)
        .gender(it.gender)
        .caseloadType("INST")
        .lastNameKey(lastName)
        .lastNameAlphaKey(lastName.take(1))
        .lastNameSoundex(Soundex().soundex(lastName))
        .build(),
    ).let { alias -> AliasId(alias.id) }
  }
}

@Component
class AliasBuilderFactory(
  private val repository: AliasBuilderRepository,
) {
  fun builder() = AliasBuilder(repository)
}

@NomisDataDslMarker
class AliasBuilder(
  private val repository: AliasBuilderRepository,
) {
  private lateinit var aliasId: AliasId

  fun build(
    offenderId: OffenderId,
    lastName: String,
    firstName: String,
    birthDate: LocalDate,
  ): AliasId = repository.save(
    offenderId = offenderId,
    lastName = lastName,
    firstName = firstName,
    birthDate = birthDate,
  ).also { aliasId = it }
}

data class AliasId(val offenderId: Long)
