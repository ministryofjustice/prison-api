package uk.gov.justice.hmpps.prison.service.receiveandtransfer

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.FinanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking

interface TrustAccountService {
  fun createTrustAccount(
    booking: OffenderBooking,
    fromAgency: AgencyLocation,
    movementIn: ExternalMovement
  )
}

@Service
@Profile("nomis")
class TrustAccountSPService(val financeRepository: FinanceRepository) : TrustAccountService {
  override fun createTrustAccount(
    booking: OffenderBooking,
    fromAgency: AgencyLocation,
    movementIn: ExternalMovement
  ) {
    financeRepository.createTrustAccount(
      movementIn.toAgency.id,
      booking.bookingId,
      booking.rootOffender.id,
      fromAgency.id,
      movementIn.movementReason.code,
      null,
      null,
      movementIn.toAgency.id
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
    fromAgency: AgencyLocation,
    movementIn: ExternalMovement
  ) {
    log.warn("Not running against NOMIS database so will not create Trust accounts")
  }
}
