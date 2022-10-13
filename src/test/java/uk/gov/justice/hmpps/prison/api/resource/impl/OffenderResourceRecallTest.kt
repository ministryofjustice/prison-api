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
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.service.receiveandtransfer.TrustAccountService
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.builders.getBedAssignments
import uk.gov.justice.hmpps.prison.util.builders.getCaseNotes
import uk.gov.justice.hmpps.prison.util.builders.getCurrentIEP
import uk.gov.justice.hmpps.prison.util.builders.getMovements
import uk.gov.justice.hmpps.prison.util.builders.transferOut
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@WithMockUser
class OffenderResourceRecallTest : ResourceTest() {

  @MockBean
  private lateinit var trustAccountService: TrustAccountService

  @Nested
  @DisplayName("POST /offenders/{offenderNo}/recall")
  inner class RecallOffender {

    @Nested
    @DisplayName("when recall is rejected")
    inner class Failure {
      @Test
      internal fun `404 when offender not found`() {
        val offenderNo = "Z9999ZZ"

        // Given offender does not exist
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
          .expectStatus().isNotFound

        // when recall is requested
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "recallTime": "2020-01-01T12:00:00",
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      internal fun `404 when offender has no bookings`() {
        val offenderNo = createPrisonerWithNoBooking()

        // Given offender has no bookings
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
          .expectStatus().isOk
          .expectBody()
          .jsonPath("bookingNo")
          .doesNotExist()

        // when recall is requested
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "recallTime": "2020-01-01T12:00:00",
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      internal fun `400 when offender is still active, for instance are in another prison `() {
        val offenderNo = createActiveBooking()

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "recallTime": "2020-01-01T12:00:00",
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Prisoner is currently active")
      }

      @Test
      internal fun `400 when offender is inactive but not OUT, for instance currently being transferred`() {
        val offenderNo = createActiveBooking(prisonId = "MDI").also { testDataContext.transferOut(it) }

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "recallTime": "2020-01-01T12:00:00",
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Prisoner is not currently OUT")
      }

      @Test
      internal fun `404 when trying to recall from a location that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "recallTime": "2020-01-01T12:00:00",
               "fromLocationId": "ZZZ", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("ZZZ is not a valid from location")
      }

      @Test
      internal fun `404 (possibly incorrectly) when trying to recall in from the OUT location (even though this the default when no supplied)`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "recallTime": "2020-01-01T12:00:00",
               "fromLocationId": "OUT", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("OUT is not a valid from location")
      }

      @Test
      internal fun `404 when trying to recall with an imprisonment status that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "ZZZ", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("No imprisonment status ZZZ found")
      }

      @Test
      internal fun `404 when trying to recall in to a prison that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "ZZZ", 
               "recallTime": "2020-01-01T12:00:00",
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("ZZZ prison not found")
      }

      @Test
      internal fun `404 when trying to recall in to a cell that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "recallTime": "2020-01-01T12:00:00",
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-BANANAS"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("SYI-BANANAS cell location not found")
      }

      @Test
      internal fun `409 when trying to recall in to a cell that is full`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "MDI-FULL"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isEqualTo(409)
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("The cell MDI-FULL does not have any available capacity")
      }

      @Test
      internal fun `400 when trying to recall in prisoner in the future (and return a slightly inaccurate message)`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "recallTime": "${
            LocalDateTime.now().plusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }"
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Transfer cannot be done in the future")
      }

      @Test
      internal fun `400 when trying to recall in prisoner before they were released from previous prison`() {
        val offenderNo = createInactiveBooking()

        // when offender is recalled request is rejected
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "recallTime": "${
            LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }"
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
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
        val bookingId = webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "MDI", 
               "movementReasonCode": "24" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        assertThat(testDataContext.getMovements(bookingId).last().fromAgency.id)
          .isEqualTo("OUT")
      }

      @Test
      internal fun `will by default set movement time to now`() {
        // when recall is requested
        val bookingId = webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "movementReasonCode": "24" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        assertThat(testDataContext.getMovements(bookingId).last().toAgency.id)
          .isEqualTo("SYI")
        assertThat(testDataContext.getMovements(bookingId).last().movementTime)
          .isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS))
      }

      @Test
      internal fun `will by default not set an imprisonment status`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "movementReasonCode": "24" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        // then we have an active booking with original status
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
          .expectStatus().isOk
          .expectBody()
          .jsonPath("imprisonmentStatus").isEqualTo("SENT03")
      }

      @Test
      internal fun `will set imprisonment status when supplied`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "imprisonmentStatus": "CUR_ORA", 
               "movementReasonCode": "24" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        // then we have an active booking with original status
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
          .expectStatus().isOk
          .expectBody()
          .jsonPath("imprisonmentStatus").isEqualTo("CUR_ORA")
      }

      @Test
      internal fun `will by default place prisoner in reception`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "MDI", 
               "movementReasonCode": "24" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        // then we have an active booking with cell location set to reception
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
          .expectStatus().isOk
          .expectBody()
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("MDI")
          .jsonPath("assignedLivingUnit.description").isEqualTo("RECP")
      }

      @Test
      internal fun `will create a new movement for the recall`() {
        val bookingId = webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "movementReasonCode": "24" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        assertThat(testDataContext.getMovements(bookingId))
          .extracting(
            ExternalMovement::getMovementSequence,
            ExternalMovement::getMovementDirection,
            ExternalMovement::isActive
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

        val bookingId = webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "cellLocation": "SYI-A-1-1",     
               "movementReasonCode": "24",
               "recallTime": "${bookingInTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        assertThat(testDataContext.getBedAssignments(bookingId))
          .extracting(
            BedAssignmentHistory::getAssignmentReason,
            BedAssignmentHistory::getAssignmentDate,
            BedAssignmentHistory::getAssignmentEndDate
          )
          .contains(
            tuple(
              "ADM",
              bookingInTime.toLocalDate(),
              null
            ),
          )
      }

      @Test
      internal fun `will reset IEP level back to default for prison`() {
        assertThat(testDataContext.getCurrentIEP(offenderNo))
          .extracting(PrivilegeSummary::getIepLevel)
          .isEqualTo("Enhanced")

        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "MDI", 
               "movementReasonCode": "24" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        assertThat(testDataContext.getCurrentIEP(offenderNo))
          .extracting(PrivilegeSummary::getIepLevel)
          .isEqualTo("Entry")
      }

      @Test
      internal fun `will create admission case note`() {
        val bookingId = webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "MDI", 
               "movementReasonCode": "24" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        val caseNote = testDataContext.getCaseNotes(bookingId).maxBy { it.creationDateTime }
        assertThat(caseNote.type).isEqualTo("TRANSFER")
        assertThat(caseNote.subType).isEqualTo("FROMTOL")
        assertThat(caseNote.text).isEqualTo("Offender admitted to MOORLAND for reason: Recall From Intermittent Custody from OUTSIDE.")
      }

      @Test
      internal fun `will create trust accounts`() {
        val inmate = webTestClient.put()
          .uri("/api/offenders/{offenderNo}/recall", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_TRANSFER_PRISONER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!

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
          }
        )
      }
    }
  }

  fun createActiveBooking(prisonId: String = "MDI"): String = OffenderBuilder().withBooking(
    OffenderBookingBuilder(
      prisonId = prisonId,
    )
  ).save(testDataContext).offenderNo

  fun createPrisonerWithNoBooking(): String =
    OffenderBuilder(bookingBuilders = arrayOf()).save(testDataContext).offenderNo

  fun createInactiveBooking(iepLevel: String = "ENH", imprisonmentStatus: String = "SENT03"): String =
    OffenderBuilder().withBooking(
      OffenderBookingBuilder(
        prisonId = "MDI",
        released = true,
        imprisonmentStatus = imprisonmentStatus,
      ).withIEPLevel(iepLevel)
    ).save(testDataContext).offenderNo
}
