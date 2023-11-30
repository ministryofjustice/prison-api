package uk.gov.justice.hmpps.prison.api.resource.impl

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.StatusAssertions
import uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital.Companion.OffenderType.BOOKING
import uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital.Companion.OffenderType.NO_BOOKING
import uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital.Companion.OffenderType.RELEASED
import uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital.Companion.OffenderType.TEMPORARY_ABSENCE
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.AWAIT_REMOVAL_TO_PSY_HOSPITAL
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.DISCHARGE_TO_PSY_HOSPITAL
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
import java.util.stream.Stream

class OffenderResourceIntTest_dischargeToHospital : ResourceTest() {

  lateinit var offenderNo: String
  var bookingId: Long? = null
  lateinit var externalServiceName: String

  @Autowired
  private lateinit var dataLoaderTransaction: DataLoaderTransaction

  companion object {
    enum class OffenderType {
      BOOKING,
      NO_BOOKING,
      TEMPORARY_ABSENCE,
      RELEASED,
    }

    @JvmStatic
    fun getOffenderTypes(): Stream<Arguments> =
      Stream.of(
        *OffenderType.entries.map {
          Arguments.of(it.name)
        }.toTypedArray(),
      )
  }

  private fun createOffender(offenderType: OffenderType) {
    when (offenderType) {
      BOOKING -> createOffenderBooking()
      NO_BOOKING -> createOffenderWithNoBooking()
      TEMPORARY_ABSENCE -> createOffenderOutAtCourt()
      RELEASED -> createOffenderAndRelease()
    }
  }

  private fun findBookingId(offenderType: OffenderType): Long =
    when (offenderType) {
      BOOKING -> bookingId!!
      NO_BOOKING -> testDataContext.getOffenderBooking(offenderNo, active = false)!!.bookingId
      TEMPORARY_ABSENCE -> bookingId!!
      RELEASED -> bookingId!!
    }

  @Nested
  @DisplayName("POST /offenders/{offenderNo}/discharge-to-hospital")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  inner class DischargeToHospital {

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
        createOffenderBooking()
      }

