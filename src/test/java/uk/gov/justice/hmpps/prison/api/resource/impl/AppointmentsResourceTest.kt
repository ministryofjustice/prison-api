package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.aop.ProxyUserAspect
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

class AppointmentsResourceTest : ResourceTest() {
  @SpyBean
  private lateinit var proxyUserAspect: ProxyUserAspect

  @MockBean
  private lateinit var bookingRepository: BookingRepository

  @Nested
  @DisplayName("POST /api/appointments")
  inner class CreateAppointments {
    @Test
    @Throws(Throwable::class)
    fun triggerProxyUserAspect() {
      `when`(bookingRepository.checkBookingExists(anyLong())).thenReturn(true)
      `when`(bookingRepository.findBookingsIdsInAgency(any(), anyString())).thenReturn(listOf(1L, 2L))
      // `when`(bookingRepository.createAppointment(any(), any(), anyString())).thenReturn(3L)
      makeCreateAppointmentsRequest(createAppointmentBody())
      verify(proxyUserAspect).controllerCall(any())
    }

    @Test
    fun createAnAppointment() {
      val firstEventId = 11
      val secondEventId = 12
      `when`(bookingRepository.checkBookingExists(anyLong())).thenReturn(true)
      `when`(bookingRepository.findBookingsIdsInAgency(any(), anyString())).thenReturn(listOf(1L, 2L))
      // `when`(bookingRepository.createAppointment(any(), any(), anyString())).thenReturn(firstEventId.toLong(), secondEventId.toLong())
      val appointments = createAppointmentBody()

      val response = makeCreateAppointmentsRequest(appointments)
      response.expectStatus().isOk
      response.expectBody()
        .jsonPath("length()").isEqualTo(2)
        .jsonPath("[*].appointmentEventId").value<List<Int>> { assertThat(it).containsExactlyInAnyOrder(firstEventId, secondEventId) }
        .jsonPath("[*].bookingId").value<List<Int>> {
          assertThat(it).containsExactlyInAnyOrder(
            appointments.appointments[0].bookingId.toInt(),
            appointments.appointments[1].bookingId.toInt(),
          )
        }

      // verify(bookingRepository, times(2)).createAppointment(any(), any(), anyString())
    }

    private fun makeCreateAppointmentsRequest(body: AppointmentsToCreate) =
      webTestClient.post().uri("/api/appointments")
        .headers(setAuthorisation(listOf("BULK_APPOINTMENTS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(body))
        .exchange()

    private fun createAppointmentBody(): AppointmentsToCreate {
      val now = LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.SECONDS)
      val in1Hour = now.plusHours(6)
      val bookingIds = listOf(-31L, -32L)
      val appointments = bookingIds
        .stream()
        .map { id: Long ->
          AppointmentDetails
            .builder()
            .bookingId(id)
            .comment("Comment")
            .build()
        }
        .toList()
      val defaults = AppointmentDefaults
        .builder()
        .locationId(-25L) // LEI-CHAP
        .appointmentType("ACTI") // Activity
        .startTime(now)
        .endTime(in1Hour)
        .build()
      return AppointmentsToCreate.builder()
        .appointmentDefaults(defaults)
        .appointments(appointments).build()
    }
  }

  @Test
  fun deleteAnAppointment() {
    val scheduledEvent = ScheduledEvent
      .builder()
      .eventId(1L)
      .eventType("APP")
      .eventSubType("VLB")
      .startTime(LocalDateTime.of(2020, 1, 1, 1, 1))
      .endTime(LocalDateTime.of(2020, 1, 1, 1, 31))
      .eventLocationId(2L)
      .build()
    `when`(bookingRepository.getBookingAppointmentByEventId(anyLong())).thenReturn(Optional.of(scheduledEvent))
    val response = testRestTemplate.exchange(
      "/api/appointments/1",
      DELETE,
      createHttpEntity(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), null),
      Void::class.java,
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    verify(bookingRepository).getBookingAppointmentByEventId(1L)
  }

  @Nested
  @DisplayName("DELETE /api/appointments/{appointmentId}")
  inner class DeleteAppointment {
    @Test
    fun deleteAnAppointment_notFound() {
      `when`(bookingRepository.getBookingAppointmentByEventId(1L)).thenReturn(Optional.empty())
      val response = testRestTemplate.exchange(
        "/api/appointments/1",
        DELETE,
        createHttpEntity(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), null),
        Void::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      verify(bookingRepository).getBookingAppointmentByEventId(1L)
    }

    @Test
    fun deleteAnAppointment_notAuthorised() {
      val response = testRestTemplate.exchange(
        "/api/appointments/1",
        DELETE,
        createHttpEntity(validToken(listOf()), null),
        Void::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
      verifyNoInteractions(bookingRepository)
    }
  }

  @Nested
  @DisplayName("GET /api/appointments/{appointmentId}")
  inner class GetAppointment {
    @Test
    fun getAnAppointment() {
      val scheduledEvent = ScheduledEvent
        .builder()
        .eventId(1L)
        .eventType("APP")
        .eventSubType("VLB")
        .startTime(LocalDateTime.of(2020, 1, 1, 1, 1))
        .endTime(LocalDateTime.of(2020, 1, 1, 1, 31))
        .eventLocationId(2L)
        .createUserId("user")
        .build()
      `when`(bookingRepository.getBookingAppointmentByEventId(anyLong())).thenReturn(Optional.of(scheduledEvent))
      val response = testRestTemplate.exchange(
        "/api/appointments/1",
        GET,
        createHttpEntity(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), null),
        ScheduledEvent::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      assertThat(response.body).isEqualTo(scheduledEvent)
      verify(bookingRepository).getBookingAppointmentByEventId(1L)
    }

    @Test
    fun getAnAppointment_notFound() {
      `when`(bookingRepository.getBookingAppointmentByEventId(anyLong())).thenReturn(Optional.empty())
      val response = testRestTemplate.exchange(
        "/api/appointments/1",
        GET,
        createHttpEntity(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), null),
        String::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      verify(bookingRepository).getBookingAppointmentByEventId(1L)
    }

    @Test
    fun getAnAppointment_notAuthorised() {
      `when`(bookingRepository.getBookingAppointmentByEventId(anyLong())).thenReturn(Optional.empty())
      val response = testRestTemplate.exchange(
        "/api/appointments/1",
        GET,
        createHttpEntity(validToken(listOf()), null),
        String::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
      verifyNoInteractions(bookingRepository)
    }
  }
  private fun headers(token: String, contentType: MediaType): HttpHeaders {
    val headers = HttpHeaders()
    headers.contentType = contentType
    headers.setBearerAuth(token)
    return headers
  }

  @Test
  fun updateAppointmentComment() {
    `when`(bookingRepository.updateBookingAppointmentComment(anyLong(), anyString())).thenReturn(true)
    val response = testRestTemplate
      .exchange(
        "/api/appointments/1/comment",
        PUT,
        HttpEntity(
          "Comment",
          headers(
            validToken(listOf("ROLE_GLOBAL_APPOINTMENT")),
            MediaType.TEXT_PLAIN,
          ),
        ),
        Void::class.java,
      )
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    verify(bookingRepository).updateBookingAppointmentComment(1L, "Comment")
  }

  @Test
  fun updateAppointmentComment_emptyComment() {
    `when`(bookingRepository.updateBookingAppointmentComment(anyLong(), isNull())).thenReturn(true)
    val response = testRestTemplate
      .exchange(
        "/api/appointments/1/comment",
        PUT,
        HttpEntity(
          "",
          headers(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), MediaType.TEXT_PLAIN),
        ),
        Void::class.java,
      )
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    verify(bookingRepository).updateBookingAppointmentComment(1L, null)
  }

  @Test
  fun updateAppointmentComment_noComment() {
    `when`(bookingRepository.updateBookingAppointmentComment(anyLong(), isNull())).thenReturn(true)
    val response = testRestTemplate
      .exchange(
        "/api/appointments/1/comment",
        PUT,
        HttpEntity<Any>(headers(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), MediaType.TEXT_PLAIN)),
        Void::class.java,
      )
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    verify(bookingRepository).updateBookingAppointmentComment(1L, null)
  }

  @Test
  fun updateAppointmentComment_notFound() {
    `when`(bookingRepository.updateBookingAppointmentComment(anyLong(), anyString())).thenReturn(false)
    val response = testRestTemplate
      .exchange(
        "/api/appointments/1/comment",
        PUT,
        HttpEntity(
          "Comment",
          headers(
            validToken(listOf("ROLE_GLOBAL_APPOINTMENT")),
            MediaType.TEXT_PLAIN,
          ),
        ),
        Void::class.java,
      )
    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    verify(bookingRepository).updateBookingAppointmentComment(1L, "Comment")
  }

  @Test
  fun updateAppointmentComment_unauthorised() {
    val response = testRestTemplate
      .exchange(
        "/api/appointments/1/comment",
        PUT,
        HttpEntity(
          "Comment",
          headers(
            validToken(listOf()),
            MediaType.TEXT_PLAIN,
          ),
        ),
        Void::class.java,
      )
    assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    verifyNoInteractions(bookingRepository)
  }
}
