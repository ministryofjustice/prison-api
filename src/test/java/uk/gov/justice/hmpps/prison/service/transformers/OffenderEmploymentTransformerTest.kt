package uk.gov.justice.hmpps.prison.service.transformers

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.gov.justice.hmpps.prison.repository.jpa.model.EmploymentSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.EmploymentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.Occupation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmploymentAddress
import uk.gov.justice.hmpps.prison.repository.jpa.model.PayPeriod
import java.math.BigDecimal
import java.time.LocalDate

class OffenderEmploymentTransformerTest {
  private val transformer = OffenderEmploymentTransformer()

  @Test
  fun convert() {
    val address: OffenderEmploymentAddress = OffenderEmploymentAddress.builder()
      .addressId(1L)
      .build()

    val offenderEmployment = OffenderEmployment
      .builder()
      .id(OffenderEmployment.PK(1L, 2L))
      .startDate(LocalDate.now().minusDays(5))
      .endDate(LocalDate.now())
      .postType(EmploymentStatus("CAS", "Casual"))
      .employerName("greggs")
      .supervisorName("lorem")
      .position("ipsum")
      .terminationReason("end of program")
      .wage(BigDecimal.valueOf(5000.55))
      .wagePeriod(PayPeriod("WEEK", "Weekly"))
      .occupation(Occupation("COOK", "Cook"))
      .comment("Good cook")
      .scheduleType(EmploymentSchedule("FTNIGHT", "Fortnightly"))
      .hoursWeek(30)
      .isEmployerAware(true)
      .isEmployerContactable(false)
      .addresses(listOf(address))
      .build()

    val actual = transformer.convert(offenderEmployment)

    assertThat(actual!!.bookingId).isEqualTo(offenderEmployment.id.bookingId)
    assertThat(actual.startDate).isEqualTo(offenderEmployment.startDate)
    assertThat(actual.endDate).isEqualTo(offenderEmployment.endDate)
    assertThat(actual.postType).isEqualTo(offenderEmployment.postType.description)
    assertThat(actual.employerName).isEqualTo(offenderEmployment.employerName)
    assertThat(actual.supervisorName).isEqualTo(offenderEmployment.supervisorName)
    assertThat(actual.position).isEqualTo(offenderEmployment.position)
    assertThat(actual.terminationReason).isEqualTo(offenderEmployment.terminationReason)
    assertThat(actual.wage).isEqualTo(offenderEmployment.wage)
    assertThat(actual.wagePeriod).isEqualTo(offenderEmployment.wagePeriod.description)
    assertThat(actual.occupation).isEqualTo(offenderEmployment.occupation.description)
    assertThat(actual.comment).isEqualTo(offenderEmployment.comment)
    assertThat(actual.schedule).isEqualTo(offenderEmployment.scheduleType.description)
    assertThat(actual.hoursWeek).isEqualTo(offenderEmployment.hoursWeek)
    assertThat(actual.isEmployerAware).isEqualTo(offenderEmployment.isEmployerAware)
    assertThat(actual.isEmployerContactable).isEqualTo(offenderEmployment.isEmployerContactable)

    assertThat(actual.addresses).hasSize(1)
    assertThat(actual.addresses[0].addressId).isEqualTo(address.addressId)
  }

  @Test
  fun convertWithMissingNestedFields() {
    val offenderEmployment = OffenderEmployment
      .builder()
      .id(OffenderEmployment.PK(1L, 2L))
      .startDate(LocalDate.now().minusDays(5))
      .endDate(LocalDate.now())
      .employerName("greggs")
      .supervisorName("lorem")
      .position("ipsum")
      .terminationReason("end of program")
      .wage(BigDecimal.valueOf(5000.55))
      .comment("Good cook")
      .hoursWeek(30)
      .isEmployerAware(true)
      .isEmployerContactable(false)
      .build()

    val actual = transformer.convert(offenderEmployment)

    assertThat(actual!!.bookingId).isEqualTo(offenderEmployment.id.bookingId)
    assertThat(actual.startDate).isEqualTo(offenderEmployment.startDate)
    assertThat(actual.endDate).isEqualTo(offenderEmployment.endDate)
    assertThat(actual.postType).isNull()
    assertThat(actual.employerName).isEqualTo(offenderEmployment.employerName)
    assertThat(actual.supervisorName).isEqualTo(offenderEmployment.supervisorName)
    assertThat(actual.position).isEqualTo(offenderEmployment.position)
    assertThat(actual.terminationReason).isEqualTo(offenderEmployment.terminationReason)
    assertThat(actual.wage).isEqualTo(offenderEmployment.wage)
    assertThat(actual.wagePeriod).isNull()
    assertThat(actual.occupation).isNull()
    assertThat(actual.comment).isEqualTo(offenderEmployment.comment)
    assertThat(actual.schedule).isNull()
    assertThat(actual.hoursWeek).isEqualTo(offenderEmployment.hoursWeek)
    assertThat(actual.isEmployerAware).isEqualTo(offenderEmployment.isEmployerAware)
    assertThat(actual.isEmployerContactable).isEqualTo(offenderEmployment.isEmployerContactable)

    assertThat(actual.addresses).hasSize(0)
  }
}
