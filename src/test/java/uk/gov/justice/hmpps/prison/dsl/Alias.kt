package uk.gov.justice.hmpps.prison.dsl

import org.apache.commons.codec.language.Soundex
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import java.time.LocalDate

@DslMarker
annotation class AliasDslMarker

@NomisDataDslMarker
interface AliasDsl

@Component
class AliasBuilderRepository(
  private val offenderRepository: OffenderRepository,
) {
  fun save(
    offenderId: OffenderId,
    lastName: String,
    firstName: String,
    birthDate: LocalDate,
  ): AliasId = offenderRepository.findRootOffenderByNomsId(offenderId.offenderNo).orElseThrow().let {
    offenderRepository.save(
      Offender.builder()
        .nomsId(it.nomsId)
        .rootOffenderId(it.id)
        .rootOffender(it)
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

class AliasBuilder(
  private val repository: AliasBuilderRepository,
) : AliasDsl {

  private lateinit var aliasId: AliasId

  fun build(
    offenderId: OffenderId,
    lastName: String,
    firstName: String,
    birthDate: LocalDate,
  ): AliasId {
    return repository.save(
      offenderId = offenderId,
      lastName = lastName,
      firstName = firstName,
      birthDate = birthDate,
    ).also { aliasId = it }
  }
}

data class AliasId(val offenderId: Long)
