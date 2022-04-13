package uk.gov.justice.hmpps.prison.util

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking
import java.time.LocalDateTime

class OffenderBookingBuilder(
  private val webTestClient: WebTestClient,
  jwtAuthenticationHelper: JwtAuthenticationHelper,
  var prisonId: String = "MDI",
  var bookingInTime: LocalDateTime = LocalDateTime.now(),
  var fromLocationId: String = "OUT",
  var movementReasonCode: String = "N",
  var youthOffender: Boolean = false,
  var cellLocation: String? = null,
  var imprisonmentStatus: String = "SENT03"
) : WebClientEntityBuilder(jwtAuthenticationHelper) {
  fun save(offenderNo: String): InmateDetail {
    val request =
      RequestForNewBooking.builder().bookingInTime(bookingInTime).cellLocation(cellLocation)
        .fromLocationId(fromLocationId).imprisonmentStatus(imprisonmentStatus).movementReasonCode(movementReasonCode)
        .prisonId(prisonId).youthOffender(youthOffender).build()

    return webTestClient.post()
      .uri("/api/offenders/{offenderNo}/booking", offenderNo)
      .headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(request)
      )
      .exchange()
      .expectStatus().isOk
      .returnResult<InmateDetail>().responseBody.blockFirst()!!
  }
}
