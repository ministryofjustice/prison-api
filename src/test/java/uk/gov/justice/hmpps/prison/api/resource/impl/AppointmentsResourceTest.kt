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
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
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
import java.util.Optional

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
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
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

    webTestClient.delete().uri("/api/appointments/1")
      .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent
    verify(offenderIndividualScheduleRepository).findById(1L)
  }

  @Nested
  @DisplayName("DELETE /api/appointments/{appointmentId}")
  inner class DeleteAppointment {
    @Test
    fun deleteAnAppointment_notFound() {
      whenever(offenderIndividualScheduleRepository.findById(1L)).thenReturn(Optional.empty())

      webTestClient.delete().uri("/api/appointments/1")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
      verify(offenderIndividualScheduleRepository).findById(1L)
    }

    @Test
    fun `delete an appointment not authorised`() {
      webTestClient.delete().uri("/api/appointments/1")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
      verifyNoInteractions(offenderIndividualScheduleRepository)
    }

    @Test
    fun `delete an appointment forbidden`() {
      webTestClient.delete().uri("/api/appointments/1")
        .headers(setAuthorisation(listOf("ROLE_BOB")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
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

      webTestClient.get().uri("/api/appointments/1")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody(ScheduledEvent::class.java).isEqualTo(ScheduledEvent(appointment))
      verify(offenderIndividualScheduleRepository).findById(1L)
    }

    @Test
    fun getAnAppointment_notFound() {
      whenever(offenderIndividualScheduleRepository.findById(1L)).thenReturn(Optional.empty())

      webTestClient.get().uri("/api/appointments/1")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound

      verify(offenderIndividualScheduleRepository).findById(1L)
    }

    @Test
    fun `get an appointment forbidden`() {
      webTestClient.get().uri("/api/appointments/1")
        .headers(setAuthorisation(listOf("ROLE_BOB")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
      verifyNoInteractions(offenderIndividualScheduleRepository)
    }

    @Test
    fun `get an appointment not authorised`() {
      webTestClient.get().uri("/api/appointments/1")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
      verifyNoInteractions(offenderIndividualScheduleRepository)
    }
  }

  private fun headers(token: String, contentType: MediaType): HttpHeaders {
    val headers = HttpHeaders()
    headers.contentType = contentType
    headers.setBearerAuth(token)
    return headers
  }

  @Nested
  @DisplayName("PUT /{appointmentId}/comment")
  inner class UpdateComment {
    @Test
    fun updateAppointmentComment() {
      val appointment = appointmentWithId(1)
      whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.of(appointment))

      webTestClient.put().uri("/api/appointments/1/comment")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", TEXT_PLAIN_VALUE)
        .bodyValue("Comment")
        .exchange()
        .expectStatus().isNoContent

      assertThat(appointment.comment).isEqualTo("Comment")
    }

    @Test
    fun updateAppointmentComment_emptyComment() {
      val appointment = appointmentWithId(1).apply { comment = "existing comment" }
      whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.of(appointment))

      webTestClient.put().uri("/api/appointments/1/comment")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", TEXT_PLAIN_VALUE)
        .bodyValue("")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent

      assertThat(appointment.comment).isNull()
    }

    @Test
    fun updateAppointmentComment_noComment() {
      val appointment = appointmentWithId(1).apply { comment = "existing comment" }
      whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.of(appointment))

      webTestClient.put().uri("/api/appointments/1/comment")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", TEXT_PLAIN_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent

      assertThat(appointment.comment).isNull()
    }

    @Test
    fun updateAppointmentComment_notFound() {
      whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.empty())

      webTestClient.put().uri("/api/appointments/1/comment")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", TEXT_PLAIN_VALUE)
        .bodyValue("Comment")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `update appointment comment forbidden`() {
      webTestClient.put().uri("/api/appointments/1/comment")
        .headers(setAuthorisation(listOf("ROLE_BOB")))
        .header("Content-Type", TEXT_PLAIN_VALUE)
        .bodyValue("")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden

      verifyNoInteractions(offenderIndividualScheduleRepository)
    }

    @Test
    fun `update appointment comment not authorised`() {
      webTestClient.put().uri("/api/appointments/1/comment")
        .header("Content-Type", TEXT_PLAIN_VALUE)
        .bodyValue("")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized

      verifyNoInteractions(offenderIndividualScheduleRepository)
    }
  }

  @Nested
  @DisplayName("PUT /{appointmentId}/comment/v2")
  inner class UpdateCommentV2 {
    @Test
    fun updateAppointmentComment() {
      val appointment = appointmentWithId(1)
      whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.of(appointment))

      webTestClient.put().uri("/api/appointments/1/comment/v2")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("""{ "comment": "Comment" } """)
        .exchange()
        .expectStatus().isNoContent

      assertThat(appointment.comment).isEqualTo("Comment")
    }

    @Test
    fun updateAppointmentComment_emptyComment() {
      val appointment = appointmentWithId(1).apply { comment = "existing comment" }
      whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.of(appointment))

      webTestClient.put().uri("/api/appointments/1/comment/v2")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("""{ "comment": "" } """)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent

      assertThat(appointment.comment).isNull()
    }

    @Test
    fun updateAppointmentComment_noComment() {
      val appointment = appointmentWithId(1).apply { comment = "existing comment" }
      whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.of(appointment))

      webTestClient.put().uri("/api/appointments/1/comment/v2")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent

      assertThat(appointment.comment).isNull()
    }

    @Test
    fun updateAppointmentComment_notFound() {
      whenever(offenderIndividualScheduleRepository.findById(1)).thenReturn(Optional.empty())

      webTestClient.put().uri("/api/appointments/1/comment/v2")
        .headers(setAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("""{ "comment": "Comment" } """)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `update appointment comment forbidden`() {
      webTestClient.put().uri("/api/appointments/1/comment/v2")
        .headers(setAuthorisation(listOf("ROLE_BOB")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("""{ "comment": "Comment" } """)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden

      verifyNoInteractions(offenderIndividualScheduleRepository)
    }

    @Test
    fun `update appointment comment not authorised`() {
      webTestClient.put().uri("/api/appointments/1/comment/v2")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("""{ "comment": "Comment" } """)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized

      verifyNoInteractions(offenderIndividualScheduleRepository)
    }
  }
}
