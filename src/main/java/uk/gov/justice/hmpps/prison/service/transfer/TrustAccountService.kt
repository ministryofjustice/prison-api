package uk.gov.justice.hmpps.prison.service.transfer

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.FinanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking

interface TrustAccountService {
  fun createTrustAccount(booking: OffenderBooking, lastMovement: ExternalMovement, movementReason: MovementReason)
}

@Service
@Profile("nomis")
class TrustAccountSPService(val financeRepository: FinanceRepository) : TrustAccountService {
  override fun createTrustAccount(
    booking: OffenderBooking,
    lastMovement: ExternalMovement,
    movementReason: MovementReason
  ) {
    financeRepository.createTrustAccount(
      lastMovement.toAgency.id,
      booking.bookingId,
      booking.rootOffender.id,
      lastMovement.fromAgency.id,
      movementReason.code,
      null,
      null,
      lastMovement.toAgency.id
    )
  }
}

@Service
@Profile("!nomis")
class TrustAccountNoopService : TrustAccountService {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  override fun createTrustAccount(
    booking: OffenderBooking,
    lastMovement: ExternalMovement,
    movementReason: MovementReason
  ) {
    log.warn("Not running against NOMIS database so will not create Trust accounts")
  }
}
