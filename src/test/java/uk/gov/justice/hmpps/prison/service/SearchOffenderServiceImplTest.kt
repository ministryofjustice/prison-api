package uk.gov.justice.hmpps.prison.service

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anySet
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isA
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking
import uk.gov.justice.hmpps.prison.api.support.Page
import uk.gov.justice.hmpps.prison.repository.InmateRepository
import uk.gov.justice.hmpps.prison.repository.OffenderBookingSearchRequest
import uk.gov.justice.hmpps.prison.service.support.SearchOffenderRequest

class SearchOffenderServiceImplTest {
  private val bookingService: BookingService = mock()
  private val userService: UserService = mock()
  private val inmateRepository: InmateRepository = mock()

  @Test
  fun testFindOffenders_findAssessmentsCorrectlyBatchesQueries() {
    val offenderNoRegex = "^[A-Za-z]\\d{4}[A-Za-z]{2}$}"
    val maxBatchSize = 1

    val bookings = listOf(
      OffenderBooking.builder().firstName("firstName1").bookingId(1L).bookingNo("1").build(),
      OffenderBooking.builder().firstName("firstName2").bookingId(2L).bookingNo("2").build(),
    )

    whenever(
      inmateRepository.searchForOffenderBookings(isA(OffenderBookingSearchRequest::class.java)),
    ).thenReturn(Page(bookings, bookings.size.toLong(), 0, bookings.size.toLong()))

    val service = SearchOffenderService(bookingService, userService, inmateRepository, offenderNoRegex, maxBatchSize)

    service.findOffenders(
      SearchOffenderRequest.builder().keywords("firstName").locationPrefix("LEI").returnCategory(true).build(),
    )

    verify(inmateRepository).findAssessments(
      eq(mutableListOf(1L)),
      anyString(),
      anySet(),
    )
    verify(inmateRepository).findAssessments(
      eq(mutableListOf(2L)),
      anyString(),
      anySet(),
    )
  }
}
