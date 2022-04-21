package uk.gov.justice.hmpps.prison.service.transfer

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking

@Service
class VisitBalanceTransferService {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  fun adjustVisitBalances(booking: OffenderBooking) {
    log.debug("Will update visit balances based on current IEP level")
  }
}
