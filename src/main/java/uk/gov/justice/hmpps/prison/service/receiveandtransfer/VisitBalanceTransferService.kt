package uk.gov.justice.hmpps.prison.service.receiveandtransfer

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking

@Service
class VisitBalanceTransferService {
  fun adjustVisitBalances(booking: OffenderBooking) {
    // see SDU-187 - if that goes live in NOMIS then we will need the same logic here
    // That would update visit order balances for recalls and where there has been no recent manual adjustment
    // see the latest version of oidamis.create_vo_allowance with code that was due to go live
  }
}
