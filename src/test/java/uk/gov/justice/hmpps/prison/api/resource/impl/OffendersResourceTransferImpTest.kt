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
import uk.gov.justice.hmpps.prison.util.builders.getBedAssignments
import uk.gov.justice.hmpps.prison.util.builders.getCaseNotes
import uk.gov.justice.hmpps.prison.util.builders.getCourtHearings
import uk.gov.justice.hmpps.prison.util.builders.getCurrentIEP
import uk.gov.justice.hmpps.prison.util.builders.getMovements
import uk.gov.justice.hmpps.prison.util.builders.getVOBalanceDetails
import uk.gov.justice.hmpps.prison.util.builders.release
import uk.gov.justice.hmpps.prison.util.builders.transferOut
import uk.gov.justice.hmpps.prison.util.builders.transferOutToCourt
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
      internal fun `will reset IEP level back to default for prison`() {
        assertThat(testDataContext.getCurrentIEP(offenderNo))
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

        assertThat(testDataContext.getCurrentIEP(offenderNo))
          .extracting(PrivilegeSummary::getIepLevel)
          .isEqualTo("Entry")
      }

      @Test
      internal fun `will not create a visit order balance adjustment even though IEP levels has changed`() {
        // Why is this odd test here?
        // NOMIS was supposed to update the balance after an IEP was updated.
        // This was part of change SDU-187 that was reverted from production due to issues
        // If this change is implemented then we would need to port this functionality as well

        assertThat(testDataContext.getCurrentIEP(offenderNo))
          .extracting(PrivilegeSummary::getIepLevel)
          .isEqualTo("Enhanced")

        // vo balance exists with no adjustments
        assertThat(testDataContext.getVOBalanceDetails(offenderNo))
          .extracting(VisitBalances::getRemainingPvo, VisitBalances::getLatestIepAdjustDate)
          .containsExactly(8, null)

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

        // vo balance exists with no adjustments
        assertThat(testDataContext.getVOBalanceDetails(offenderNo))
          .extracting(VisitBalances::getRemainingPvo, VisitBalances::getLatestIepAdjustDate)
          .containsExactly(8, null)
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
          transferOutDateTime = testDataContext.transferOutToCourt(offenderNo, toLocation = "COURT1", shouldReleaseBed = true)
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
                tuple("19", transferOutDateTime.toLocalDate(), null), // trigger end_prev_bed_assg_hty will add an end date to the previous movement, but can't be tested
                tuple(null, receiveDateTime.toLocalDate(), null), // as per nomis
              )
          }

          @Test
          internal fun `will reset IEP level back to default for prison`() {
            assertThat(testDataContext.getCurrentIEP(offenderNo))
              .extracting(PrivilegeSummary::getIepLevel)
              .isEqualTo("Enhanced")

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

            assertThat(testDataContext.getCurrentIEP(offenderNo))
              .extracting(PrivilegeSummary::getIepLevel)
              .isEqualTo("Entry")
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
          transferOutDateTime = testDataContext.transferOutToCourt(offenderNo, toLocation = "COURT1", shouldReleaseBed = false)
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
                tuple("ADM", bookingInTime.toLocalDate(), null), // trigger end_prev_bed_assg_hty will add an end date to the previous movement, but can't be tested
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
          assertThat(testDataContext.getCourtHearings(bookingId)).extracting("eventStatus.code").containsExactly("COMP", "SCH")
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

        transferOutDateTime = testDataContext.transferOutToCourt(offenderNo, toLocation = "COURT1", shouldReleaseBed = false)
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
          val transferOutDateTime = testDataContext.transferOutToCourt(offenderNo, toLocation = "COURT1", shouldReleaseBed = false)

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
