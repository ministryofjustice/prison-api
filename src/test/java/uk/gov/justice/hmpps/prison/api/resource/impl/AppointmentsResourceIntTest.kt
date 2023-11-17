package uk.gov.justice.hmpps.prison.api.resource.impl

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

class AppointmentsResourceIntTest : ResourceTest() {
  @Autowired
  private lateinit var bookingRepository: BookingRepository

  @Nested
  @DisplayName("POST /api/bookings/{bookingId}/appointments")
  inner class CreateAppointments {
    private fun futureDate(day: Long, minutes: Int): String =
      now.plusDays(day).withHour(14).withMinute(minutes).format(ISO_LOCAL_DATE_TIME)

    private val now: LocalDateTime = LocalDateTime.now().withNano(0)

    @Test
    fun `Appointments successfully created for tomorrow`() {
      val tomorrow: LocalDate = now.plusDays(1).toLocalDate()

      webTestClient.post().uri("/api/appointments")
        .headers(setAuthorisation(listOf("BULK_APPOINTMENTS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "appointmentDefaults": {
                "appointmentType": "ACTI",
                "locationId": -25,
                "startTime": "${futureDate(1, 30)}",
                "endTime": "${futureDate(1, 45)}",
                "comment": "A default comment"
               },
              "appointments": [
                {
                  "bookingId": -31
                },
                {
                  "bookingId": -32,
                  "startTime": "${futureDate(1, 35)}",
                  "endTime": "${futureDate(1, 55)}",
                  "comment": "Another comment"
                }
              ]
            }
          """,
          ),
        )
        .exchange()
        .expectStatus().isOk

      val appointment31 = bookingRepository.getBookingAppointments(-31, tomorrow, tomorrow, "startTime", Order.ASC)
      assertThat(appointment31.size).isEqualTo(1)
      assertThat(appointment31[0].bookingId).isEqualTo(-31)
      assertThat(appointment31[0].eventSubType).isEqualTo("ACTI")
      assertThat(appointment31[0].startTime).isEqualTo(futureDate(1, 30))
      assertThat(appointment31[0].endTime).isEqualTo(futureDate(1, 45))
      assertThat(appointment31[0].eventLocation).isEqualTo("Chapel")

      val appointment32 = bookingRepository.getBookingAppointments(-32, tomorrow, tomorrow, "startTime", Order.ASC)
      assertThat(appointment32.size).isEqualTo(1)
      assertThat(appointment32[0].bookingId).isEqualTo(-32)
      assertThat(appointment32[0].eventSubType).isEqualTo("ACTI")
      assertThat(appointment32[0].startTime).isEqualTo(futureDate(1, 35))
      assertThat(appointment32[0].endTime).isEqualTo(futureDate(1, 55))
      assertThat(appointment32[0].eventLocation).isEqualTo("Chapel")
    }

    @Test
    fun `Appointments successfully created for 2 days time`() {
      val twoDaysTime: LocalDate = now.plusDays(2).toLocalDate()

      webTestClient.post().uri("/api/appointments")
        .headers(setAuthorisation(listOf("BULK_APPOINTMENTS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "appointmentDefaults": {
                "appointmentType": "ACTI",
                "locationId": -25,
                "startTime": "${futureDate(2, 30)}",
                "comment": "A default comment"
               },
              "appointments": [
                {
                  "bookingId": -31
                 },
                {
                  "bookingId": -32,
                  "startTime": "${futureDate(2, 35)}",
                  "endTime": "${futureDate(2, 55)}",
                  "comment": "Another comment"
            }
              ],
              "repeat": {
                "repeatPeriod": "WEEKLY",
                "count": 2
              }
            }
          """,
          ),
        )
        .exchange()
        .expectStatus().isOk

      val apptOne = bookingRepository.getBookingAppointments(-31, twoDaysTime, twoDaysTime, "startTime", Order.ASC)
      assertThat(apptOne.size).isEqualTo(1)
      assertThat(apptOne[0].bookingId).isEqualTo(-31)
      assertThat(apptOne[0].eventSubType).isEqualTo("ACTI")
      assertThat(apptOne[0].startTime).isEqualTo(futureDate(2, 30))
      assertThat(apptOne[0].endTime).isNull()
      assertThat(apptOne[0].eventLocation).isEqualTo("Chapel")

      val apptTwo = bookingRepository.getBookingAppointments(-32, twoDaysTime, twoDaysTime, "startTime", Order.ASC)
      assertThat(apptTwo.size).isEqualTo(1)
      assertThat(apptTwo[0].bookingId).isEqualTo(-32)
      assertThat(apptTwo[0].eventSubType).isEqualTo("ACTI")
      assertThat(apptTwo[0].startTime).isEqualTo(futureDate(2, 35))
      assertThat(apptTwo[0].endTime).isEqualTo(futureDate(2, 55))
      assertThat(apptTwo[0].eventLocation).isEqualTo("Chapel")
    }

    @Test
    fun `Returns 400 if appointment date in past`() {
      webTestClient.post().uri("/api/appointments")
        .headers(setAuthorisation(listOf("BULK_APPOINTMENTS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "appointmentDefaults": {
                "appointmentType": "ACTI",
                "locationId": -25,
                "startTime": "2018-01-01T00:00",
                "comment": "A default comment"
               }
            }
          """,
          ),
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").toString().contains("startTime: must be a future date")
    }
  }
}
