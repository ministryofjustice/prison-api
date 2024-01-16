@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.StatusAssertions
import uk.gov.justice.hmpps.prison.service.DataLoaderTransaction
import uk.gov.justice.hmpps.prison.util.builders.ExternalServiceBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderNoPayPeriodBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderPayStatusBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderProgramProfileBuilder
import uk.gov.justice.hmpps.prison.util.builders.ServiceAgencySwitchBuilder
import uk.gov.justice.hmpps.prison.util.builders.getBedAssignments
import uk.gov.justice.hmpps.prison.util.builders.getCaseNotes
import uk.gov.justice.hmpps.prison.util.builders.getKeyDateAdjustments
import uk.gov.justice.hmpps.prison.util.builders.getMovements
import uk.gov.justice.hmpps.prison.util.builders.getOffenderBooking
import uk.gov.justice.hmpps.prison.util.builders.getOffenderNoPayPeriods
import uk.gov.justice.hmpps.prison.util.builders.getOffenderPayStatus
import uk.gov.justice.hmpps.prison.util.builders.getOffenderProgramProfiles
import uk.gov.justice.hmpps.prison.util.builders.getSentenceAdjustments
import uk.gov.justice.hmpps.prison.util.builders.release
import uk.gov.justice.hmpps.prison.util.builders.transferOutToCourt
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OffenderResourceIntTest_release : ResourceTest() {

  @Autowired
  private lateinit var dataLoaderTransaction: DataLoaderTransaction

  @Nested
  @DisplayName("POST /offenders/{offenderNo}/release")
  inner class ReleaseOffender {
    lateinit var offenderNo: String
    var bookingId: Long? = null
    lateinit var externalServiceName: String

    @AfterEach
    fun `tear down`() {
      if (::externalServiceName.isInitialized) {
        testDataContext.dataLoader.externalServiceRepository.deleteById(externalServiceName)
      }
      bookingId?.let {
        dataLoaderTransaction.transaction {
          with(testDataContext.dataLoader) {
            bedAssignmentHistoriesRepository.deleteByOffenderBooking_BookingId(it)
            offenderProgramProfileRepository.deleteByOffenderBooking_BookingId(it)
            offenderPayStatusRepository.deleteByBookingId(it)
            offenderBookingRepository.deleteById(it)
            offenderNoPayPeriodRepository.deleteByBookingId(it)
          }
        }
      }
    }

    @Nested
    inner class Authorisation {

      @BeforeEach
      fun setUp() {
        createBooking()
      }

      @Test
      fun `401 when user does not even have token`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/release", offenderNo)
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(releaseRequest())
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `403 when user does not have any roles`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/release", offenderNo)
          .headers(setAuthorisation(listOf()))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(releaseRequest())
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
      }

      @Test
      fun `403 when user does not have required role`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/release", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_BANANAS")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(releaseRequest())
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
      }

      @Test
      fun `404 when the user is unknown`() {
        releaseOffender(
          offenderNo,
          releaseRequest(),
          username = "UNKNOWN",
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Offender booking with id $bookingId not found.")
      }

      @Test
      fun `404 when there is no user`() {
        releaseOffender(
          offenderNo,
          releaseRequest(),
          username = "",
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Offender booking with id $bookingId not found.")
      }

      @Test
      fun `404 when the user does not have access to the caseload`() {
        releaseOffender(
          offenderNo,
          releaseRequest(),
          username = "IEP_USER",
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Offender booking with id $bookingId not found.")
      }
    }

    @Nested
    @DisplayName("when release is rejected")
    inner class Failure {
      @Test
      fun `404 when offender not found`() {
        offenderNo = "Z9999ZZ"

        // Given offender does not exist
        getOffender(offenderNo).isNotFound

        releaseOffender(
          offenderNo,
          releaseRequest(),
        ).isNotFound
      }

      @Test
      fun `404 when offender has no bookings`() {
        createPrisonerWithNoBooking()

        // Given offender has no bookings
        getOffender(offenderNo).isOk
          .expectBody()
          .jsonPath("bookingNo")
          .doesNotExist()

        releaseOffender(
          offenderNo,
          releaseRequest(),
        ).isNotFound
      }

      @Test
      fun `404 when offender is not active and user DOES NOT have INACTIVE_BOOKINGS role`() {
        createBooking().also { testDataContext.release(offenderNo) }

        releaseOffender(
          offenderNo,
          releaseRequest(),
        ).isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Offender booking with id $bookingId not found.")
      }

      @Test
      fun `400 when offender is not active and user DOES have INACTIVE_BOOKINGS role`() {
        createBooking().also { testDataContext.release(offenderNo) }

        releaseOffender(
          offenderNo,
          releaseRequest(),
          roles = listOf("ROLE_RELEASE_PRISONER", "ROLE_INACTIVE_BOOKINGS"),
        ).isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Booking $bookingId is not active")
      }

      @Test
      fun `400 when offender is active but OUT, for instance currently out at court`() {
        createBooking().also { testDataContext.transferOutToCourt(offenderNo, "COURT1", expectedAgency = "SYI") }

        releaseOffender(
          offenderNo,
          releaseRequest(),
        ).isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Booking $bookingId is not IN")
      }

      @Test
      fun `404 when the movement reason is unknown for type REL`() {
        createBooking()

        releaseOffender(
          offenderNo,
          releaseRequest(movementReasonCode = "ZZZ"),
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("No movement reason ZZZ found")
      }

      @Test
      fun `400 when the release time is in the future`() {
        createBooking()

        releaseOffender(
          offenderNo,
          releaseRequest(releaseTime = LocalDateTime.now().plusHours(1)),
        )
          .isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be done in the future")
      }

      @Test
      fun `400 when the release time is before last movement`() {
        createBooking()

        releaseOffender(
          offenderNo,
          releaseRequest(releaseTime = LocalDateTime.now().minusDays(2)),
        )
          .isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be before the previous active movement")
      }

      @Test
      fun `404 when the to location is unknown`() {
        createBooking()

        releaseOffender(
          offenderNo,
          releaseRequest(toLocationCode = "ZZZ"),
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("No ZZZ agency found")
      }
    }

    @Nested
    @DisplayName("when release is a success")
    inner class SideEffects {
      @BeforeEach
      fun setUp() {
        createBooking()
      }

      @Test
      fun `should update booking`() {
        releaseOffender(offenderNo, releaseRequest())
          .isOk
          .expectBody()
          .jsonPath("offenderNo").isEqualTo(offenderNo)
          .jsonPath("bookingId").isEqualTo("$bookingId")
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("agencyId").isEqualTo("OUT")
          .jsonPath("assignedLivingUnitId").doesNotExist()
          .jsonPath("inOutStatus").isEqualTo("OUT")
          .jsonPath("status").isEqualTo("INACTIVE OUT")
          .jsonPath("statusReason").isEqualTo("REL-CR")
          .jsonPath("lastMovementTypeCode").isEqualTo("REL")
          .jsonPath("lastMovementReasonCode").isEqualTo("CR")

        testDataContext.getOffenderBooking(bookingId!!)?.also {
          assertThat(it.isActive).isFalse()
          assertThat(it.location.id).isEqualTo("OUT")
        }
      }

      @Test
      fun `should create an outbound movement`() {
        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getMovements(bookingId!!)
          .also {
            with(it.last()) {
              assertThat(fromAgency.id).isEqualTo("SYI")
              assertThat(toAgency.id).isEqualTo("OUT")
              assertThat(movementDate).isEqualTo(LocalDate.now())
              assertThat(isActive).isTrue()
            }
          }
      }

      @Test
      fun `should make old movements inactive`() {
        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getMovements(bookingId!!)
          .filter { it.isActive }
          .also {
            assertThat(it).hasSize(1)
          }
      }

      @Test
      fun `should create a case note for the release`() {
        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getCaseNotes(offenderNo)
          .maxBy { it.caseNoteId }
          .also {
            assertThat(it.type).isEqualTo("PRISON")
            assertThat(it.subType).isEqualTo("RELEASE")
            assertThat(it.text).contains("Released from SHREWSBURY for reason: Conditional Release")
          }
      }

      @Test
      fun `should update the bed assignment history`() {
        testDataContext.getBedAssignments(bookingId!!)
          .maxBy { it.assignmentDate }
          .also {
            assertThat(it.assignmentEndDate).isNull()
          }

        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getBedAssignments(bookingId!!)
          .maxBy { it.assignmentDate }
          .also {
            assertThat(it.assignmentEndDate).isEqualTo(LocalDate.now())
          }
      }

      @Test
      fun `should deactivate sentence adjustments`() {
        testDataContext.getSentenceAdjustments(bookingId!!)
          .forEach {
            assertThat(it.isActive).isTrue()
          }

        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getSentenceAdjustments(bookingId!!)
          .forEach {
            assertThat(it.isActive).isFalse()
          }
      }

      @Test
      fun `should deactivate key date adjustments`() {
        testDataContext.getKeyDateAdjustments(bookingId!!)
          .forEach {
            assertThat(it.isActive).isTrue()
          }

        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getKeyDateAdjustments(bookingId!!)
          .forEach {
            assertThat(it.isActive).isFalse()
          }
      }

      @Test
      fun `should end all offender pay statuses`() {
        testDataContext.getOffenderPayStatus(bookingId!!)
          .forEach {
            assertThat(it.endDate).isNull()
          }

        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getOffenderPayStatus(bookingId!!)
          .forEach {
            assertThat(it.endDate).isBeforeOrEqualTo(LocalDate.now())
          }
      }

      @Test
      fun `should end all offender no pay periods`() {
        testDataContext.getOffenderNoPayPeriods(bookingId!!)
          .forEach {
            assertThat(it.endDate).isNull()
          }

        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getOffenderNoPayPeriods(bookingId!!)
          .forEach {
            assertThat(it.endDate).isBeforeOrEqualTo(LocalDate.now())
          }
      }

      @Test
      fun `should deactivate events`() {
        testDataContext.getOffenderProgramProfiles(bookingId!!, "ALLOC")
          .forEach {
            assertThat(it.endDate).isNull()
          }

        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getOffenderProgramProfiles(bookingId!!, "ALLOC")
          .forEach {
            assertThat(it.endDate).isEqualTo(LocalDate.now())
          }
      }

      @Test
      fun `should NOT deactivate events if the prison's ACTIVITY feature switch is turned on`() {
        createExternalService()
        createServiceAgencySwitch()

        releaseOffender(offenderNo, releaseRequest()).isOk

        testDataContext.getOffenderProgramProfiles(bookingId!!, "ALLOC")
          .forEach {
            assertThat(it.endDate).isNull()
          }
      }

      @Test
      fun `should allow missing optional request fields`() {
        releaseOffender(offenderNo, releaseRequestWithoutNullables(movementReasonCode = "CR"))
          .isOk

        testDataContext.getOffenderBooking(bookingId!!)?.also {
          assertThat(it.isActive).isFalse()
          assertThat(it.location.id).isEqualTo("OUT")
          assertThat(it.statusReason).isEqualTo("REL-CR")
          assertThat(it.bookingEndDate.toLocalDate()).isEqualTo("${LocalDate.now()}")
        }
      }
    }

    private fun getOffender(offenderNo: String): StatusAssertions =
      webTestClient.get()
        .uri("/api/offenders/{offenderNo}", offenderNo)
        .headers(
          setAuthorisation(
            listOf("ROLE_SYSTEM_USER"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()

    private fun createPrisonerWithNoBooking() {
      OffenderBuilder(bookingBuilders = arrayOf()).save(testDataContext).also {
        offenderNo = it.offenderNo
        bookingId = it.bookingId
      }
    }

    private fun createBooking() {
      OffenderBuilder().withBooking(
        OffenderBookingBuilder(
          prisonId = "SYI",
          cellLocation = "SYI-A-1-1",
          programProfiles = listOf(OffenderProgramProfileBuilder()),
          payStatuses = listOf(OffenderPayStatusBuilder()),
          noPayPeriods = listOf(OffenderNoPayPeriodBuilder()),
        ),
      )
        .save(testDataContext)
        .also {
          offenderNo = it.offenderNo
          bookingId = it.bookingId
        }
    }

    private fun createExternalService(serviceName: String = "ACTIVITY") {
      ExternalServiceBuilder(serviceName).save(testDataContext.dataLoader)
        .also {
          externalServiceName = it.serviceName
        }
    }

    private fun createServiceAgencySwitch(serviceName: String = "ACTIVITY", agencyId: String = "SYI") =
      ServiceAgencySwitchBuilder(serviceName, agencyId).save(testDataContext.dataLoader)

    private fun releaseOffender(
      offenderNo: String,
      body: String,
      username: String = "ITAG_USER",
      roles: List<String> = listOf("ROLE_RELEASE_PRISONER"),
    ): StatusAssertions =
      webTestClient.put()
        .uri("/api/offenders/{offenderNo}/release", offenderNo)
        .headers(setAuthorisation(username, roles))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(body.trimIndent())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()

    private fun releaseRequest(
      movementReasonCode: String = "CR",
      releaseTime: LocalDateTime = LocalDateTime.now(),
      toLocationCode: String = "OUT",
    ): String =
      """
        {
           "movementReasonCode": "$movementReasonCode", 
           "releaseTime": "${releaseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
           "commentText": "released prisoner",
           "toLocationCode": "$toLocationCode" 
        }
      """.trimIndent()

    private fun releaseRequestWithoutNullables(movementReasonCode: String): String =
      """
        {
           "movementReasonCode": "$movementReasonCode"
        }
      """.trimIndent()
  }
}
