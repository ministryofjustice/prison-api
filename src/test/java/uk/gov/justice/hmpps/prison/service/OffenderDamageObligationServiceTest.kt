package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderDamageObligationRepository
import java.math.BigDecimal
import java.time.LocalDateTime

class OffenderDamageObligationServiceTest {
  private val repository: OffenderDamageObligationRepository = mock()
  private val service = OffenderDamageObligationService(repository)

  @Test
  fun callRepositoryWithOffenderNoOnly() {
    service.getDamageObligations("A1234", null)

    verify(repository).findOffenderDamageObligationByOffender_NomsId("A1234")
  }

  @Test
  fun callRepositoryWithOffenderNoAndStatus() {
    service.getDamageObligations("A1234", OffenderDamageObligation.Status.ACTIVE)

    verify(repository).findOffenderDamageObligationByOffender_NomsIdAndStatus("A1234", "ACTIVE")
  }

  @Test
  fun transferIntoCorrectApiModel() {
    whenever(
      repository.findOffenderDamageObligationByOffender_NomsIdAndStatus(
        any(),
        any(),
      ),
    ).thenReturn(
      listOf(
        OffenderDamageObligation.builder()
          .id(1L)
          .comment("Broken canteen table")
          .amountToPay(BigDecimal.valueOf(500))
          .amountPaid(BigDecimal.ZERO)
          .startDateTime(LocalDateTime.parse("2020-10-10T10:00"))
          .endDateTime(LocalDateTime.parse("2020-10-22T10:00"))
          .offender(Offender.builder().nomsId("A12345").build())
          .referenceNumber("123")
          .status("ACTIVE")
          .comment("test")
          .prison(AgencyLocation.builder().id("MDI").description("Moorland").build())
          .build(),
      ),
    )

    val outstandingDamageBalance =
      service.getDamageObligations("A1234", OffenderDamageObligation.Status.ACTIVE).first()

    assertThat(outstandingDamageBalance.id).isEqualTo(1L)
    assertThat(outstandingDamageBalance.offenderNo).isEqualTo("A12345")
    assertThat(outstandingDamageBalance.prisonId).isEqualTo("MDI")
    assertThat(outstandingDamageBalance.referenceNumber).isEqualTo("123")
    assertThat(outstandingDamageBalance.startDateTime).isEqualTo("2020-10-10T10:00")
    assertThat(outstandingDamageBalance.endDateTime).isEqualTo("2020-10-22T10:00")
    assertThat(outstandingDamageBalance.comment).isEqualTo("test")
    assertThat(outstandingDamageBalance.amountPaid).isEqualTo("0")
    assertThat(outstandingDamageBalance.amountToPay).isEqualTo("500")
    assertThat(outstandingDamageBalance.status).isEqualTo("ACTIVE")
  }

  @Test
  fun handleNullValuesOnTransformWithoutCrashing() {
    whenever(
      repository.findOffenderDamageObligationByOffender_NomsIdAndStatus(
        any(),
        any(),
      ),
    ).thenReturn(
      listOf(
        OffenderDamageObligation.builder().offender(Offender.builder().build()).build(),
      ),
    )
    service.getDamageObligations("A1234", OffenderDamageObligation.Status.ACTIVE).first()
  }
}
