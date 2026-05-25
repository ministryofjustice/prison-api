package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationLevel
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducationAddress
import uk.gov.justice.hmpps.prison.repository.jpa.model.StudyArea
import java.time.LocalDate
import java.util.List

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderEducationRepositoryTest(
  @Autowired private val repository: OffenderEducationRepository,
) {
  private val offenderNumber = "G8346GA"
  private val pageSize = 1

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
    .addresses(List.of<OffenderEducationAddress?>(address))
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

  @Test
  fun testExpectedNumberOfEducationsAreReturned() {
    val educations = repository.findAllByNomisId(offenderNumber, Pageable.unpaged())

    assertThat(educations).hasSize(2)

    assertThat(education1).isEqualTo(educations.getContent()[0])
    assertThat(education2).isEqualTo(educations.getContent()[1])
  }

  @Test
  fun pagedFindAllByNomisIdPage1() {
    val educations = repository.findAllByNomisId(offenderNumber, PageRequest.of(0, pageSize))

    assertThat(educations.totalElements).isEqualTo(2)
    assertThat(educations).hasSize(1)
    assertThat(education1).isEqualTo(educations.getContent()[0])
  }

  @Test
  fun pagedFindAllByNomisIdPage2() {
    val educations = repository.findAllByNomisId(offenderNumber, PageRequest.of(1, pageSize))

    assertThat(educations.totalElements).isEqualTo(2)
    assertThat(educations).hasSize(1)
    assertThat(education2).isEqualTo(educations.getContent()[0])
  }

  @Test
  fun bulk_testExpectedNumberOfEducationsAreReturned() {
    val educations = repository.findAllByNomisIdIn(listOf(offenderNumber))

    assertThat(educations).hasSize(2)

    assertThat(education1).isEqualTo(educations!![0])
    assertThat(education2).isEqualTo(educations[1])
  }
}
