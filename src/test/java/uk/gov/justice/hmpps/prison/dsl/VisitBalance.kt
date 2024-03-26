package uk.gov.justice.hmpps.prison.dsl

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.BookingRepository

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

@NomisDataDslMarker
class VisitBalanceBuilder(
  private val repository: VisitBalanceBuilderRepository,
) {
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
