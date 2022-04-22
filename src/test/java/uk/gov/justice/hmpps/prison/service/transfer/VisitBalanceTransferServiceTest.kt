package uk.gov.justice.hmpps.prison.service.transfer

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking

internal class VisitBalanceTransferServiceTest {
  @Nested
  @DisplayName("adjustVisitBalances")
  inner class AdjustVisitBalances {
    val service = VisitBalanceTransferService()

    @Test
    internal fun `it does nothing, but will do soon`() {
      service.adjustVisitBalances(OffenderBooking())
    }
  }
}
