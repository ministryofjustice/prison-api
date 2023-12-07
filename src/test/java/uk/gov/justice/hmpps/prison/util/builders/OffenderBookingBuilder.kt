package uk.gov.justice.hmpps.prison.util.builders

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking
import uk.gov.justice.hmpps.prison.api.model.RequestForTemporaryAbsenceArrival
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToCourt
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToTemporaryAbsence
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
  var payStatuses: List<OffenderPayStatusBuilder> = emptyList(),
  var noPayPeriods: List<OffenderNoPayPeriodBuilder> = emptyList(),
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
    dataLoader: DataLoaderRepository,
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
      .returnResult<InmateDetail>().responseBody.blockFirst()!!.also {
      if (released) {
        OffenderBookingReleaseBuilder(
          offenderNo = offenderNo,
        ).release(
          webTestClient = webTestClient,
          jwtAuthenticationHelper = jwtAuthenticationHelper,
        )
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
      this.payStatuses.forEach {
        it.save(bookingId = inmateDetail.bookingId, dataLoader = dataLoader)
      }
      this.noPayPeriods.forEach {
        it.save(bookingId = inmateDetail.bookingId, dataLoader = dataLoader)
      }
    }
  }
}

class OffenderBookingReleaseBuilder(
  val offenderNo: String,
  val movementReasonCode: String = "CR",
  val commentText: String = "released prisoner today",
  val releaseTime: LocalDateTime = LocalDateTime.now().minusHours(1),
) : WebClientEntityBuilder() {
  fun release(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
  ) {
    webTestClient.put()
      .uri("/api/offenders/{nomsId}/release", offenderNo)
      .headers(
        setAuthorisation(
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          roles = listOf("ROLE_RELEASE_PRISONER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
          {
            "movementReasonCode":"$movementReasonCode",
            "commentText":"$commentText",
            "releaseTime": "${releaseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            
          }
          """.trimIndent(),
        ),
      )
      .exchange()
      .expectStatus().isOk
  }
}

class OffenderBookingRecallBuilder(
  val offenderNo: String,
  val prisonId: String,
  val movementReasonCode: String,
  val commentText: String,
  val recallTime: LocalDateTime = LocalDateTime.now().minusHours(1),
) : WebClientEntityBuilder() {
  fun recall(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
  ) {
    webTestClient.put()
      .uri("/api/offenders/{offenderNo}/recall", offenderNo)
      .headers(
        setAuthorisation(
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          roles = listOf("ROLE_TRANSFER_PRISONER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          // language=json
          """
          {
            "prisonId": "$prisonId", 
            "imprisonmentStatus": "CUR_ORA", 
            "movementReasonCode":"$movementReasonCode",
            "commentText":"$commentText",
            "recallTime": "${recallTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
          }
          """.trimIndent(),
        ),
      )
      .exchange()
      .expectStatus().isOk
  }
}
class OffenderBookingCourtTransferBuilder(
  private val offenderNo: String,
  private val movementReasonCode: String,
  private val commentText: String,
) : WebClientEntityBuilder() {
  fun toCourt(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
    releaseTime: LocalDateTime,
  ) {
    webTestClient.put()
      .uri("/api/offenders/{nomsId}/court-transfer-out", offenderNo)
      .headers(
        setAuthorisation(
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          roles = listOf("ROLE_TRANSFER_PRISONER_ALPHA"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          RequestToTransferOutToCourt.builder()
            .toLocation("COURT1")
            .movementTime(releaseTime)
            .transferReasonCode(movementReasonCode)
            .commentText(commentText)
            .shouldReleaseBed(false)
            .courtEventId(null)
            .build(),
        ),
      )
      .exchange()
      .expectStatus().isOk
  }
  fun fromCourt(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
    returnTime: LocalDateTime,
    prisonId: String,
  ) {
    webTestClient.put()
      .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
      .headers(
        setAuthorisation(
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          roles = listOf("ROLE_TRANSFER_PRISONER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          RequestForCourtTransferIn.builder()
            .agencyId(prisonId)
            .dateTime(returnTime)
            .movementReasonCode(movementReasonCode)
            .commentText(commentText)
            .build(),
        ),
      )
      .exchange()
      .expectStatus().isOk
  }
}

class OffenderBookingTAPTransferBuilder(
  private val offenderNo: String,
  private val movementReasonCode: String,
  private val commentText: String,
) : WebClientEntityBuilder() {
  fun temporaryAbsenceRelease(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
    releaseTime: LocalDateTime,
  ) {
    webTestClient.put()
      .uri("/api/offenders/{nomsId}/temporary-absence-out", offenderNo)
      .headers(
        setAuthorisation(
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          roles = listOf("ROLE_TRANSFER_PRISONER_ALPHA"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          RequestToTransferOutToTemporaryAbsence.builder()
            .toCity("18248")
            .movementTime(releaseTime)
            .transferReasonCode(movementReasonCode)
            .commentText(commentText)
            .shouldReleaseBed(false)
            .scheduleEventId(null)
            .build(),
        ),
      )
      .exchange()
      .expectStatus().isOk
  }
  fun temporaryAbsenceReturn(
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper,
    returnTime: LocalDateTime,
    prisonId: String,
  ) {
    webTestClient.put()
      .uri("/api/offenders/{nomsId}/temporary-absence-arrival", offenderNo)
      .headers(
        setAuthorisation(
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          roles = listOf("ROLE_TRANSFER_PRISONER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          RequestForTemporaryAbsenceArrival.builder()
            .agencyId(prisonId)
            .dateTime(returnTime)
            .movementReasonCode(movementReasonCode)
            .commentText(commentText)
            .build(),
        ),
      )
      .exchange()
      .expectStatus().isOk
  }
}
