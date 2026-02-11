package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.CreatedAppointmentDetails
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.Repeat
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.RepeatPeriod
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class DeleteAppointmentIntegrationTest : ResourceTest() {
  @Test
  fun deleteMultipleAppointments() {
    val createAppointmentResponse: ResponseEntity<MutableList<CreatedAppointmentDetails>> =
      makeCreateAppointmentsRequest(
        this.createAppointmentBody,
      )
    val appointmentIds = createAppointmentResponse.getBody()!!.map { it.appointmentEventId }

    val appointments = appointmentIds.map { makeGetAppointmentDetails(it) }.mapNotNull { it.getBody()?.eventId }

    assertThat(appointments).isEqualTo(appointmentIds)

    val response: ResponseEntity<Void> = testRestTemplate.exchange(
      "/api/appointments/delete",
      HttpMethod.POST,
      createHttpEntity(validToken(mutableListOf("ROLE_GLOBAL_APPOINTMENT")), appointmentIds),
      Void::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

    val getAppointmentStatsCodes = appointmentIds
      .map { makeGetAppointmentDetails(it) }
      .map { it.statusCode.value() }

    assertThat(getAppointmentStatsCodes).containsExactly(404, 404)
  }

  @Test
  fun returnsNotAuthorised_whenMissingTheRightRole() {
    val response: ResponseEntity<Void> = testRestTemplate.exchange(
      "/api/appointments/delete",
      HttpMethod.POST,
      createHttpEntity(validToken(mutableListOf("1000")), mutableListOf(1L)),
      Void::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
  }

  private fun makeCreateAppointmentsRequest(body: AppointmentsToCreate): ResponseEntity<MutableList<CreatedAppointmentDetails>> = testRestTemplate.exchange(
    "/api/appointments",
    HttpMethod.POST,
    createHttpEntity(validToken(mutableListOf("ROLE_GLOBAL_APPOINTMENT")), body),
    object : ParameterizedTypeReference<MutableList<CreatedAppointmentDetails>>() {
    },
  )

  private fun makeGetAppointmentDetails(appointmentId: Long): ResponseEntity<ScheduledEvent> = testRestTemplate.exchange(
    "/api/appointments/{appointmentId}",
    HttpMethod.GET,
    createHttpEntity(validToken(mutableListOf("ROLE_GLOBAL_APPOINTMENT")), null),
    object : ParameterizedTypeReference<ScheduledEvent>() {
    },
    appointmentId,
  )

  private val createAppointmentBody: AppointmentsToCreate
    get() {
      val now =
        LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.SECONDS)
      val in1Hour = now.plusHours(6)

      val defaults = AppointmentDefaults
        .builder()
        .locationId(-25L)
        .appointmentType("ACTI")
        .startTime(now)
        .endTime(in1Hour)
        .build()

      return AppointmentsToCreate.builder()
        .appointmentDefaults(defaults)
        .repeat(
          Repeat.builder().repeatPeriod(RepeatPeriod.DAILY)
            .count(2).build(),
        )
        .appointments(
          listOf<AppointmentDetails>(
            AppointmentDetails
              .builder()
              .bookingId(-31L)
              .comment("Comment")
              .build(),
          ),
        ).build()
    }
}
