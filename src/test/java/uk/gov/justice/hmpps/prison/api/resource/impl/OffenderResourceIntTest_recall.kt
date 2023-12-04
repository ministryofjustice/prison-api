package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.StatusAssertions
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.service.enteringandleaving.TrustAccountService
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.builders.getBedAssignments
import uk.gov.justice.hmpps.prison.util.builders.getCaseNotes
import uk.gov.justice.hmpps.prison.util.builders.getMovements
import uk.gov.justice.hmpps.prison.util.builders.transferOut
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@WithMockUser
class OffenderResourceIntTest_recall : ResourceTest() {

  @MockBean
  private lateinit var trustAccountService: TrustAccountService

  @Nested
  @DisplayName("POST /offenders/{offenderNo}/recall")
  inner class RecallOffender {
    @Nested
    inner class Authorisation {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        offenderNo = createInactiveBooking()
      }

      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(recallRequest())
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `should return 403 when user does not have any roles`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(setAuthorisation(listOf()))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(recallRequest())
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
      }

      @Test
      fun `should return 403 when user does not have required role`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_BANANAS")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(recallRequest())
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
      }
    }

    @Nested
    @DisplayName("when recall is rejected")
    inner class Failure {
      @Test
      internal fun `404 when offender not found`() {
        val offenderNo = "Z9999ZZ"

        // Given offender does not exist
        getOffender(offenderNo).isNotFound

        // when recall is requested
        recallOffender(
          offenderNo,
          recallRequest(),
        ).isNotFound
      }

      @Test
      internal fun `404 when offender has no bookings`() {
        val offenderNo = createPrisonerWithNoBooking()

        // Given offender has no bookings
        getOffender(offenderNo).isOk
          .expectBody()
          .jsonPath("bookingNo")
          .doesNotExist()

        // when recall is requested
        recallOffender(
          offenderNo,
          recallRequest(),
        ).isNotFound
      }

      @Test
      internal fun `400 when offender is still active, for instance are in another prison `() {
        val offenderNo = createActiveBooking()

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(),
        ).isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Prisoner is currently active")
      }

      @Test
      internal fun `400 when offender is inactive but not OUT, for instance currently being transferred`() {
        val offenderNo = createActiveBooking(prisonId = "MDI").also { testDataContext.transferOut(it) }

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(),
        ).isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Prisoner is not currently OUT")
      }

      @Test
      internal fun `404 when trying to recall from a location that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(fromLocationId = "ZZZ"),
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("ZZZ is not a valid from location")
      }

      @Test
      internal fun `404 (possibly incorrectly) when trying to recall in from the OUT location (even though this the default when no supplied)`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(fromLocationId = "OUT"),
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("OUT is not a valid from location")
      }

      @Test
      internal fun `404 when trying to recall with an imprisonment status that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(imprisonmentStatus = "ZZZ"),
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("No imprisonment status ZZZ found")
      }

      @Test
      internal fun `404 when trying to recall in to a prison that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(prisonId = "ZZZ"),
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("ZZZ prison not found")
      }

      @Test
      internal fun `404 when trying to recall in to a cell that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(prisonId = "SYI", cellLocation = "SYI-BANANAS"),
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("SYI-BANANAS cell location not found")
      }

      @Test
      internal fun `409 when trying to recall in to a cell that is full`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(prisonId = "MDI", cellLocation = "MDI-FULL"),
        )
          .isEqualTo(409)
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("The cell MDI-FULL does not have any available capacity")
      }

      @Test
      internal fun `400 when trying to recall in prisoner in the future`() {
        val offenderNo = createInactiveBooking()
        val twoMinutesInTheFuture = LocalDateTime.now().plusMinutes(2)

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(recallTime = twoMinutesInTheFuture),
        )
          .isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be done in the future")
      }

      @Test
      internal fun `400 when trying to recall in prisoner before they were released from previous prison`() {
        val offenderNo = createInactiveBooking()
        val twoMonthsInThePastBeforeLastRelease = LocalDateTime.now().minusMonths(2)

        // when offender is recalled request is rejected
        recallOffender(
          offenderNo,
          recallRequest(recallTime = twoMonthsInThePastBeforeLastRelease),
        )
          .isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be before the previous active movement")
      }
    }

    @Nested
    @DisplayName("when recall is a success")
    inner class SideEffects {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        offenderNo = createInactiveBooking(iepLevel = "ENH", imprisonmentStatus = "SENT03")
      }

      @Test
      internal fun `will by default set last location to OUT`() {
        // when recall is requested
        val bookingId = recallOffender(
          offenderNo,
          """
            {
               "prisonId": "MDI", 
               "movementReasonCode": "24" 
            }
            """,
        ).inmate().bookingId

        assertThat(testDataContext.getMovements(bookingId).last().fromAgency.id)
          .isEqualTo("OUT")
      }

      @Test
      internal fun `will recall prisoner`() {
        val lastBookingId = getOffender(offenderNo).inmate().bookingId

        // when recall is requested
        recallOffender(
          offenderNo,
          """
            {
               "prisonId": "MDI", 
               "movementReasonCode": "24" 
            }
            """,
        )
          .isOk
          .expectBody()
          .jsonPath("inOutStatus").isEqualTo("IN")
          .jsonPath("status").isEqualTo("ACTIVE IN")
          .jsonPath("lastMovementTypeCode").isEqualTo("ADM")
          .jsonPath("lastMovementReasonCode").isEqualTo("24")
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("agencyId").isEqualTo("MDI")
          .jsonPath("bookingId").isEqualTo(lastBookingId)

        getOffender(offenderNo).isOk
          .expectBody()
          .jsonPath("inOutStatus").isEqualTo("IN")
          .jsonPath("status").isEqualTo("ACTIVE IN")
          .jsonPath("lastMovementTypeCode").isEqualTo("ADM")
          .jsonPath("lastMovementReasonCode").isEqualTo("24")
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("agencyId").isEqualTo("MDI")
          .jsonPath("bookingId").isEqualTo(lastBookingId)
      }

      @Test
      internal fun `will by default set movement time to now`() {
        // when recall is requested
        val bookingId = recallOffender(
          offenderNo,
          """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "movementReasonCode": "24" 
            }
            """,
        ).inmate().bookingId

        assertThat(testDataContext.getMovements(bookingId).last().toAgency.id)
          .isEqualTo("SYI")
        assertThat(testDataContext.getMovements(bookingId).last().movementTime)
          .isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS))
      }

      @Test
      internal fun `will by default not set an imprisonment status`() {
        recallOffender(
          offenderNo,

          """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "movementReasonCode": "24" 
            }
          """.trimIndent(),
        ).isOk

        // then we have an active booking with original status
        getOffender(offenderNo).isOk
          .expectBody()
          .jsonPath("imprisonmentStatus").isEqualTo("SENT03")
      }

      @Test
      internal fun `will set imprisonment status when supplied`() {
        recallOffender(
          offenderNo,
          """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "imprisonmentStatus": "CUR_ORA", 
               "movementReasonCode": "24" 
            }
          """.trimIndent(),
        ).isOk

        // then we have an active booking with original status
        getOffender(offenderNo).isOk
          .expectBody()
          .jsonPath("imprisonmentStatus").isEqualTo("CUR_ORA")
      }

      @Test
      internal fun `will by default place prisoner in reception`() {
        recallOffender(
          offenderNo,
          """
            {
               "prisonId": "MDI", 
               "movementReasonCode": "24" 
            }
          """.trimIndent(),
        ).isOk

        // then we have an active booking with cell location set to reception
        getOffender(offenderNo).isOk
          .expectBody()
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("MDI")
          .jsonPath("assignedLivingUnit.description").isEqualTo("RECP")
      }

      @Test
      internal fun `will create a new movement for the recall`() {
        val bookingId = recallOffender(
          offenderNo,
          """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "movementReasonCode": "24" 
            }
            """,
        ).inmate().bookingId

        assertThat(testDataContext.getMovements(bookingId))
          .extracting(
            ExternalMovement::getMovementSequence,
            ExternalMovement::getMovementDirection,
            ExternalMovement::isActive,
          )
          .containsExactly(
            tuple(1L, MovementDirection.IN, false),
            tuple(2L, MovementDirection.OUT, false),
            tuple(3L, MovementDirection.IN, true),
          )
      }

      @Test
      internal fun `will create a bed history for the recall`() {
        val bookingInTime = LocalDateTime.now()

        val bookingId = recallOffender(
          offenderNo,

          """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "movementReasonCode": "24",
               "recallTime": "${bookingInTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            }
          """.trimIndent(),
        ).inmate().bookingId

        assertThat(testDataContext.getBedAssignments(bookingId))
          .extracting(
            BedAssignmentHistory::getAssignmentReason,
            BedAssignmentHistory::getAssignmentDate,
            BedAssignmentHistory::getAssignmentEndDate,
          )
          .contains(
            tuple(
              "ADM",
              bookingInTime.toLocalDate(),
              null,
            ),
          )
      }

      @Test
      internal fun `will create admission case note`() {
        recallOffender(
          offenderNo,

          """
            {
               "prisonId": "MDI", 
               "movementReasonCode": "24" 
            }
          """.trimIndent(),
        )

        val caseNote = testDataContext.getCaseNotes(offenderNo).maxBy { it.creationDateTime }
        assertThat(caseNote.type).isEqualTo("TRANSFER")
        assertThat(caseNote.subType).isEqualTo("FROMTOL")
        assertThat(caseNote.text).isEqualTo("Offender admitted to MOORLAND for reason: Recall From Intermittent Custody from OUTSIDE.")
      }

      @Test
      internal fun `will create trust accounts`() {
        val inmate = recallOffender(
          offenderNo,
          """
            {
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24" 
            }
          """.trimIndent(),
        ).inmate()

        // since this calls a NOMIS store procedure the best we can do
        // is check the service was called with the correct parameters
        verify(trustAccountService).createTrustAccount(
          check {
            assertThat(it.bookingId).isEqualTo(inmate.bookingId)
            assertThat(it.rootOffender.id).isEqualTo(inmate.rootOffenderId)
          },
          check {
            assertThat(it.id).isEqualTo("COURT1")
          },
          check {
            assertThat(it.toAgency.id).isEqualTo("MDI")
            assertThat(it.movementReason.code).isEqualTo("24")
          },
        )
      }
    }
  }

  private fun getOffender(offenderNo: String): StatusAssertions =
    webTestClient.get()
      .uri("/api/offenders/{offenderNo}", offenderNo)
      .headers(
        setAuthorisation(
          listOf("ROLE_VIEW_PRISONER_DATA"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()

  fun createActiveBooking(prisonId: String = "MDI"): String = OffenderBuilder().withBooking(
    OffenderBookingBuilder(
      prisonId = prisonId,
    ),
  ).save(testDataContext).offenderNo

  fun createPrisonerWithNoBooking(): String =
    OffenderBuilder(bookingBuilders = arrayOf()).save(testDataContext).offenderNo

  fun createInactiveBooking(iepLevel: String = "ENH", imprisonmentStatus: String = "SENT03"): String =
    OffenderBuilder().withBooking(
      OffenderBookingBuilder(
        prisonId = "MDI",
        released = true,
        imprisonmentStatus = imprisonmentStatus,
      ).withIEPLevel(iepLevel),
    ).save(testDataContext).offenderNo

  private fun recallOffender(offenderNo: String, body: String): StatusAssertions =
    webTestClient.put()
      .uri("/api/offenders/{offenderNo}/recall", offenderNo)
      .headers(
        setAuthorisation(
          listOf("ROLE_TRANSFER_PRISONER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(
        body.trimIndent(),
      )
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()

  private fun StatusAssertions.inmate() = this.isOk
    .returnResult(InmateDetail::class.java)
    .responseBody.blockFirst()!!

  private fun recallRequest(
    fromLocationId: String = "COURT1",
    imprisonmentStatus: String = "CUR_ORA",
    prisonId: String = "SYI",
    cellLocation: String = "SYI-A-1-1",
    recallTime: LocalDateTime = LocalDateTime.now(),
  ): String = """
                {
                   "prisonId": "$prisonId", 
                   "recallTime": "${recallTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
                   "fromLocationId": "$fromLocationId", 
                   "movementReasonCode": "24", 
                   "youthOffender": "true", 
                   "imprisonmentStatus": "$imprisonmentStatus", 
                   "cellLocation": "$cellLocation"     
                }
                """
}
