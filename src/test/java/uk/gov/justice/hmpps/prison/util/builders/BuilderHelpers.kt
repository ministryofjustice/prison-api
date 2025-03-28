package uk.gov.justice.hmpps.prison.util.builders

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.CaseNote
import uk.gov.justice.hmpps.prison.api.model.CourtHearing
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToCourt
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToTemporaryAbsence
import uk.gov.justice.hmpps.prison.api.resource.impl.RestResponsePage
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNoPayPeriod
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPayStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

fun randomName(): String {
  // return random name between 3 and 10 characters long
  return (1..(3 + (Math.random() * 7).toInt())).map {
    ('a' + (Math.random() * 26).toInt())
  }.joinToString("")
}

data class TestDataContext(
  val webTestClient: WebTestClient,
  val jwtAuthenticationHelper: JwtAuthorisationHelper,
  val dataLoader: DataLoaderRepository,
)

fun TestDataContext.transferOut(offenderNo: String, toLocation: String = "MDI") {
  webTestClient.put()
    .uri("/api/offenders/{nomsId}/transfer-out", offenderNo)
    .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .body(
      BodyInserters.fromValue(
        """
          {
            "transferReasonCode":"NOTR",
            "commentText":"transferred prisoner today",
            "toLocation":"$toLocation",
            "movementTime": "${LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            
          }
        """.trimIndent(),
      ),
    )
    .exchange()
    .expectStatus().isOk
    .expectBody()
    .jsonPath("inOutStatus").isEqualTo("TRN")
    .jsonPath("status").isEqualTo("INACTIVE TRN")
    .jsonPath("lastMovementTypeCode").isEqualTo("TRN")
    .jsonPath("lastMovementReasonCode").isEqualTo("NOTR")
    .jsonPath("assignedLivingUnit.agencyId").isEqualTo("TRN")
    .jsonPath("assignedLivingUnit.description").doesNotExist()
}

fun TestDataContext.release(
  offenderNo: String,
  movementReasonCode: String = "CR",
  movementTime: LocalDateTime = LocalDateTime.now().minusHours(1),
) {
  webTestClient.put()
    .uri("/api/offenders/{nomsId}/release", offenderNo)
    .headers(setAuthorisation(listOf("ROLE_RELEASE_PRISONER")))
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .body(
      BodyInserters.fromValue(
        """
          {
            "movementReasonCode":"$movementReasonCode",
            "commentText":"released prisoner today",
            "movementTime": "${movementTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            
          }
        """.trimIndent(),
      ),
    )
    .exchange()
    .expectStatus().isOk
    .expectBody()
    .jsonPath("inOutStatus").isEqualTo("OUT")
    .jsonPath("status").isEqualTo("INACTIVE OUT")
    .jsonPath("lastMovementTypeCode").isEqualTo("REL")
    .jsonPath("lastMovementReasonCode").isEqualTo(movementReasonCode)
    .jsonPath("assignedLivingUnit.agencyId").isEqualTo("OUT")
    .jsonPath("assignedLivingUnit.description").doesNotExist()
}

fun TestDataContext.transferOutToCourt(offenderNo: String, toLocation: String, shouldReleaseBed: Boolean = false, courtHearingEventId: Long? = null, expectedAgency: String = "LEI"): LocalDateTime {
  val movementTime = LocalDateTime.now().minusHours(1)
  val request = RequestToTransferOutToCourt.builder()
    .toLocation(toLocation)
    .movementTime(movementTime)
    .transferReasonCode("19")
    .commentText("court appearance")
    .shouldReleaseBed(shouldReleaseBed)
    .courtEventId(courtHearingEventId)
    .build()
  webTestClient.put()
    .uri("/api/offenders/{nomsId}/court-transfer-out", offenderNo)
    .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER_ALPHA")))
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .body(BodyInserters.fromValue(request))
    .exchange()
    .expectStatus().isOk
    .expectBody()
    .jsonPath("inOutStatus").isEqualTo("OUT")
    .jsonPath("status").isEqualTo("ACTIVE OUT")
    .jsonPath("lastMovementTypeCode").isEqualTo("CRT")
    .jsonPath("lastMovementReasonCode").isEqualTo("19")
    .jsonPath("assignedLivingUnit.agencyId").isEqualTo(expectedAgency)

  return movementTime
}

fun TestDataContext.transferOutToTemporaryAbsence(
  offenderNo: String,
  toLocation: String,
  shouldReleaseBed: Boolean = false,
  tapIndividualScheduleEventId: Long? = null,
): LocalDateTime {
  val movementTime = LocalDateTime.now().minusHours(1)
  val request = RequestToTransferOutToTemporaryAbsence.builder()
    .toCity(toLocation)
    .movementTime(movementTime)
    .transferReasonCode("C3")
    .commentText("day release")
    .shouldReleaseBed(shouldReleaseBed)
    .scheduleEventId(tapIndividualScheduleEventId)
    .build()
  webTestClient.put()
    .uri("/api/offenders/{nomsId}/temporary-absence-out", offenderNo)
    .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER_ALPHA")))
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .body(BodyInserters.fromValue(request))
    .exchange()
    .expectStatus().isOk
    .expectBody()
    .jsonPath("inOutStatus").isEqualTo("OUT")
    .jsonPath("status").isEqualTo("ACTIVE OUT")
    .jsonPath("lastMovementTypeCode").isEqualTo("TAP")
    .jsonPath("lastMovementReasonCode").isEqualTo("C3")
    .jsonPath("assignedLivingUnit.agencyId").isEqualTo("LEI")

  return movementTime
}