      @Test
      fun `401 when user does not even have token`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/discharge-to-hospital", offenderNo)
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(dischargeRequest())
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `403 when user does not have any roles`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/discharge-to-hospital", offenderNo)
          .headers(setAuthorisation(listOf()))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(dischargeRequest())
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
      }

      @Test
      fun `403 when user does not have required role`() {
        webTestClient.put()
          .uri("/api/offenders/{offenderNo}/discharge-to-hospital", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_BANANAS")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(dischargeRequest())
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
      }

      @Test
      fun `404 when the user is unknown`() {
        dischargeToHospital(
          offenderNo,
          dischargeRequest(),
          username = "UNKNOWN",
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Resource with id [UNKNOWN] not found.")
      }

      @Test
      fun `404 when there is no user`() {
        dischargeToHospital(
          offenderNo,
          dischargeRequest(),
          username = "",
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Resource with id [] not found.")
      }

      @Test
      fun `OK even if user does not have access to the caseload`() {
        dischargeToHospital(
          offenderNo,
          dischargeRequest(),
          username = "IEP_USER",
        ).isOk
      }
    }

    @Nested
    @DisplayName("when discharge is rejected")
    inner class Failure {
      @Test
      fun `404 when offender not found`() {
        offenderNo = "Z9999ZZ"

        getOffender(offenderNo).isNotFound

        dischargeToHospital(
          offenderNo,
          dischargeRequest(),
        ).isNotFound
      }

      @Test
      fun `400 when the discharge time is in the future`() {
        createOffenderBooking()

        dischargeToHospital(
          offenderNo,
          dischargeRequest(dischargeTime = LocalDateTime.now().plusHours(1)),
        )
          .isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be done in the future")
      }

      @Test
      fun `400 when the discharge time is before last movement`() {
        createOffenderBooking()

        dischargeToHospital(
          offenderNo,
          dischargeRequest(dischargeTime = LocalDateTime.now().minusDays(2)),
        )
          .isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be before the previous active movement")
      }

      @Test
      fun `404 when the hospital is not an agency`() {
        createOffenderBooking()

        dischargeToHospital(
          offenderNo,
          dischargeRequest(hospitalLocationCode = "ZZZ"),
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("No ZZZ agency found")
      }

      @Test
      fun `404 when the hospital agency type is not a hospital`() {
        createOffenderBooking()

        dischargeToHospital(
          offenderNo,
          dischargeRequest(hospitalLocationCode = "MDI"),
        )
          .isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("MOORLAND is not a hospital")
      }
    }

    @Nested
    @DisplayName("when discharge to hospital is a success")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SideEffects {

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should update booking`(offenderType: OffenderType) {
        createOffender(offenderType)

        dischargeToHospital(offenderNo, dischargeRequest())
          .isOk
          .expectBody()
          .jsonPath("offenderNo").isEqualTo(offenderNo)
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("agencyId").isEqualTo("OUT")
          .jsonPath("assignedLivingUnitId").doesNotExist()
          .jsonPath("inOutStatus").isEqualTo("OUT")
          .jsonPath("status").isEqualTo("INACTIVE OUT")
          .jsonPath("statusReason").isEqualTo("REL-HP")
          .jsonPath("lastMovementTypeCode").isEqualTo("REL")
          .jsonPath("lastMovementReasonCode").isEqualTo("HP")

        bookingId = findBookingId(offenderType)

        testDataContext.getOffenderBooking(bookingId!!)?.also {
          assertThat(it.isActive).isFalse()
          assertThat(it.location.id).isEqualTo("OUT")
          assertThat(it.statusReason).isEqualTo("REL-HP")
        }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should create an outbound movement`(offenderType: OffenderType) {
        createOffender(offenderType)

        // TODO SDIT-549 The comment text is ignored - why have it on the request object? Better to remove it.
        dischargeToHospital(offenderNo, dischargeRequest(commentText = "This is ignored")).isOk

        bookingId = findBookingId(offenderType)

        dataLoaderTransaction.get {
          testDataContext.getMovements(bookingId!!)
            .also {
              with(it.last()) {
                assertThat(fromAgency.id).isEqualTo("SYI")
                assertThat(toAgency.id).isEqualTo("HAZLWD")
                assertThat(movementDate).isEqualTo(LocalDate.now())
                assertThat(isActive).isTrue()
                assertThat(movementReason.code).isEqualTo(DISCHARGE_TO_PSY_HOSPITAL.code)
                assertThat(commentText).contains("Psychiatric Hospital Discharge to Hazelwood House")
              }
            }
        }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should make old movements inactive`(offenderType: OffenderType) {
        createOffender(offenderType)

        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        bookingId = findBookingId(offenderType)

        testDataContext.getMovements(bookingId!!)
          .filter { it.isActive }
          .also { assertThat(it).hasSize(1) }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should create a case note for the release`(offenderType: OffenderType) {
        createOffender(offenderType)

        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        bookingId = findBookingId(offenderType)

        testDataContext.getCaseNotes(offenderNo)
          .maxBy { it.caseNoteId }
          .also {
            assertThat(it.type).isEqualTo("PRISON")
            assertThat(it.subType).isEqualTo("RELEASE")
            // TODO SDIT-549 Shouldn't the case note be updated if the offender was released before moving to hospital? This feels like a bug.
            if (offenderType != RELEASED) {
              assertThat(it.text).isEqualTo("Transferred from SHREWSBURY for reason: Moved to psychiatric hospital Hazelwood House.")
            }
            assertThat(it.creationDateTime.toLocalDate()).isEqualTo(LocalDate.now())
            assertThat(it.agencyId).isEqualTo("SYI")
          }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should update the bed assignment history`(offenderType: OffenderType) {
        createOffender(offenderType)

        // check bed assignments not ended (yet) - not relevant if no booking or released
        if (listOf(BOOKING, TEMPORARY_ABSENCE).contains(offenderType)) {
          testDataContext.getBedAssignments(bookingId!!)
            .maxBy { it.assignmentDate }
            .also {
              assertThat(it.assignmentEndDate).isNull()
            }
        }

        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        bookingId = findBookingId(offenderType)

        testDataContext.getBedAssignments(bookingId!!)
          .maxBy { it.assignmentDate }
          .also {
            assertThat(it.assignmentEndDate).isEqualTo(LocalDate.now())
          }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should deactivate sentence adjustments`(offenderType: OffenderType) {
        createOffender(offenderType)

        bookingId?.also {
          testDataContext.getSentenceAdjustments(it)
            .forEach {
              assertThat(it.isActive).isTrue()
            }
        }

        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        bookingId = findBookingId(offenderType)

        testDataContext.getSentenceAdjustments(bookingId!!)
          .forEach {
            assertThat(it.isActive).isFalse()
          }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should deactivate key date adjustments`(offenderType: OffenderType) {
        createOffender(offenderType)

        bookingId?.also {
          testDataContext.getKeyDateAdjustments(it)
            .forEach {
              assertThat(it.isActive).isTrue()
            }
        }

        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        bookingId = findBookingId(offenderType)

        testDataContext.getKeyDateAdjustments(bookingId!!)
          .forEach {
            assertThat(it.isActive).isFalse()
          }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should end all offender pay statuses`(offenderType: OffenderType) {
        createOffender(offenderType)

        // check pay statuses not ended (yet) - not relevant if no booking or released
        if (listOf(BOOKING, TEMPORARY_ABSENCE).contains(offenderType)) {
          testDataContext.getOffenderPayStatus(bookingId!!)
            .forEach {
              assertThat(it.endDate).isNull()
            }
        }

        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        bookingId = findBookingId(offenderType)

        testDataContext.getOffenderPayStatus(bookingId!!)
          .forEach {
            assertThat(it.endDate).isBeforeOrEqualTo(LocalDate.now())
          }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should end all offender no pay periods`(offenderType: OffenderType) {
        createOffender(offenderType)

        // check pay periods not ended (yet) - not relevant if no booking or released
        if (listOf(BOOKING, TEMPORARY_ABSENCE).contains(offenderType)) {
          testDataContext.getOffenderNoPayPeriods(bookingId!!)
            .forEach {
              assertThat(it.endDate).isNull()
            }
        }

        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        bookingId = findBookingId(offenderType)

        testDataContext.getOffenderNoPayPeriods(bookingId!!)
          .forEach {
            assertThat(it.endDate).isBeforeOrEqualTo(LocalDate.now())
          }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should deactivate events`(offenderType: OffenderType) {
        createOffender(offenderType)

        // check activity allocations not ended (yet) - not relevant if no booking or released
        if (listOf(BOOKING, TEMPORARY_ABSENCE).contains(offenderType)) {
          testDataContext.getOffenderProgramProfiles(bookingId!!, "ALLOC")
            .forEach {
              assertThat(it.endDate).isNull()
            }
        }

        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        bookingId = findBookingId(offenderType)

        testDataContext.getOffenderProgramProfiles(bookingId!!, "ALLOC")
          .forEach {
            assertThat(it.endDate).isEqualTo(LocalDate.now())
          }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should NOT deactivate events if the prison's ACTIVITY feature switch is turned on`(offenderType: OffenderType) {
        createExternalService()
        createServiceAgencySwitch()
        createOffender(offenderType)

        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        bookingId = findBookingId(offenderType)

        testDataContext.getOffenderProgramProfiles(bookingId!!, "ALLOC")
          .forEach {
            assertThat(it.endDate).isNull()
          }
      }

      @ParameterizedTest
      @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceIntTest_dischargeToHospital#getOffenderTypes")
      fun `should allow missing optional request fields`(offenderType: OffenderType) {
        createOffender(offenderType)

        when (offenderType) {
          NO_BOOKING -> dischargeToHospital(offenderNo, dischargeRequestWithoutNullables(hospitalLocationCode = "HAZLWD"))
            .isBadRequest

          else -> {
            dischargeToHospital(offenderNo, dischargeRequestWithoutNullables(hospitalLocationCode = "HAZLWD")).isOk

            bookingId = findBookingId(offenderType)

            testDataContext.getOffenderBooking(bookingId!!)?.also {
              assertThat(it.isActive).isFalse()
              assertThat(it.location.id).isEqualTo("OUT")
              assertThat(it.statusReason).isEqualTo("REL-HP")
              assertThat(it.bookingEndDate.toLocalDate()).isEqualTo("${LocalDate.now()}")
            }
          }
        }
      }
    }

    @Nested
    @DisplayName("when offender does not have an active booking")
    inner class NoBookingSideEffects {
      @BeforeEach
      fun setUp() {
        createOffenderWithNoBooking()

        getOffender(offenderNo).isOk
          .expectBody()
          .jsonPath("bookingNo").doesNotExist()
      }

      @Test
      fun `should create an inbound movement for the new booking`() {
        dischargeToHospital(
          offenderNo,
          dischargeRequest(),
        ).isOk

        testDataContext.getOffenderBooking(offenderNo, active = false)
          .also { bookingId = it?.bookingId }

        dataLoaderTransaction.get {
          testDataContext.getMovements(bookingId!!)
            .also {
              with(it.first()) {
                assertThat(fromAgency.id).isEqualTo("ABDRCT")
                assertThat(toAgency.id).isEqualTo("SYI")
                assertThat(movementDate).isEqualTo(LocalDate.now())
                assertThat(isActive).isFalse()
                assertThat(movementReason.code).isEqualTo(AWAIT_REMOVAL_TO_PSY_HOSPITAL.code)
              }
            }
        }
      }
    }

    @Nested
    @DisplayName("when offender is already out on a temporary absence")
    inner class OutOnTemporaryAbsenceSideEffects {
      @BeforeEach
      fun setUp() {
        createOffenderOutAtCourt()
      }

      @Test
      fun `should not overwrite TAP movement`() {
        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        testDataContext.getMovements(bookingId!!)
          .also {
            with(it[it.size - 2]) {
              assertThat(fromAgency.id).isEqualTo("SYI")
              assertThat(toAgency.id).isEqualTo("COURT1")
              assertThat(movementDate).isEqualTo(LocalDate.now())
              assertThat(isActive).isFalse()
            }
          }
      }
    }

    @Nested
    @DisplayName("when offender has already been released")
    inner class AlreadyReleasedSideEffects {
      @BeforeEach
      fun setUp() {
        createOffenderAndRelease()
      }

      @Test
      fun `should overwrite REL movement`() {
        dischargeToHospital(offenderNo, dischargeRequest()).isOk

        dataLoaderTransaction.get {
          testDataContext.getMovements(bookingId!!)
            .also {
              // The 1st movement is the admission
              with(it[it.size - 2]) {
                assertThat(movementSequence).isEqualTo(1)
                assertThat(isActive).isFalse()
                assertThat(movementReason.code).isNotEqualTo("ADM")
              }
            }
            .also {
              // The 2nd and last movement is the discharge to hospital
              with(it.last()) {
                assertThat(movementSequence).isEqualTo(2)
                assertThat(isActive).isTrue()
                assertThat(movementReason.code).isEqualTo(DISCHARGE_TO_PSY_HOSPITAL.code)
                assertThat(commentText).isEqualTo("released prisoner today. Psychiatric Hospital Discharge to Hazelwood House")
              }
            }
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

    private fun createExternalService(serviceName: String = "ACTIVITY") {
      ExternalServiceBuilder(serviceName).save(testDataContext.dataLoader)
        .also {
          externalServiceName = it.serviceName
        }
    }

    private fun createServiceAgencySwitch(serviceName: String = "ACTIVITY", agencyId: String = "SYI") =
      ServiceAgencySwitchBuilder(serviceName, agencyId).save(testDataContext.dataLoader)

    private fun dischargeToHospital(
      offenderNo: String,
      body: String,
      username: String = "ITAG_USER",
      roles: List<String> = listOf("ROLE_RELEASE_PRISONER"),
    ): StatusAssertions =
      webTestClient.put()
        .uri("/api/offenders/{offenderNo}/discharge-to-hospital", offenderNo)
        .headers(setAuthorisation(username, roles))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(body.trimIndent())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()

    private fun dischargeRequest(
      hospitalLocationCode: String = "HAZLWD",
      dischargeTime: LocalDateTime = LocalDateTime.now(),
      supportingPrisonId: String = "SYI",
      fromLocationId: String = "ABDRCT",
      commentText: String = "Discharged Offender from $fromLocationId to $hospitalLocationCode",
    ): String =
      """
        {
           "hospitalLocationCode": "$hospitalLocationCode", 
           "dischargeTime": "${dischargeTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
           "commentText": "$commentText",
           "supportingPrisonId": "$supportingPrisonId" ,
           "fromLocationId": "$fromLocationId" 
        }
      """.trimIndent()

    private fun dischargeRequestWithoutNullables(hospitalLocationCode: String): String =
      """
        {
           "hospitalLocationCode": "$hospitalLocationCode"
        }
      """.trimIndent()
  }

  private fun createOffenderBooking() {
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

  private fun createOffenderWithNoBooking() {
    OffenderBuilder(bookingBuilders = arrayOf()).save(testDataContext).also {
      offenderNo = it.offenderNo
      bookingId = it.bookingId
    }
  }

  private fun createOffenderOutAtCourt() {
    createOffenderBooking().also { testDataContext.transferOutToCourt(offenderNo, "COURT1", expectedAgency = "SYI") }
  }

  private fun createOffenderAndRelease() {
    createOffenderBooking().also { testDataContext.release(offenderNo) }
  }
}
