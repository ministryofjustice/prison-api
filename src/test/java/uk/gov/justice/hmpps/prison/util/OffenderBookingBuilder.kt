package uk.gov.justice.hmpps.prison.util

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.IepLevelAndComment
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.service.BadRequestException
import java.time.LocalDateTime

class OffenderBookingBuilder(
  var prisonId: String = "MDI",
  var bookingInTime: LocalDateTime = LocalDateTime.now(),
  var fromLocationId: String = "OUT",
  var movementReasonCode: String = "N",
  var youthOffender: Boolean = false,
  var cellLocation: String? = null,
  var imprisonmentStatus: String = "SENT03",
  var iepLevel: String? = null,
  var iepLevelComment: String = "iep level comment",
  var voBalance: Int? = null,
  var pvoBalance: Int? = null,
) : WebClientEntityBuilder() {

  fun withIEPLevel(iepLevel: String): OffenderBookingBuilder {
    this.iepLevel = iepLevel
    return this
  }

  fun withInitialVoBalances(voBalance: Int, pvoBalance: Int): OffenderBookingBuilder {
    this.voBalance = voBalance
    this.pvoBalance = pvoBalance
    return this
  }

  fun save(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
    offenderNo: String,
    bookingRepository: BookingRepository? = null
  ): InmateDetail {

    val request =
      RequestForNewBooking.builder().bookingInTime(bookingInTime).cellLocation(cellLocation)
        .fromLocationId(fromLocationId).imprisonmentStatus(imprisonmentStatus).movementReasonCode(movementReasonCode)
        .prisonId(prisonId).youthOffender(youthOffender).build()

    return webTestClient.post()
      .uri("/api/offenders/{offenderNo}/booking", offenderNo)
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
      .returnResult<InmateDetail>().responseBody.blockFirst()!!.also {
      this.iepLevel?.run {
        webTestClient.post()
          .uri("/api/bookings/{bookingId}/iepLevels", it.bookingId)
          .headers(
            setAuthorisation(
              jwtAuthenticationHelper = jwtAuthenticationHelper,
              roles = listOf("ROLE_MAINTAIN_IEP")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(IepLevelAndComment.builder().iepLevel(iepLevel).comment(iepLevelComment).build())
          )
          .exchange()
          .expectStatus().is2xxSuccessful
      }
    }.also {
      this.voBalance?.run {
        bookingRepository?.createBookingVisitOrderBalances(it.bookingId, voBalance, pvoBalance)
          ?: throw BadRequestException("No booking repository provided")
      }
    }
  }
}
