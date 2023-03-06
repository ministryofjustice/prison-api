package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpMethod
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import java.time.LocalDateTime

@DisplayName("OffenderResource get Events")
class OffenderResourceIntTest_getEvents : ResourceTest() {
  @SpyBean
  lateinit var bookingRepository: BookingRepository

  @Test
  fun events() {
    whenever(bookingRepository.getBookingActivities(anyLong(), any(), any(), anyString(), any())).thenReturn(
      listOf(
        createEvent("act", "10:11:12"),
        createEvent("act", "08:59:50"),
      ),
    )
    whenever(bookingRepository.getBookingVisits(anyLong(), any(), any(), anyString(), any())).thenReturn(
      listOf(createEvent("vis", "09:02:03")),
    )
    whenever(bookingRepository.getBookingAppointments(anyLong(), any(), any(), anyString(), any())).thenReturn(
      listOf(createEvent("app", null)),
    )
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/offenders/A1234AA/events", HttpMethod.GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "events.json")
  }

  @Test
  fun scheduledEvents() {
    whenever(bookingRepository.getBookingActivities(anyLong(), any(), any(), anyString(), any())).thenReturn(
      listOf(
        createEvent("act", "10:11:12"),
        createEvent("act", "08:59:50"),
      ),
    )
    whenever(bookingRepository.getBookingVisits(anyLong(), any(), any(), anyString(), any())).thenReturn(
      listOf(
        createEvent("vis", "09:02:03"),
        ScheduledEvent.builder().bookingId(-1L).eventType("act").eventStatus("CANC").build(),
      ),
    )
    whenever(bookingRepository.getBookingAppointments(anyLong(), any(), any(), anyString(), any())).thenReturn(
      listOf(createEvent("app", null)),
    )
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/offenders/A1234AA/scheduled-events", HttpMethod.GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "events.json")
  }

  private fun createEvent(type: String, time: String?): ScheduledEvent = ScheduledEvent.builder().bookingId(-1L)
    .startTime(time?.let { "2019-01-02T$it" }?.let { LocalDateTime.parse(it) })
    .eventType("$type$time")
    .eventSubType("some sub $type")
    .eventStatus("SCH")
    .build()
}
