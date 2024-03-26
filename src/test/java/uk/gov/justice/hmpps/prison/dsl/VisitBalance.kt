package uk.gov.justice.hmpps.prison.dsl

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.BookingRepository

@NomisDataDslMarker
interface VisitBalanceDsl

@Component
class VisitBalanceBuilderRepository(
  private val bookingRepository: BookingRepository,
) {
  fun save(
    bookingId: Long,
    voBalance: Int,
    pvoBalance: Int,
  ) {
    bookingRepository.createBookingVisitOrderBalances(bookingId, voBalance, pvoBalance)
  }
}

@Component
class VisitBalanceBuilderFactory(
  private val repository: VisitBalanceBuilderRepository,
) {

  fun builder(): VisitBalanceBuilder {
    return VisitBalanceBuilder(repository)
  }
}

class VisitBalanceBuilder(
  private val repository: VisitBalanceBuilderRepository,
) : VisitBalanceDsl {
  fun build(
    offenderBookingId: OffenderBookingId,
    voBalance: Int,
    pvoBalance: Int,
  ) = repository.save(
    bookingId = offenderBookingId.bookingId,
    voBalance = voBalance,
    pvoBalance = pvoBalance,
  )
}
