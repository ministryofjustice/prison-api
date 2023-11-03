package uk.gov.justice.hmpps.prison.util.builders

import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNoPayPeriod
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import java.time.LocalDate
import java.time.LocalDateTime

class OffenderNoPayPeriodBuilder(
  private val startDate: LocalDate = LocalDate.of(2016, 11, 9),
  private val endDate: LocalDate? = null,
  private val reason: String = "REFUSAL",
) {
  fun save(
    bookingId: Long,
    dataLoader: DataLoaderRepository,
  ) {
    val offenderBooking = dataLoader.offenderBookingRepository.findByBookingId(bookingId).orElseThrow(
      BadRequestException("booking $bookingId not found"),
    )
    val offenderNoPayPeriod = OffenderNoPayPeriod.builder()
      .bookingId(offenderBooking.bookingId)
      .startDate(startDate)
      .endDate(endDate)
      .reasonCode(reason)
      .createDatetime(LocalDateTime.now())
      .createUserId("BUILDER")
      .build()
    dataLoader.offenderNoPayPeriodRepository.save(offenderNoPayPeriod)
  }
}
