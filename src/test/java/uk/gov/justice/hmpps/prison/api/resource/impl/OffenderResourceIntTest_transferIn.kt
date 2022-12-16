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
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.StatusAssertions
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.CaseNote
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary
import uk.gov.justice.hmpps.prison.api.model.VisitBalances
import uk.gov.justice.hmpps.prison.exception.CustomErrorCodes
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection.IN
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection.OUT
import uk.gov.justice.hmpps.prison.repository.jpa.model.Team
import uk.gov.justice.hmpps.prison.service.DataLoaderTransaction
import uk.gov.justice.hmpps.prison.service.receiveandtransfer.WorkflowTaskService
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderCourtCaseBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderTeamAssignmentBuilder
import uk.gov.justice.hmpps.prison.util.builders.TeamBuilder
import uk.gov.justice.hmpps.prison.util.builders.createCourtHearing
import uk.gov.justice.hmpps.prison.util.builders.createScheduledTemporaryAbsence
import uk.gov.justice.hmpps.prison.util.builders.getBedAssignments
import uk.gov.justice.hmpps.prison.util.builders.getCaseNotes
import uk.gov.justice.hmpps.prison.util.builders.getCourtHearings
import uk.gov.justice.hmpps.prison.util.builders.getCurrentIEP
import uk.gov.justice.hmpps.prison.util.builders.getMovements
import uk.gov.justice.hmpps.prison.util.builders.getScheduledMovements
import uk.gov.justice.hmpps.prison.util.builders.getVOBalanceDetails
import uk.gov.justice.hmpps.prison.util.builders.release
import uk.gov.justice.hmpps.prison.util.builders.transferOut
import uk.gov.justice.hmpps.prison.util.builders.transferOutToCourt
import uk.gov.justice.hmpps.prison.util.builders.transferOutToTemporaryAbsence
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * KOTLIN
 */
@WithMockUser
class OffendersResourceTransferImpTest : ResourceTest() {
  @Autowired
  private lateinit var dataLoaderTransaction: DataLoaderTransaction

  @MockBean
  private lateinit var workflowTaskService: WorkflowTaskService