private fun TestDataContext.setAuthorisation(roles: List<String>): Consumer<HttpHeaders> = Consumer { httpHeaders: HttpHeaders ->
  httpHeaders.add(
    "Authorization",
    "Bearer " + this.validToken(roles),
  )
}

fun TestDataContext.validToken(roles: List<String>): String = this.jwtAuthenticationHelper.createJwtAccessToken(
  username = "ITAG_USER",
  scope = listOf("read", "write"),
  roles = roles,
)

fun TestDataContext.createScheduledTemporaryAbsence(
  bookingId: Long,
  toAddressId: Long,
  startTime: LocalDateTime,
): OffenderIndividualSchedule = this.dataLoader.offenderBookingRepository.findByIdOrNull(bookingId)!!.let {
  this.dataLoader.scheduleRepository.save(
    OffenderIndividualSchedule.builder()
      .eventDate(startTime.toLocalDate())
      .startTime(startTime)
      .eventClass(OffenderIndividualSchedule.EventClass.EXT_MOV)
      .eventType("TAP")
      .eventSubType("ET")
      .eventStatus(this.dataLoader.eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED).orElseThrow())
      .escortAgencyType(
        this.dataLoader.escortAgencyTypeRepository.findByIdOrNull(
          EscortAgencyType.pk("L"),
        ),
      )
      .fromLocation(it.location)
      .toAddressOwnerClass("CORP")
      .toAddressId(toAddressId)
      .movementDirection(MovementDirection.OUT)
      .offenderBooking(it)
      .build(),
  )
}

fun TestDataContext.createCourtHearing(bookingId: Long): Long {
  val courtCase = this.dataLoader.offenderCourtCaseRepository.findAllByOffenderBooking_BookingId(bookingId).first()

  return webTestClient.post()
    .uri("/api/bookings/{bookingId}/court-cases/{courtCaseId}/prison-to-court-hearings", bookingId, courtCase.id)
    .headers(setAuthorisation(listOf("ROLE_COURT_HEARING_MAINTAINER")))
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .body(
      BodyInserters.fromValue(
        """
          {
            "fromPrisonLocation":"LEI",
            "toCourtLocation":"COURT1",
            "courtHearingDateTime": "${LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
            "comments":"court appearance"
            
          }
        """.trimIndent(),
      ),
    )
    .exchange()
    .expectStatus().isCreated
    .returnResult<CourtHearing>().responseBody.blockFirst()?.id!!
}

fun TestDataContext.getMovements(bookingId: Long): List<ExternalMovement> = this.dataLoader.externalMovementRepository.findAllByOffenderBooking_BookingId(bookingId)
fun TestDataContext.getBedAssignments(bookingId: Long): List<BedAssignmentHistory> = this.dataLoader.bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId)

fun TestDataContext.getCaseNotes(offenderNo: String): List<CaseNote> = webTestClient.get()
  .uri("/api/offenders/{offenderNo}/case-notes/v2?size=999", offenderNo)
  .headers(
    setAuthorisation(
      listOf("ROLE_VIEW_CASE_NOTES"),
    ),
  )
  .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
  .accept(MediaType.APPLICATION_JSON)
  .exchange()
  .expectStatus().isOk
  .returnResult<RestResponsePage<CaseNote>>().responseBody.blockFirst()!!.content

fun TestDataContext.getCourtHearings(bookingId: Long): List<CourtEvent> = this.dataLoader.courtEventRepository.findByOffenderBooking_BookingIdOrderByIdAsc(bookingId)

fun TestDataContext.getScheduledMovements(bookingId: Long): List<OffenderIndividualSchedule> = this.dataLoader.scheduleRepository.findByOffenderBooking_BookingIdOrderByIdAsc(bookingId)

fun TestDataContext.getSentenceAdjustments(bookingId: Long): List<SentenceAdjustment> = this.dataLoader.offenderSentenceAdjustmentRepository.findAllByOffenderBooking_BookingId(bookingId)

fun TestDataContext.getKeyDateAdjustments(bookingId: Long): List<KeyDateAdjustment> = this.dataLoader.offenderKeyDateAdjustmentRepository.findAllByOffenderBooking_BookingId(bookingId)

fun TestDataContext.getOffenderPayStatus(bookingId: Long): List<OffenderPayStatus> = this.dataLoader.offenderPayStatusRepository.findAllByBookingId(bookingId)

fun TestDataContext.getOffenderNoPayPeriods(bookingId: Long): List<OffenderNoPayPeriod> = this.dataLoader.offenderNoPayPeriodRepository.findAllByBookingId(bookingId)

fun TestDataContext.getOffenderProgramProfiles(bookingId: Long, programStatus: String): List<OffenderProgramProfile> = this.dataLoader.offenderProgramProfileRepository.findByOffenderBooking_BookingIdAndProgramStatus(bookingId, programStatus)

fun TestDataContext.getOffenderBooking(bookingId: Long): OffenderBooking? = this.dataLoader.offenderBookingRepository.findByBookingId(bookingId).orElse(null)

fun TestDataContext.getOffenderBooking(offenderNo: String, active: Boolean = true): OffenderBooking? = this.dataLoader.offenderBookingRepository.findByOffenderNomsIdAndActive(offenderNo, active).orElse(null)
