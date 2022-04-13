package uk.gov.justice.hmpps.prison.api.resource.impl

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.CaseNote
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection.IN
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection.OUT
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.util.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.OffenderBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * KOTLIN
 */

class OffendersResourceTransferImpTest : ResourceTest() {
  @Autowired
  private lateinit var externalMovementRepository: ExternalMovementRepository

  @Autowired
  private lateinit var bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository

  @Nested
  @DisplayName("PUT /{offenderNo}/transfer-in")
  inner class TransferIn {
    @Nested
    @DisplayName("Successful transfer in")
    inner class Success {
      private lateinit var offenderNo: String
      private var bookingId: Long = 0
      private val bookingInTime = LocalDateTime.now().minusDays(1)

      @BeforeEach
      internal fun setUp() {
        OffenderBuilder().withBooking(
          OffenderBookingBuilder(
            prisonId = "LEI",
            bookingInTime = bookingInTime
          ).withIEPLevel("ENH")
        ).save(webTestClient = webTestClient, jwtAuthenticationHelper = jwtAuthenticationHelper).also {
          offenderNo = it.offenderNo
          bookingId = it.bookingId
        }

        transferOut(offenderNo, "MDI")
      }

      @Test
      internal fun `can transfer a prisoner a prison and specify a cell`() {
        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "transferReasonCode":"NOTR",
            "commentText":"admitted",
            "cellLocation":"MDI-1-3-022",
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("inOutStatus").isEqualTo("IN")
          .jsonPath("status").isEqualTo("ACTIVE IN")
          .jsonPath("lastMovementTypeCode").isEqualTo("ADM")
          .jsonPath("lastMovementReasonCode").isEqualTo("INT")
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("MDI")
          .jsonPath("assignedLivingUnit.description").isEqualTo("1-3-022")
      }

      @Test
      internal fun `by default transfer will be into reception`() {
        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("MDI")
          .jsonPath("assignedLivingUnit.description").isEqualTo("RECP")
      }

      @Test
      internal fun `will create a new movement and deactivate the transfer out`() {
        assertThat(getMovements(bookingId))
          .extracting(
            ExternalMovement::getMovementSequence,
            ExternalMovement::getMovementDirection,
            ExternalMovement::isActive
          )
          .containsExactly(
            tuple(1L, IN, false),
            tuple(2L, OUT, true),
          )

        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk

        assertThat(getMovements(bookingId))
          .extracting(
            ExternalMovement::getMovementSequence,
            ExternalMovement::getMovementDirection,
            ExternalMovement::isActive
          )
          .containsExactly(
            tuple(1L, IN, false),
            tuple(2L, OUT, false),
            tuple(3L, IN, true),
          )
      }

      @Test
      internal fun `will create a new bed assignment history record`() {
        assertThat(getBedAssignments(bookingId))
          .extracting(
            BedAssignmentHistory::getAssignmentReason,
            BedAssignmentHistory::getAssignmentDate,
            BedAssignmentHistory::getAssignmentEndDate
          )
          .containsExactly(
            tuple("ADM", bookingInTime.toLocalDate(), LocalDate.now()),
          )

        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk

        assertThat(getBedAssignments(bookingId))
          .extracting(
            BedAssignmentHistory::getAssignmentReason,
            BedAssignmentHistory::getAssignmentDate,
            BedAssignmentHistory::getAssignmentEndDate
          )
          .containsExactly(
            tuple("ADM", bookingInTime.toLocalDate(), LocalDate.now()),
            tuple("ADM", LocalDate.now(), null),
          )
      }

      @Test
      internal fun `will reset IEP level back to default for prison`() {
        assertThat(getCurrentIEP(offenderNo))
          .extracting(PrivilegeSummary::getIepLevel)
          .isEqualTo("Enhanced")

        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk

        assertThat(getCurrentIEP(offenderNo))
          .extracting(PrivilegeSummary::getIepLevel)
          .isEqualTo("Entry")
      }

      @Test
      internal fun `will create a transfer in case note`() {
        assertThat(getCaseNotes(bookingId))
          .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
          .contains(tuple("TRANSFER", "FROMTOL", "Offender admitted to LEEDS for reason: Unconvicted Remand from OUTSIDE."))

        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk

        assertThat(getCaseNotes(bookingId))
          .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
          .contains(tuple("TRANSFER", "FROMTOL", "Offender admitted to MOORLAND for reason: Transfer In from Other Establishment from LEEDS."))
      }
    }

    @Nested
    @DisplayName("Failed transfer in")
    inner class Failed {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        offenderNo =
          OffenderBuilder().withBooking(
            OffenderBookingBuilder(
              prisonId = "LEI",
              bookingInTime = LocalDateTime.now().minusDays(1)
            )
          ).save(webTestClient = webTestClient, jwtAuthenticationHelper = jwtAuthenticationHelper).offenderNo
      }

      @Test
      internal fun `cannot transfer a prisoner in to a full cell`() {
        transferOut(offenderNo, "MDI")

        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "transferReasonCode":"NOTR",
            "commentText":"admitted",
            "cellLocation":"MDI-FULL",
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isEqualTo(409)
          .expectBody()
          .jsonPath("userMessage").isEqualTo("The cell MDI-FULL does not have any available capacity")
      }

      @Test
      internal fun `cannot transfer in when not already in transit`() {
        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "transferReasonCode":"NOTR",
            "commentText":"admitted",
            "cellLocation":"MDI-RECP",
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isEqualTo(400)
          .expectBody()
          .jsonPath("userMessage").isEqualTo("Prisoner is not currently being transferred")
      }

      @Test
      internal fun `cannot transfer with a time in the future`() {
        transferOut(offenderNo, "MDI")

        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "transferReasonCode":"NOTR",
            "commentText":"admitted",
            "receiveTime": "${
              LocalDateTime.now().plusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isEqualTo(400)
          .expectBody()
          .jsonPath("userMessage").isEqualTo("Transfer cannot be done in the future")
      }

      @Test
      internal fun `cannot transfer with a time before transfer out time`() {
        transferOut(offenderNo, "MDI")

        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "transferReasonCode":"NOTR",
            "commentText":"admitted",
            "receiveTime": "${
              LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isEqualTo(400)
          .expectBody()
          .jsonPath("userMessage").isEqualTo("Movement cannot be before the previous active movement")
      }
    }
  }

  @Nested
  @DisplayName("PUT /{offenderNo}/court-transfer-in")
  inner class CourtTransferIn

  fun transferOut(offenderNo: String, toLocation: String) {
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
          """.trimIndent()
        )
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

  private fun getMovements(bookingId: Long) = externalMovementRepository.findAllByOffenderBooking_BookingId(bookingId)
  private fun getBedAssignments(bookingId: Long) =
    bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId)

  private fun getCurrentIEP(offenderNo: String) = webTestClient.get()
    .uri("/api/offenders/{offenderNo}/iepSummary", offenderNo)
    .headers(
      setAuthorisation(
        listOf("ROLE_SYSTEM_USER")
      )
    )
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .returnResult<PrivilegeSummary>().responseBody.blockFirst()!!

  private fun getCaseNotes(bookingId: Long) = webTestClient.get()
    .uri("/api/bookings/{bookingId}/caseNotes?size=999", bookingId)
    .headers(
      setAuthorisation(
        listOf("ROLE_SYSTEM_USER")
      )
    )
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .returnResult<RestResponsePage<CaseNote>>().responseBody.blockFirst()!!.content
}

class RestResponsePage<T> @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("content") content: List<T>,
  @JsonProperty("number") number: Int,
  @JsonProperty("size") size: Int,
  @JsonProperty("totalElements") totalElements: Long,
  @Suppress("UNUSED_PARAMETER") @JsonProperty(
    "pageable"
  ) pageable: JsonNode
) : PageImpl<T>(content, PageRequest.of(number, size), totalElements)
