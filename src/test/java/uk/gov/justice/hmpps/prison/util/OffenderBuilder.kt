package uk.gov.justice.hmpps.prison.util

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestToCreate
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import java.time.LocalDate

class OffenderBuilder(
  var pncNumber: String? = null,
  var croNumber: String? = null,
  var lastName: String = "NTHANDA",
  var firstName: String = randomName(),
  var middleName1: String? = null,
  var middleName2: String? = null,
  var birthDate: LocalDate = LocalDate.of(1965, 7, 19),
  var genderCode: String = "M",
  var title: String? = null,
  var ethnicity: String? = null,
  var bookingBuilders: Array<OffenderBookingBuilder> = Array(1) {
    OffenderBookingBuilder()
  }

) : WebClientEntityBuilder() {

  fun withBooking(vararg bookingBuilder: OffenderBookingBuilder): OffenderBuilder {
    bookingBuilders = arrayOf(*bookingBuilder)
    return this
  }

  fun save(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
    bookingRepository: BookingRepository? = null
  ): InmateDetail {
    val request =
      RequestToCreate.builder().croNumber(croNumber).pncNumber(pncNumber).lastName(lastName).firstName(firstName)
        .middleName1(middleName1).middleName2(middleName2).dateOfBirth(birthDate).gender(genderCode)
        .ethnicity(ethnicity).build()

    val offender = webTestClient.post()
      .uri("/api/offenders")
      .headers(
        setAuthorisation(
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          roles = listOf("ROLE_BOOKING_CREATE")
        )
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(request)
      )
      .exchange()
      .expectStatus().isOk
      .returnResult<InmateDetail>().responseBody.blockFirst()!!

    return bookingBuilders.map {
      it.save(
        webTestClient = webTestClient,
        jwtAuthenticationHelper = jwtAuthenticationHelper,
        offenderNo = offender.offenderNo,
        bookingRepository = bookingRepository
      )
    }
      .lastOrNull() ?: offender
  }
}

private fun randomName(): String {
  // return random name between 3 and 10 characters long
  return (1..(3 + (Math.random() * 7).toInt())).map {
    ('a' + (Math.random() * 26).toInt())
  }.joinToString("")
}
