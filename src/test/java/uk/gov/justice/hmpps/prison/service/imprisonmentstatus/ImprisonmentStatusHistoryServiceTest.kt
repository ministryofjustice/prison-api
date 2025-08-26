package uk.gov.justice.hmpps.prison.service.imprisonmentstatus

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.imprisonmentstatus.ImprisonmentStatusHistoryDto
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
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
    val bookingOne = OffenderBooking().withBookingId(1L).withBookNumber("One")
    val bookingTwo = OffenderBooking().withBookingId(2L).withBookNumber("Two")
    whenever(offenderImprisonmentStatusRepository.findByOffender(offenderId)).thenReturn(
      listOf(
        OffenderImprisonmentStatus()
          .withImprisonmentStatus(sentencedStatus)
          .withEffectiveDate(firstDate)
          .withImprisonStatusSeq(1)
          .withAgyLocId("KMI")
          .withOffenderBooking(bookingOne),
        OffenderImprisonmentStatus()
          .withImprisonmentStatus(remandStatus)
          .withEffectiveDate(firstDate)
          .withImprisonStatusSeq(2)
          .withAgyLocId("KMI")
          .withOffenderBooking(bookingOne),
        OffenderImprisonmentStatus()
          .withImprisonmentStatus(recallStatus)
          .withEffectiveDate(secondDate)
          .withImprisonStatusSeq(3)
          .withAgyLocId("BMI")
          .withOffenderBooking(bookingTwo),
        OffenderImprisonmentStatus()
          .withImprisonmentStatus(sentencedStatus)
          .withEffectiveDate(secondDate)
          .withImprisonStatusSeq(4)
          .withAgyLocId("BMI")
          .withOffenderBooking(bookingTwo),
      ),
    )

    val result = imprisonmentStatusHistoryService.getImprisonmentStatusHistory(offenderId)

    assertThat(
      result,
    ).isEqualTo(
      listOf(
        ImprisonmentStatusHistoryDto(
          status = "SEC38",
          effectiveDate = firstDate,
          agencyId = "KMI",
          bookingId = 1L,
          bookNumber = "One",
        ),
        ImprisonmentStatusHistoryDto(
          status = "ADIMP_ORA20",
          effectiveDate = secondDate,
          agencyId = "BMI",
          bookingId = 2L,
          bookNumber = "Two",
        ),
      ),
    )
  }
}
