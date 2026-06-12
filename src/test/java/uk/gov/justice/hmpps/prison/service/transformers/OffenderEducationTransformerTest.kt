package uk.gov.justice.hmpps.prison.service.transformers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationLevel
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducationAddress
import uk.gov.justice.hmpps.prison.repository.jpa.model.StudyArea
import java.time.LocalDate

class OffenderEducationTransformerTest {
  private val transformer = OffenderEducationTransformer()

  @Test
  fun convert() {
    val address: OffenderEducationAddress = OffenderEducationAddress.builder()
      .addressId(1L)
      .build()

    val offenderEducation = OffenderEducation
      .builder()
      .id(OffenderEducation.PK(1L, 2L))
      .startDate(LocalDate.now().minusDays(5))
      .endDate(LocalDate.now())
      .studyArea(StudyArea("GEN", "General Studies"))
      .educationLevel(EducationLevel("DEG", "Degree Level or Higher"))
      .numberOfYears(1)
      .graduationYear("2018")
      .comment("Good student")
      .school("School of economics")
      .isSpecialEducation(false)
      .addresses(listOf(address))
      .build()

    val actual = transformer.convert(offenderEducation)

    assertThat(actual!!.bookingId).isEqualTo(offenderEducation.id.bookingId)
    assertThat(actual.startDate).isEqualTo(offenderEducation.startDate)
    assertThat(actual.endDate).isEqualTo(offenderEducation.endDate)
    assertThat(actual.studyArea).isEqualTo(offenderEducation.studyArea.description)
    assertThat(actual.educationLevel)
      .isEqualTo(offenderEducation.educationLevel.description)
    assertThat(actual.numberOfYears).isEqualTo(offenderEducation.numberOfYears)
    assertThat(actual.graduationYear).isEqualTo(offenderEducation.graduationYear)
    assertThat(actual.comment).isEqualTo(offenderEducation.comment)
    assertThat(actual.school).isEqualTo(offenderEducation.school)
    assertThat(actual.isSpecialEducation).isEqualTo(offenderEducation.isSpecialEducation)

    assertThat(actual.addresses).hasSize(1)
    assertThat(actual.addresses[0].addressId).isEqualTo(address.addressId)
  }

  @Test
  fun convertWithMissingNestedFields() {
    val offenderEducation = OffenderEducation
      .builder()
      .id(OffenderEducation.PK(1L, 2L))
      .startDate(LocalDate.now().minusDays(5))
      .endDate(LocalDate.now())
      .numberOfYears(1)
      .graduationYear("2018")
      .comment("Good student")
      .school("School of economics")
      .isSpecialEducation(false)
      .build()

    val actual = transformer.convert(offenderEducation)

    assertThat(actual!!.bookingId).isEqualTo(offenderEducation.id.bookingId)
    assertThat(actual.startDate).isEqualTo(offenderEducation.startDate)
    assertThat(actual.endDate).isEqualTo(offenderEducation.endDate)
    assertThat(actual.numberOfYears).isEqualTo(offenderEducation.numberOfYears)
    assertThat(actual.graduationYear).isEqualTo(offenderEducation.graduationYear)
    assertThat(actual.comment).isEqualTo(offenderEducation.comment)
    assertThat(actual.school).isEqualTo(offenderEducation.school)
    assertThat(actual.isSpecialEducation).isEqualTo(offenderEducation.isSpecialEducation)

    assertThat(actual.addresses).hasSize(0)
  }
}
