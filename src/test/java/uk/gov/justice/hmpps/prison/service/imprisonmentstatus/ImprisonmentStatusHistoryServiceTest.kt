package uk.gov.justice.hmpps.prison.service.imprisonmentstatus

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.inmatestatus.ImprisonmentStatusHistoryDto
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImprisonmentStatusRepository
import java.time.LocalDate

class ImprisonmentStatusHistoryServiceTest {

  private val offenderImprisonmentStatusRepository = mock<OffenderImprisonmentStatusRepository>()
  private val imprisonmentStatusHistoryService = ImprisonmentStatusHistoryService(
    offenderImprisonmentStatusRepository,
  )

  @Test
  fun getInmateStatusHistory() {
    val offenderId = "ABC123"
    val remandStatus = ImprisonmentStatus().withStatus("SEC38")
    val sentencedStatus = ImprisonmentStatus().withStatus("ADIMP_ORA20")
    val recallStatus = ImprisonmentStatus().withStatus("LR")
    val firstDate = LocalDate.of(2024, 1, 1)
    val secondDate = LocalDate.of(2024, 2, 1)
    whenever(offenderImprisonmentStatusRepository.findByOffender(offenderId)).thenReturn(
      listOf(
        OffenderImprisonmentStatus()
          .withImprisonmentStatus(sentencedStatus)
          .withEffectiveDate(firstDate)
          .withImprisonStatusSeq(1)
          .withAgyLocId("KMI"),
        OffenderImprisonmentStatus()
          .withImprisonmentStatus(remandStatus)
          .withEffectiveDate(firstDate)
          .withImprisonStatusSeq(2)
          .withAgyLocId("KMI"),
        OffenderImprisonmentStatus()
          .withImprisonmentStatus(recallStatus)
          .withEffectiveDate(secondDate)
          .withImprisonStatusSeq(3)
          .withAgyLocId("BMI"),
        OffenderImprisonmentStatus()
          .withImprisonmentStatus(sentencedStatus)
          .withEffectiveDate(secondDate)
          .withImprisonStatusSeq(4)
          .withAgyLocId("BMI"),
      ),
    )

    val result = imprisonmentStatusHistoryService.getInmateStatusHistory(offenderId)

    assertThat(
      result,
    ).isEqualTo(
      listOf(
        ImprisonmentStatusHistoryDto(
          status = "SEC38",
          effectiveDate = firstDate,
          agencyId = "KMI",
        ),
        ImprisonmentStatusHistoryDto(
          status = "ADIMP_ORA20",
          effectiveDate = secondDate,
          agencyId = "BMI",
        ),
      ),
    )
  }
}
