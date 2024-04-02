package uk.gov.justice.hmpps.prison.util.builders

import org.apache.commons.codec.language.Soundex
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import java.time.LocalDate

class OffenderAliasBuilder(
  var lastName: String = "ALIAS",
  var firstName: String = randomName(),
  var birthDate: LocalDate = LocalDate.of(1990, 8, 20),
) {
  fun save(inmate: InmateDetail, dataLoader: DataLoaderRepository): Offender {
    val offender = dataLoader.offenderRepository.findRootOffenderByNomsId(inmate.offenderNo).orElseThrow()

    val alias = Offender.builder()
      .nomsId(offender.nomsId)
      .rootOffender(offender.rootOffender)
      .birthDate(birthDate)
      .lastName(lastName)
      .firstName(firstName)
      .gender(offender.gender)
      .caseloadType("INST")
      .lastNameKey(lastName)
      .lastNameAlphaKey(lastName.take(1))
      .lastNameSoundex(Soundex().soundex(lastName))
      .build()
    return dataLoader.offenderRepository.save(alias)
  }
}
