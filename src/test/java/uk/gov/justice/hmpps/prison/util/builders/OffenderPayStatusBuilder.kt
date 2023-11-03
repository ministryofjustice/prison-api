package uk.gov.justice.hmpps.prison.util.builders

import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPayStatus
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import java.time.LocalDate
import java.time.LocalDateTime

class OffenderPayStatusBuilder(
  private val startDate: LocalDate = LocalDate.of(2016, 11, 9),
  private val endDate: LocalDate? = null,
  private val specialPayTypeCode: String = "RETIRED",
) {
  fun save(
    bookingId: Long,
    dataLoader: DataLoaderRepository,
  ) {
    val offenderBooking = dataLoader.offenderBookingRepository.findByBookingId(bookingId).orElseThrow(
      BadRequestException("booking $bookingId not found"),
    )
    val offenderPayStatus = OffenderPayStatus.builder()
      .bookingId(offenderBooking.bookingId)
      .startDate(startDate)
      .endDate(endDate)
      .specialPayTypeCode(specialPayTypeCode)
      .createDatetime(LocalDateTime.now())
      .createUserId("BUILDER")
      .build()
    dataLoader.offenderPayStatusRepository.save(offenderPayStatus)
  }
}
