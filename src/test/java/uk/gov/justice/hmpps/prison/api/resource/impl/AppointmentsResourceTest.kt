package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.whenever
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
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule.EventClass
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class AppointmentsResourceTest : ResourceTest() {
  @SpyBean
  private lateinit var proxyUserAspect: ProxyUserAspect

  @MockBean
  private lateinit var offenderIndividualScheduleRepository: OffenderIndividualScheduleRepository

  private fun appointmentWithId(createdId1: Long): OffenderIndividualSchedule {
    val a1 = OffenderIndividualSchedule()
    a1.id = createdId1
    a1.eventType = "APP"
    a1.eventClass = EventClass.INT_MOV
    return a1
  }

  @Nested
  @DisplayName("POST /api/appointments")
  inner class CreateAppointments {
    @Test
    @Throws(Throwable::class)
    fun triggerProxyUserAspect() {
      makeCreateAppointmentsRequest(createAppointmentBody())
      verify(proxyUserAspect).controllerCall(any())
    }

    @Test
    fun createAnAppointment() {
      val firstEventId = 11
      val secondEventId = 12
      whenever(offenderIndividualScheduleRepository.save(org.mockito.kotlin.any()))
        .thenReturn(appointmentWithId(11))
        .thenReturn(appointmentWithId(12))
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

      verify(offenderIndividualScheduleRepository, times(2)).save(any())
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
    whenever(offenderIndividualScheduleRepository.findById(1L)).thenReturn(
      Optional.of(
        OffenderIndividualSchedule().apply {
          id = 1L
          eventType = "APP"
          eventSubType = "VLB"
          startTime = LocalDateTime.of(2020, 1, 1, 1, 1)
          endTime = LocalDateTime.of(2020, 1, 1, 1, 31)
          internalLocation = AgencyInternalLocation().apply { locationId = 2L }
          fromLocation = AgencyLocation().apply { id = "WWI" }
        },
      ),
    )

    val response = testRestTemplate.exchange(
      "/api/appointments/1",
      DELETE,
      createHttpEntity(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), null),
      Void::class.java,
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    verify(offenderIndividualScheduleRepository).findById(1L)
  }

  @Nested
  @DisplayName("DELETE /api/appointments/{appointmentId}")
  inner class DeleteAppointment {
    @Test
    fun deleteAnAppointment_notFound() {
      whenever(offenderIndividualScheduleRepository.findById(1L)).thenReturn(Optional.empty())

      val response = testRestTemplate.exchange(
        "/api/appointments/1",
        DELETE,
        createHttpEntity(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), null),
        Void::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      verify(offenderIndividualScheduleRepository).findById(1L)
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
      verifyNoInteractions(offenderIndividualScheduleRepository)
    }
  }

  @Nested
  @DisplayName("GET /api/appointments/{appointmentId}")
  inner class GetAppointment {
    @Test
    fun getAnAppointment() {
      val appointment = OffenderIndividualSchedule().apply {
        id = 1L
        offenderBooking = OffenderBooking().apply { bookingId = 1L }
        eventClass = EventClass.INT_MOV
        eventType = "APP"
        eventSubType = "VLB"
        eventStatus = EventStatus("SCH", "desc")
        startTime = LocalDateTime.of(2020, 1, 1, 1, 1)
        endTime = LocalDateTime.of(2020, 1, 1, 1, 31)
        internalLocation = AgencyInternalLocation().apply { locationId = 2L }
        fromLocation = AgencyLocation().apply {
          id = "WWI"
          description = "desc"
        }
      }
      whenever(offenderIndividualScheduleRepository.findById(1L)).thenReturn(Optional.of(appointment))

      val response = testRestTemplate.exchange(
        "/api/appointments/1",
        GET,
        createHttpEntity(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), null),
        ScheduledEvent::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      assertThat(response.body).isEqualTo(ScheduledEvent(appointment))
      verify(offenderIndividualScheduleRepository).findById(1L)
    }

    @Test
    fun getAnAppointment_notFound() {
      whenever(offenderIndividualScheduleRepository.findById(1L)).thenReturn(Optional.empty())

      val response = testRestTemplate.exchange(
        "/api/appointments/1",
        GET,
        createHttpEntity(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), null),
        String::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      verify(offenderIndividualScheduleRepository).findById(1L)
    }

    @Test
    fun getAnAppointment_notAuthorised() {
      val response = testRestTemplate.exchange(
        "/api/appointments/1",
        GET,
        createHttpEntity(validToken(listOf()), null),
        String::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
      verifyNoInteractions(offenderIndividualScheduleRepository)
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
    val appointment = appointmentWithId(1)
    whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.of(appointment))

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
    assertThat(appointment.comment).isEqualTo("Comment")
  }

  @Test
  fun updateAppointmentComment_emptyComment() {
    val appointment = appointmentWithId(1).apply { comment = "existing comment" }
    whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.of(appointment))

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
    assertThat(appointment.comment).isNull()
  }

  @Test
  fun updateAppointmentComment_noComment() {
    val appointment = appointmentWithId(1).apply { comment = "existing comment" }
    whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.of(appointment))

    val response = testRestTemplate
      .exchange(
        "/api/appointments/1/comment",
        PUT,
        HttpEntity<Any>(headers(validToken(listOf("ROLE_GLOBAL_APPOINTMENT")), MediaType.TEXT_PLAIN)),
        Void::class.java,
      )
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    assertThat(appointment.comment).isNull()
  }

  @Test
  fun updateAppointmentComment_notFound() {
    whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.empty())

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
    verifyNoInteractions(offenderIndividualScheduleRepository)
  }
}