  private val team: Team by lazy {
    dataLoaderTransaction.load(TeamBuilder(), testDataContext)
  }

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
          ).withIEPLevel("ENH").withInitialVoBalances(2, 8)
        ).save(testDataContext).also {
          offenderNo = it.offenderNo
          bookingId = it.bookingId
        }

        testDataContext.transferOut(offenderNo, "MDI")
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
        assertThat(testDataContext.getMovements(bookingId))
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

        assertThat(testDataContext.getMovements(bookingId))
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
        assertThat(testDataContext.getBedAssignments(bookingId))
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

        assertThat(testDataContext.getBedAssignments(bookingId))
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
      internal fun `will create a transfer in case note`() {
        assertThat(testDataContext.getCaseNotes(bookingId))
          .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
          .contains(
            tuple(
              "TRANSFER",
              "FROMTOL",
              "Offender admitted to LEEDS for reason: Unconvicted Remand from OUTSIDE."
            )
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

        assertThat(testDataContext.getCaseNotes(bookingId))
          .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
          .contains(
            tuple(
              "TRANSFER",
              "FROMTOL",
              "Offender admitted to MOORLAND for reason: Transfer In from Other Establishment from LEEDS."
            )
          )
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
          ).save(testDataContext).offenderNo
      }

      @Test
      internal fun `cannot transfer a prisoner in to a full cell`() {
        testDataContext.transferOut(offenderNo, "MDI")

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
          .jsonPath("errorCode").isEqualTo(CustomErrorCodes.NO_CELL_CAPACITY)
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
        testDataContext.transferOut(offenderNo, "MDI")

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
        testDataContext.transferOut(offenderNo, "MDI")

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
  inner class CourtTransferIn {
    @Nested
    @DisplayName("Successful transfer in from court")
    inner class Success {
      private lateinit var offenderNo: String
      private var bookingId: Long = 0
      private val bookingInTime = LocalDateTime.now().minusDays(1)

      @BeforeEach
      internal fun setUp() {
        dataLoaderTransaction.load(
          OffenderBuilder().withBooking(
            OffenderBookingBuilder(
              prisonId = "LEI",
              bookingInTime = bookingInTime,
              cellLocation = "LEI-RECP"
            )
              .withIEPLevel("ENH")
              .withInitialVoBalances(2, 8)
              .withCourtCases(OffenderCourtCaseBuilder())
          ),
          testDataContext
        )
          .also {
            offenderNo = it.offenderNo
            bookingId = it.bookingId
          }
      }

      @Nested
      @DisplayName("When bed is released")
      inner class BedReleased {
        private lateinit var transferOutDateTime: LocalDateTime

        @BeforeEach
        internal fun setUp() {
          transferOutDateTime =
            testDataContext.transferOutToCourt(offenderNo, toLocation = "COURT1", shouldReleaseBed = true)
        }

        @Nested
        @DisplayName("Returning back to the same prison")
        inner class SamePrison {

          @Test
          internal fun `will set the prisoner as active in`() {
            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "movementReasonCode":"CRT",
                    "dateTime": "${LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("status").isEqualTo("ACTIVE IN")
          }

          @Test
          internal fun `can override movement reason`() {
            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "movementReasonCode":"CRT",
                    "dateTime": "${LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk
              .expectBody()
              .jsonPath("lastMovementTypeCode").isEqualTo("CRT")
              .jsonPath("lastMovementReasonCode").isEqualTo("CRT")
          }

          @Test
          internal fun `cell remains unchanged from when the prisoner was transferred to court`() {
            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "dateTime": "${LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk
              .expectBody()
              .jsonPath("assignedLivingUnit.agencyId").isEqualTo("LEI")
              .jsonPath("assignedLivingUnit.description").isEqualTo("COURT") // as set when bed was released
              .jsonPath("lastMovementReasonCode").isEqualTo("19")
          }

          @Test
          internal fun `will create a new movement and deactivate the transfer to court out`() {
            assertThat(testDataContext.getMovements(bookingId))
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
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "dateTime": "${LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
                    "movementReasonCode": "19"
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk

            assertThat(testDataContext.getMovements(bookingId))
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
          internal fun `will not record any bed history changes`() {
            val receiveDateTime = LocalDateTime.now().minusMinutes(2)
            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), transferOutDateTime.toLocalDate()),
                tuple("19", transferOutDateTime.toLocalDate(), null),
              )

            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "dateTime": "${receiveDateTime.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk

            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), transferOutDateTime.toLocalDate()),
                tuple("19", transferOutDateTime.toLocalDate(), null),
              )
          }
        }

        @Nested
        @DisplayName("Returning to a different prison")
        inner class DifferentPrison {
          @Test
          internal fun `returning to a different prison is allowed`() {
            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"MDI",
                    "commentText":"admitted"
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
              .jsonPath("lastMovementReasonCode").isEqualTo("TRNCRT")
          }

          @Test
          internal fun `by default transfer will be into reception`() {
            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"MDI",
                    "commentText":"admitted"
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
            assertThat(testDataContext.getMovements(bookingId))
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
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"MDI",
                    "commentText":"admitted"
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk

            assertThat(testDataContext.getMovements(bookingId))
              .extracting(
                ExternalMovement::getMovementSequence,
                ExternalMovement::getMovementDirection,
                ExternalMovement::isActive
              )
              .containsExactly(
                tuple(1L, IN, false),
                tuple(2L, OUT, false), // updated false
                tuple(3L, IN, true), // new created transfer in event
              )
          }

          @Test
          internal fun `will create a new bed assignment history record with no reason code`() {
            val receiveDateTime = LocalDateTime.now().minusMinutes(2)
            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), LocalDate.now()),
                tuple("19", transferOutDateTime.toLocalDate(), null),
              )

            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"MDI",
                    "commentText":"admitted",
                    "dateTime": "${receiveDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"          
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk

            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), LocalDate.now()), // admission to original prison
                tuple(
                  "19",
                  transferOutDateTime.toLocalDate(),
                  null
                ), // trigger end_prev_bed_assg_hty will add an end date to the previous movement, but can't be tested
                tuple(null, receiveDateTime.toLocalDate(), null), // as per nomis
              )
          }

          @Test
          internal fun `will create a transfer via court case note`() {
            assertThat(testDataContext.getCaseNotes(bookingId))
              .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
              .contains(
                tuple(
                  "TRANSFER",
                  "FROMTOL",
                  "Offender admitted to LEEDS for reason: Unconvicted Remand from OUTSIDE."
                )
              )

            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"MDI",
                    "commentText":"admitted"
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk

            assertThat(testDataContext.getCaseNotes(bookingId))
              .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
              .contains(
                tuple(
                  "TRANSFER",
                  "FROMTOL",
                  "Offender admitted to MOORLAND for reason: Transfer Via Court from LEEDS."
                )
              )
          }
        }
      }

      @Nested
      @DisplayName("When bed is not released")
      inner class BedNotReleased {
        private lateinit var transferOutDateTime: LocalDateTime

        @BeforeEach
        internal fun setUp() {
          transferOutDateTime =
            testDataContext.transferOutToCourt(offenderNo, toLocation = "COURT1", shouldReleaseBed = false)
        }

        @Nested
        @DisplayName("Returning back to the same prison")
        inner class SamePrison {

          @Test
          internal fun `cell remains on changed from when the prisoner was transferred to court`() {
            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
          {
            "agencyId":"LEI",
            "commentText":"admitted"
          }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk
              .expectBody()
              .jsonPath("assignedLivingUnit.agencyId").isEqualTo("LEI")
              .jsonPath("assignedLivingUnit.description").isEqualTo("RECP")
          }

          @Test
          internal fun `will not record any bed history changes`() {
            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), null),
              )

            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "dateTime": "${LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk

            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), null),
              )
          }
        }

        @Nested
        @DisplayName("Returning to a different prison")
        inner class DifferentPrison {
          @Test
          internal fun `will set the prisoner as active in`() {
            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"MDI",
                    "commentText":"admitted",
                    "movementReasonCode":"CRT",
                    "dateTime": "${LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("status").isEqualTo("ACTIVE IN")
          }

          @Test
          internal fun `will create a new bed assignment history record with no reason code`() {
            val receiveDateTime = LocalDateTime.now().minusMinutes(2)
            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), null),
              )

            webTestClient.put()
              .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
              .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .accept(MediaType.APPLICATION_JSON)
              .body(
                BodyInserters.fromValue(
                  """
                  {
                    "agencyId":"MDI",
                    "commentText":"admitted",
                    "dateTime": "${receiveDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"          
                  }
                  """.trimIndent()
                )
              )
              .exchange()
              .expectStatus().isOk

            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple(
                  "ADM",
                  bookingInTime.toLocalDate(),
                  null
                ), // trigger end_prev_bed_assg_hty will add an end date to the previous movement, but can't be tested
                tuple(null, receiveDateTime.toLocalDate(), null),
              )
          }
        }
      }

      @Nested
      @DisplayName("With a scheduled court appearance")
      open inner class WithCourtAppearance {
        private var courtHearingEventId: Long = 0

        @BeforeEach
        internal fun setUp() {
          courtHearingEventId = testDataContext.createCourtHearing(bookingId)
          assertThat(testDataContext.getCourtHearings(bookingId)).extracting("eventStatus.code").containsExactly("SCH")
          testDataContext.transferOutToCourt(offenderNo = offenderNo, toLocation = "COURT1", false, courtHearingEventId)
        }

        @Test
        internal fun `will complete scheduled appearance event`() {
          assertThat(testDataContext.getCourtHearings(bookingId)).extracting("eventStatus.code")
            .containsExactly("COMP", "SCH")
          webTestClient.put()
            .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
            .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .body(
              BodyInserters.fromValue(
                """
                {
                  "agencyId":"LEI",
                  "commentText":"admitted"
                }
                """.trimIndent()
              )
            )
            .exchange()
            .expectStatus().isOk
          val courtHearingEVents = testDataContext.getCourtHearings(bookingId)
          assertThat(courtHearingEVents).extracting("eventStatus.code").containsExactly("COMP", "COMP")
          assertThat(testDataContext.getMovements(bookingId).last().eventId).isEqualTo(courtHearingEVents.last().id)
        }
      }
    }

    @Nested
    @DisplayName("With a team assignment")
    inner class WithTeamAssignment {
      private lateinit var offenderNo: String
      private var bookingId: Long = 0
      private val bookingInTime = LocalDateTime.now().minusDays(1)
      private lateinit var transferOutDateTime: LocalDateTime

      @BeforeEach
      internal fun setUp() {
        dataLoaderTransaction.load(
          OffenderBuilder().withBooking(
            OffenderBookingBuilder(
              prisonId = "LEI",
              bookingInTime = bookingInTime,
              cellLocation = "LEI-RECP"
            )
              .withTeamAssignment(OffenderTeamAssignmentBuilder(team))
          ),
          testDataContext
        )
          .also {
            offenderNo = it.offenderNo
            bookingId = it.bookingId
          }

        transferOutDateTime =
          testDataContext.transferOutToCourt(offenderNo, toLocation = "COURT1", shouldReleaseBed = false)
      }

      @Nested
      @DisplayName("Returning to a different prison")
      inner class DifferentPrison {
        @Test
        internal fun `will notify team of the automatic transfer`() {
          val receiveDateTime = LocalDateTime.now()
          webTestClient.put()
            .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
            .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .body(
              BodyInserters.fromValue(
                """
                  {
                    "agencyId":"MDI",
                    "dateTime":"${receiveDateTime.format(DateTimeFormatter.ISO_DATE_TIME)}"
                  }
                """.trimIndent()
              )
            )
            .exchange()
            .expectStatus().isOk

          // we can't test store procedure is called since we are running against H2, so next best thing is
          // to assert service is called with the correct parameters that are passed to stored procedure
          verify(workflowTaskService).createTaskAutomaticTransfer(
            check {
              assertThat(it.bookingId).isEqualTo(bookingId)
            },
            check {
              assertThat(it.fromAgency.id).isEqualTo("LEI")
              assertThat(it.toAgency.id).isEqualTo("MDI")
              assertThat(it.movementReason.code).isEqualTo("TRNCRT")
              assertThat(it.movementDate).isEqualTo(receiveDateTime.toLocalDate())
              assertThat(it.movementTime).isEqualTo(receiveDateTime)
            },
            check {
              assertThat(it.id).isEqualTo(team.id)
            }
          )
        }
      }

      @Nested
      @DisplayName("Returning to the same prison")
      inner class SamePrison {
        @Test
        internal fun `will not notify team of a non transfer`() {
          val receiveDateTime = LocalDateTime.now()
          webTestClient.put()
            .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
            .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .body(
              BodyInserters.fromValue(
                """
                  {
                    "agencyId":"LEI",
                    "dateTime":"${receiveDateTime.format(DateTimeFormatter.ISO_DATE_TIME)}"
                  }
                """.trimIndent()
              )
            )
            .exchange()
            .expectStatus().isOk

          verifyNoInteractions(workflowTaskService)
        }
      }
    }

    @Nested
    @DisplayName("Failed to transfer in")
    inner class Failed {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        OffenderBuilder().withBooking(
          OffenderBookingBuilder(
            prisonId = "LEI",
            bookingInTime = LocalDateTime.now().minusDays(10),
            cellLocation = "LEI-RECP"
          ).withIEPLevel("ENH").withInitialVoBalances(2, 8)
        ).save(testDataContext).also {
          offenderNo = it.offenderNo
        }
      }

      @Nested
      @DisplayName("Returning back to the same prison")
      inner class SamePrison {
        @Test
        internal fun `cannot transfer in when not already in court`() {
          webTestClient.put()
            .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
            .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .body(
              BodyInserters.fromValue(
                """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "movementReasonCode":"CRT",
                    "dateTime": "${LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                  }
                """.trimIndent()
              )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("userMessage").isEqualTo("Prisoner is not currently out")
        }

        @Test
        internal fun `cannot transfer in when not previously transferred to court`() {
          testDataContext.release(offenderNo)
          webTestClient.put()
            .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
            .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .body(
              BodyInserters.fromValue(
                """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "movementReasonCode":"CRT",
                    "dateTime": "${LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                  }
                """.trimIndent()
              )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("userMessage").isEqualTo("Latest movement not a court transfer")
        }

        @Test
        internal fun `cannot transfer with a time in the future`() {
          testDataContext.transferOutToCourt(offenderNo, toLocation = "COURT1", shouldReleaseBed = false)

          webTestClient.put()
            .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
            .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .body(
              BodyInserters.fromValue(
                """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "movementReasonCode":"CRT",
                    "dateTime": "${LocalDateTime.now().plusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
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
          val transferOutDateTime =
            testDataContext.transferOutToCourt(offenderNo, toLocation = "COURT1", shouldReleaseBed = false)

          webTestClient.put()
            .uri("/api/offenders/{nomsId}/court-transfer-in", offenderNo)
            .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .body(
              BodyInserters.fromValue(
                """
                  {
                    "agencyId":"LEI",
                    "commentText":"admitted",
                    "movementReasonCode":"CRT",
                    "dateTime": "${transferOutDateTime.minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
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
  }

  @Nested
  @DisplayName("PUT /{offenderNo}/temporary-absence-arrival")
  inner class TAPTransferIn {
    @Nested
    @DisplayName("Successful transfer in from temporary absence")
    inner class Success {
      private lateinit var offenderNo: String
      private var bookingId: Long = 0
      private val bookingInTime = LocalDateTime.now().minusDays(1)

      @BeforeEach
      internal fun setUp() {
        dataLoaderTransaction.load(
          OffenderBuilder().withBooking(
            OffenderBookingBuilder(
              prisonId = "LEI",
              bookingInTime = bookingInTime,
              cellLocation = "LEI-RECP"
            )
              .withIEPLevel("ENH")
              .withInitialVoBalances(2, 8)
          ),
          testDataContext
        )
          .also {
            offenderNo = it.offenderNo
            bookingId = it.bookingId
          }
      }

      @Nested
      @DisplayName("When bed is released")
      inner class BedReleased {
        private lateinit var transferOutDateTime: LocalDateTime
        private val toCityId = "18248"

        @BeforeEach
        internal fun setUp() {
          transferOutDateTime =
            testDataContext.transferOutToTemporaryAbsence(
              offenderNo,
              toLocation = toCityId,
              shouldReleaseBed = true
            )

          getOffender(offenderNo)
            .isOk
            .expectBody()
            .jsonPath("inOutStatus").isEqualTo("OUT")
            .jsonPath("status").isEqualTo("ACTIVE OUT")
            .jsonPath("agencyId").isEqualTo("LEI")
            .jsonPath("statusReason").isEqualTo("TAP-C3")
        }

        @Nested
        @DisplayName("Returning back to the same prison")
        inner class SamePrison {

          @Test
          internal fun `will set the prisoner as active in`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI"))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("status").isEqualTo("ACTIVE IN")
              .jsonPath("agencyId").isEqualTo("LEI")

            getOffender(offenderNo)
              .isOk
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("status").isEqualTo("ACTIVE IN")
              .jsonPath("agencyId").isEqualTo("LEI")
          }

          @Test
          internal fun `default movement reason is taken from out movement`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI", movementReasonCode = null))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("agencyId").isEqualTo("LEI")

            getOffender(offenderNo)
              .isOk
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("agencyId").isEqualTo("LEI")
              .jsonPath("lastMovementTypeCode").isEqualTo("TAP")
              .jsonPath("lastMovementReasonCode").isEqualTo("C3")
          }

          @Test
          internal fun `can override movement reason`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI", movementReasonCode = "C6"))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("agencyId").isEqualTo("LEI")

            assertThat(dataLoaderTransaction.get { lastMovement(bookingId).movementReason?.code }).isEqualTo("C6")
          }

          @Test
          internal fun `when override movement reason booking status field is updated to the new reason`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI", movementReasonCode = "C6"))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("agencyId").isEqualTo("LEI")

            getOffender(offenderNo)
              .isOk
              .expectBody()
              .jsonPath("lastMovementTypeCode").isEqualTo("TAP")
              .jsonPath("lastMovementReasonCode").isEqualTo("C6")
          }

          @Test
          internal fun `from city should be taken from the city transferred to`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI", movementReasonCode = null))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("agencyId").isEqualTo("LEI")

            assertThat(dataLoaderTransaction.get { lastMovement(bookingId).fromCity?.code }).isEqualTo(toCityId)
          }

          @Test
          internal fun `cell remains unchanged from when the prisoner was released on TAP`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI"))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("agencyId").isEqualTo("LEI")

            getOffender(offenderNo)
              .isOk
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("agencyId").isEqualTo("LEI")
              .jsonPath("assignedLivingUnit.agencyId").isEqualTo("LEI")
              .jsonPath("assignedLivingUnit.description").isEqualTo("TAP") // as set when bed was released
          }

          @Test
          internal fun `will create a new movement and deactivate the release on TAP out`() {
            assertThat(testDataContext.getMovements(bookingId))
              .extracting(
                ExternalMovement::getMovementSequence,
                ExternalMovement::getMovementDirection,
                ExternalMovement::isActive
              )
              .containsExactly(
                tuple(1L, IN, false),
                tuple(2L, OUT, true),
              )

            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI"))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("agencyId").isEqualTo("LEI")

            assertThat(testDataContext.getMovements(bookingId))
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
          internal fun `will not record any bed history changes`() {
            val receiveDateTime = LocalDateTime.now().minusMinutes(2)
            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), transferOutDateTime.toLocalDate()),
                tuple("C3", transferOutDateTime.toLocalDate(), null),
              )

            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI", dateTime = receiveDateTime))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("agencyId").isEqualTo("LEI")

            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), transferOutDateTime.toLocalDate()),
                tuple("C3", transferOutDateTime.toLocalDate(), null),
              )
          }
        }

        @Nested
        @DisplayName("Returning to a different prison")
        inner class DifferentPrison {
          @Test
          internal fun `returning to a different prison is allowed`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("status").isEqualTo("ACTIVE IN")
              .jsonPath("agencyId").isEqualTo("MDI")
              .jsonPath("lastMovementTypeCode").isEqualTo("ADM")
              .jsonPath("lastMovementReasonCode").isEqualTo("TRNTAP")

            getOffender(offenderNo)
              .isOk
              .expectBody()
              .jsonPath("lastMovementTypeCode").isEqualTo("ADM")
              .jsonPath("lastMovementReasonCode").isEqualTo("TRNTAP")
          }

          @Test
          internal fun `by default transfer will be into reception`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))
              .expectBody()
              .jsonPath("assignedLivingUnit.agencyId").isEqualTo("MDI")
              .jsonPath("assignedLivingUnit.description").isEqualTo("RECP")
          }

          @Test
          internal fun `will create a new movement and deactivate the transfer out`() {
            assertThat(testDataContext.getMovements(bookingId))
              .extracting(
                ExternalMovement::getMovementSequence,
                ExternalMovement::getMovementDirection,
                ExternalMovement::isActive
              )
              .containsExactly(
                tuple(1L, IN, false),
                tuple(2L, OUT, true),
              )

            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))

            assertThat(testDataContext.getMovements(bookingId))
              .extracting(
                ExternalMovement::getMovementSequence,
                ExternalMovement::getMovementDirection,
                ExternalMovement::isActive
              )
              .containsExactly(
                tuple(1L, IN, false),
                tuple(2L, OUT, false), // updated false
                tuple(3L, IN, true), // new created transfer in event
              )
          }

          @Test
          internal fun `from city will not be set since it is a transfer`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))

            assertThat(dataLoaderTransaction.get { lastMovement(bookingId).fromCity }).isNull()
          }

          @Test
          internal fun `will create a new bed assignment history record with no reason code`() {
            val receiveDateTime = LocalDateTime.now().minusMinutes(2)
            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), LocalDate.now()),
                tuple("C3", transferOutDateTime.toLocalDate(), null),
              )

            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI", dateTime = receiveDateTime))

            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), LocalDate.now()), // admission to original prison
                tuple(
                  "C3",
                  transferOutDateTime.toLocalDate(),
                  null
                ), // trigger end_prev_bed_assg_hty will add an end date to the previous movement, but can't be tested
                tuple(null, receiveDateTime.toLocalDate(), null), // as per nomis
              )
          }

          @Test
          internal fun `will create a transfer via TAP note`() {
            assertThat(testDataContext.getCaseNotes(bookingId))
              .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
              .contains(
                tuple(
                  "TRANSFER",
                  "FROMTOL",
                  "Offender admitted to LEEDS for reason: Unconvicted Remand from OUTSIDE."
                )
              )

            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))

            assertThat(testDataContext.getCaseNotes(bookingId))
              .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
              .contains(
                tuple(
                  "TRANSFER",
                  "FROMTOL",
                  "Offender admitted to MOORLAND for reason: Transfer Via Temporary Release from LEEDS."
                )
              )
          }
        }
      }

      @Nested
      @DisplayName("When bed is not released")
      inner class BedNotReleased {
        private lateinit var transferOutDateTime: LocalDateTime

        @BeforeEach
        internal fun setUp() {
          transferOutDateTime =
            testDataContext.transferOutToTemporaryAbsence(offenderNo, toLocation = "18248", shouldReleaseBed = false)

          getOffender(offenderNo)
            .isOk
            .expectBody()
            .jsonPath("inOutStatus").isEqualTo("OUT")
            .jsonPath("status").isEqualTo("ACTIVE OUT")
            .jsonPath("agencyId").isEqualTo("LEI")
            .jsonPath("statusReason").isEqualTo("TAP-C3")
        }

        @Nested
        @DisplayName("Returning back to the same prison")
        inner class SamePrison {

          @Test
          internal fun `cell remains unchanged from when the prisoner was released to TAP`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI"))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("status").isEqualTo("ACTIVE IN")
              .jsonPath("agencyId").isEqualTo("LEI")
              .jsonPath("assignedLivingUnit.agencyId").isEqualTo("LEI")
              .jsonPath("assignedLivingUnit.description").isEqualTo("RECP")

            getOffender(offenderNo)
              .isOk
              .expectBody()
              .jsonPath("assignedLivingUnit.agencyId").isEqualTo("LEI")
              .jsonPath("assignedLivingUnit.description").isEqualTo("RECP")
          }

          @Test
          internal fun `will not record any bed history changes`() {
            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), null),
              )

            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI"))

            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), null),
              )
          }
        }

        @Nested
        @DisplayName("Returning to a different prison")
        inner class DifferentPrison {
          @Test
          internal fun `will set the prisoner as active in`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("status").isEqualTo("ACTIVE IN")

            getOffender(offenderNo)
              .isOk
              .expectBody()
              .jsonPath("inOutStatus").isEqualTo("IN")
              .jsonPath("status").isEqualTo("ACTIVE IN")
          }

          @Test
          internal fun `will create a new bed assignment history record with no reason code`() {
            val receiveDateTime = LocalDateTime.now().minusMinutes(2)
            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple("ADM", bookingInTime.toLocalDate(), null),
              )

            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))

            assertThat(testDataContext.getBedAssignments(bookingId))
              .extracting(
                BedAssignmentHistory::getAssignmentReason,
                BedAssignmentHistory::getAssignmentDate,
                BedAssignmentHistory::getAssignmentEndDate
              )
              .containsExactly(
                tuple(
                  "ADM",
                  bookingInTime.toLocalDate(),
                  null
                ), // trigger end_prev_bed_assg_hty will add an end date to the previous movement, but can't be tested
                tuple(null, receiveDateTime.toLocalDate(), null),
              )
          }
        }
      }

      @Nested
      @DisplayName("With a scheduled temporary absence")
      inner class WithScheduledTAP {
        private var scheduledEventId: Long = 0
        private var addressId: Long = -22

        @BeforeEach
        internal fun setUp() {
          scheduledEventId = dataLoaderTransaction.save {
            testDataContext.createScheduledTemporaryAbsence(
              bookingId,
              addressId, // corporate address
              LocalDateTime.now().minusDays(1),
            ).id
          }
          assertThat(testDataContext.getScheduledMovements(bookingId)).extracting("eventStatus.code")
            .containsExactly("SCH")
          testDataContext.transferOutToTemporaryAbsence(
            offenderNo,
            toLocation = "18248",
            shouldReleaseBed = false,
            scheduledEventId
          )
        }

        @Nested
        inner class SamePrison {
          @Test
          internal fun `will complete scheduled movement event`() {
            assertThat(testDataContext.getScheduledMovements(bookingId)).extracting("eventStatus.code")
              .containsExactly("COMP", "SCH")

            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI"))

            val scheduledTAPEvents = testDataContext.getScheduledMovements(bookingId)
            assertThat(scheduledTAPEvents).extracting("eventStatus.code").containsExactly("COMP", "COMP")
            assertThat(testDataContext.getMovements(bookingId).last().eventId).isEqualTo(scheduledTAPEvents.last().id)
          }

          @Test
          internal fun `from addressId should taken from the OUT addressId`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI"))

            assertThat(lastMovement(bookingId).fromAddressId).isEqualTo(addressId)
          }

          @Test
          internal fun `escort type should be taken from the OUT movement (which is taken from schedule)`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI"))

            assertThat(lastMovement(bookingId).escortCode).isEqualTo("L")
          }

          @Test
          internal fun `from agency will not be set`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "LEI"))

            assertThat(dataLoaderTransaction.get { lastMovement(bookingId).fromAgency }).isNull()
          }
        }
        @Nested
        inner class DifferentPrison {
          @Test
          internal fun `will not complete scheduled movement event`() {
            assertThat(testDataContext.getScheduledMovements(bookingId)).extracting("eventStatus.code")
              .containsExactly("COMP", "SCH")

            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))

            val scheduledTAPEvents = testDataContext.getScheduledMovements(bookingId)
            assertThat(scheduledTAPEvents).extracting("eventStatus.code").containsExactly("COMP", "SCH")
          }

          @Test
          internal fun `from addressId will not be populated since this is a transfer`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))

            assertThat(lastMovement(bookingId).fromAddressId).isNull()
          }

          @Test
          internal fun `from agency will be taken from OUT movement`() {
            temporaryAbsenceArrival(temporaryAbsenceArrivalRequest(agencyId = "MDI"))

            assertThat(dataLoaderTransaction.get { lastMovement(bookingId).fromAgency.id }).isEqualTo("LEI")
          }
        }
      }

      private fun temporaryAbsenceArrival(body: String) = temporaryAbsenceArrival(offenderNo, body).isOk
    }

    @Nested
    @DisplayName("With a team assignment")
    inner class WithTeamAssignment {
      private lateinit var offenderNo: String
      private var bookingId: Long = 0
      private val bookingInTime = LocalDateTime.now().minusDays(1)
      private lateinit var transferOutDateTime: LocalDateTime

      @BeforeEach
      internal fun setUp() {
        dataLoaderTransaction.load(
          OffenderBuilder().withBooking(
            OffenderBookingBuilder(
              prisonId = "LEI",
              bookingInTime = bookingInTime,
              cellLocation = "LEI-RECP"
            )
              .withTeamAssignment(OffenderTeamAssignmentBuilder(team))
          ),
          testDataContext
        )
          .also {
            offenderNo = it.offenderNo
            bookingId = it.bookingId
          }

        transferOutDateTime =
          testDataContext.transferOutToTemporaryAbsence(
            offenderNo,
            toLocation = "18248",
            shouldReleaseBed = false,
          )
      }

      @Nested
      @DisplayName("Returning to a different prison")
      inner class DifferentPrison {
        @Test
        internal fun `will notify team of the automatic transfer`() {
          val receiveDateTime = LocalDateTime.now()

          temporaryAbsenceArrival(
            offenderNo,
            temporaryAbsenceArrivalRequest(agencyId = "MDI", dateTime = receiveDateTime)
          ).isOk

          // we can't test store procedure is called since we are running against H2, so next best thing is
          // to assert service is called with the correct parameters that are passed to stored procedure
          verify(workflowTaskService).createTaskAutomaticTransfer(
            check {
              assertThat(it.bookingId).isEqualTo(bookingId)
            },
            check {
              assertThat(it.fromAgency.id).isEqualTo("LEI")
              assertThat(it.toAgency.id).isEqualTo("MDI")
              assertThat(it.movementReason.code).isEqualTo("TRNTAP")
              assertThat(it.movementDate).isEqualTo(receiveDateTime.toLocalDate())
              assertThat(it.movementTime).isEqualTo(receiveDateTime)
            },
            check {
              assertThat(it.id).isEqualTo(team.id)
            }
          )
        }
      }

      @Nested
      @DisplayName("Returning to the same prison")
      inner class SamePrison {
        @Test
        internal fun `will not notify team of a non transfer`() {
          val receiveDateTime = LocalDateTime.now()

          temporaryAbsenceArrival(
            offenderNo,
            temporaryAbsenceArrivalRequest(agencyId = "LEI", dateTime = receiveDateTime)
          ).isOk

          verifyNoInteractions(workflowTaskService)
        }
      }
    }

    @Nested
    @DisplayName("Failed to transfer in")
    inner class Failed {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        OffenderBuilder().withBooking(
          OffenderBookingBuilder(
            prisonId = "LEI",
            bookingInTime = LocalDateTime.now().minusDays(10),
            cellLocation = "LEI-RECP"
          ).withIEPLevel("ENH").withInitialVoBalances(2, 8)
        ).save(testDataContext).also {
          offenderNo = it.offenderNo
        }
      }

      @Nested
      @DisplayName("Returning back to the same prison")
      inner class SamePrison {
        @Test
        internal fun `cannot arrive when not already out on TAP`() {
          temporaryAbsenceArrival(offenderNo, temporaryAbsenceArrivalRequest(agencyId = "LEI"))
            .isBadRequest
            .expectBody()
            .jsonPath("userMessage").isEqualTo("Prisoner is not currently out")
        }

        @Test
        internal fun `cannot arrive when not previously out on TAP`() {
          testDataContext.release(offenderNo)

          temporaryAbsenceArrival(offenderNo, temporaryAbsenceArrivalRequest(agencyId = "LEI"))
            .isBadRequest
            .expectBody()
            .jsonPath("userMessage").isEqualTo("Latest movement not a temporary absence")
        }

        @Test
        internal fun `cannot arrive with a time in the future`() {
          testDataContext.transferOutToTemporaryAbsence(offenderNo, toLocation = "18248", shouldReleaseBed = false)

          temporaryAbsenceArrival(
            offenderNo,
            temporaryAbsenceArrivalRequest(agencyId = "LEI", dateTime = LocalDateTime.now().plusMinutes(2))
          )
            .isBadRequest
            .expectBody()
            .jsonPath("userMessage").isEqualTo("Transfer cannot be done in the future")
        }

        @Test
        internal fun `cannot arrive with a time before transfer out time`() {
          val transferOutDateTime =
            testDataContext.transferOutToTemporaryAbsence(offenderNo, toLocation = "18248", shouldReleaseBed = false)

          temporaryAbsenceArrival(
            offenderNo,
            temporaryAbsenceArrivalRequest(agencyId = "LEI", dateTime = transferOutDateTime.minusHours(2))
          )
            .isBadRequest
            .expectBody()
            .jsonPath("userMessage").isEqualTo("Movement cannot be before the previous active movement")
        }
      }
    }

    private fun temporaryAbsenceArrival(offenderNo: String, body: String) = webTestClient.put()
      .uri("/api/offenders/{nomsId}/temporary-absence-arrival", offenderNo)
      .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(body))
      .exchange()
      .expectStatus()

    private fun temporaryAbsenceArrivalRequest(
      agencyId: String = "LEI",
      commentText: String = "admitted",
      movementReasonCode: String? = null,
      dateTime: LocalDateTime = LocalDateTime.now().minusMinutes(2)
    ) = """
                  {
                    "agencyId":"$agencyId",
                    "commentText":"$commentText",
                    ${movementReasonCode?.let { """ "movementReasonCode":"$it", """ } ?: ""}
                    "dateTime": "${dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                  }
    """.trimIndent()
  }

  private fun getOffender(offenderNo: String): StatusAssertions =
    webTestClient.get()
      .uri("/api/offenders/{offenderNo}", offenderNo)
      .headers(
        setAuthorisation(
          listOf("ROLE_SYSTEM_USER")
        )
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()

  private fun lastMovement(bookingId: Long) = testDataContext.getMovements(bookingId).find { it.isActive }!!
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
