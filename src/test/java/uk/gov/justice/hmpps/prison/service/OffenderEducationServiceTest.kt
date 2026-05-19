package uk.gov.justice.hmpps.prison.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationLevel
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducationAddress
import uk.gov.justice.hmpps.prison.repository.jpa.model.StudyArea
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderEducationRepository
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEducationTransformer
import java.time.LocalDate

class OffenderEducationServiceTest {
  private companion object {
    private const val BATCH_SIZE = 1
    private const val NOMIS_ID = "abc"
  }

  private val address: OffenderEducationAddress = OffenderEducationAddress.builder()
    .addressId(2349706L)
    .noFixedAddressFlag("N")
    .primaryFlag("N")
    .country(Country("ENG", "England"))
    .build()

  val education1: OffenderEducation = OffenderEducation
    .builder()
    .id(OffenderEducation.PK(584215L, 1L))
    .startDate(LocalDate.of(2009, 12, 21))
    .studyArea(StudyArea("GEN", "General Studies"))
    .educationLevel(EducationLevel("DEGREE", "Degree Level or Higher"))
    .comment("Good student")
    .school("School of economics")
    .isSpecialEducation(true)
    .schedule(EducationSchedule("PART", "Part Time"))
    .addresses(listOf(address))
    .build()

  private val education2: OffenderEducation = OffenderEducation
    .builder()
    .id(OffenderEducation.PK(584215L, 2L))
    .startDate(LocalDate.of(2016, 2, 10))
    .comment("Needs more focus")
    .school("moj education")
    .schedule(EducationSchedule("NK", "Not Known"))
    .isSpecialEducation(false)
    .build()

  private val transformer: OffenderEducationTransformer = mock()
  private val repository: OffenderEducationRepository = mock()

  private val service = OffenderEducationService(repository, transformer, BATCH_SIZE)

  @Test
  fun getOffenderEducationsInBulk_inBatchesOfOne() {
    val educations = listOf(education1, education2)

    whenever(repository.findAllByNomisIdIn(listOf(NOMIS_ID)))
      .thenReturn(educations)
    whenever(repository.findAllByNomisIdIn(mutableListOf("ABC123")))
      .thenReturn(
        mutableListOf<OffenderEducation?>(),
      )

    service.getOffenderEducations(listOf(NOMIS_ID, "ABC123"))

    verify(repository).findAllByNomisIdIn(listOf(NOMIS_ID))
    verify(repository).findAllByNomisIdIn(mutableListOf("ABC123"))
    verify(transformer).convert(education1)
    verify(transformer).convert(education2)
  }
}
