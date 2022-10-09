package uk.gov.justice.hmpps.prison.util.builders

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.IepLevelAndComment
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OffenderBookingBuilder(
  var prisonId: String = "MDI",
  var bookingInTime: LocalDateTime = LocalDateTime.now().minusDays(1),
  var fromLocationId: String? = null,
  var movementReasonCode: String = "N",
  var cellLocation: String? = null,
  var imprisonmentStatus: String = "SENT03",
  var iepLevel: String? = null,
  var iepLevelComment: String = "iep level comment",
  var voBalance: Int? = null,
  var pvoBalance: Int? = null,
  var programProfiles: List<OffenderProgramProfileBuilder> = emptyList(),
  var courtCases: List<OffenderCourtCaseBuilder> = emptyList(),
  var teamAssignment: OffenderTeamAssignmentBuilder? = null,
  var released: Boolean = false,
  var youthOffender: Boolean = false,
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

  fun withProgramProfiles(vararg programProfiles: OffenderProgramProfileBuilder): OffenderBookingBuilder {
    this.programProfiles = programProfiles.toList()
    return this
  }

  fun withCourtCases(vararg courtCases: OffenderCourtCaseBuilder): OffenderBookingBuilder {
    this.courtCases = courtCases.toList()
    return this
  }

  fun withTeamAssignment(teamAssignment: OffenderTeamAssignmentBuilder): OffenderBookingBuilder {
    this.teamAssignment = teamAssignment
    return this
  }

  fun save(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
    offenderNo: String,
    dataLoader: DataLoaderRepository
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
      if (released) {
        webTestClient.put()
          .uri("/api/offenders/{nomsId}/release", offenderNo)
          .headers(
            setAuthorisation(
              jwtAuthenticationHelper = jwtAuthenticationHelper,
              roles = listOf("ROLE_RELEASE_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "movementReasonCode":"CR",
            "commentText":"released prisoner today",
            "movementTime": "${LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk
      }
    }.also { inmateDetail ->
      this.voBalance?.run {
        dataLoader.bookingRepository.createBookingVisitOrderBalances(inmateDetail.bookingId, voBalance, pvoBalance)
      }
      this.programProfiles.forEach {
        it.save(offenderBookingId = inmateDetail.bookingId, prisonId = inmateDetail.agencyId, dataLoader = dataLoader)
      }
      this.courtCases.forEach {
        it.save(offenderBookingId = inmateDetail.bookingId, dataLoader = dataLoader)
      }
      this.teamAssignment?.also {
        it.save(offenderBookingId = inmateDetail.bookingId, dataLoader = dataLoader)
      }
    }
  }
}
