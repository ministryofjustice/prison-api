package uk.gov.justice.hmpps.prison.util.builders

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestToCreate
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper
import java.time.LocalDate

class OffenderBuilder(
  private var pncNumber: String? = null,
  private var croNumber: String? = null,
  var lastName: String = "NTHANDA",
  var firstName: String = randomName(),
  private var middleName1: String? = null,
  private var middleName2: String? = null,
  private var birthDate: LocalDate = LocalDate.of(1975, 7, 19),
  private var genderCode: String = "M",
  var ethnicity: String? = null,
  private var bookingBuilders: Array<OffenderBookingBuilder> = Array(1) {
    OffenderBookingBuilder()
  },
  private var aliasBuilders: List<OffenderAliasBuilder> = emptyList(),

) : WebClientEntityBuilder() {

  fun withBooking(vararg bookingBuilder: OffenderBookingBuilder): OffenderBuilder {
    bookingBuilders = arrayOf(*bookingBuilder)
    return this
  }

  fun withAliases(vararg aliasBuilder: OffenderAliasBuilder): OffenderBuilder {
    aliasBuilders = aliasBuilder.toList()
    return this
  }

  fun save(
    testDataContext: TestDataContext,
  ): InmateDetail = save(testDataContext.webTestClient, testDataContext.jwtAuthenticationHelper, testDataContext.dataLoader)

  private fun save(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
    dataLoader: DataLoaderRepository,
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
          roles = listOf("ROLE_BOOKING_CREATE"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(request),
      )
      .exchange()
      .expectStatus().isOk
      .returnResult<InmateDetail>().responseBody.blockFirst()!!

    aliasBuilders.forEach { it.save(offender, dataLoader = dataLoader) }
    return bookingBuilders.map {
      it.save(
        webTestClient = webTestClient,
        jwtAuthenticationHelper = jwtAuthenticationHelper,
        offenderNo = offender.offenderNo,
        dataLoader = dataLoader,
      )
    }
      .lastOrNull() ?: offender
  }
}
