package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramEndReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile
import uk.gov.justice.hmpps.prison.service.transfer.ActivityTransferService
import uk.gov.justice.hmpps.prison.util.DataLoaderRepository
import uk.gov.justice.hmpps.prison.util.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.OffenderProgramProfileBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * KOTLIN
 */
@WithMockUser
class ActivityTransferServiceIntTest : ResourceTest() {
  @Autowired
  private lateinit var dataLoader: DataLoaderRepository

  @Autowired
  private lateinit var transferService: ActivityTransferService

  @Nested
  @DisplayName("Successful transfer from different prison via court")
  inner class Success {
    private lateinit var offenderNo: String
    private var bookingId: Long = 0
    private val bookingInTime = LocalDateTime.now().minusDays(1)

    @BeforeEach
    internal fun setUp() {
      OffenderBuilder().withBooking(
        OffenderBookingBuilder(
          prisonId = "LEI", bookingInTime = bookingInTime
        )
          .withIEPLevel("ENH")
          .withInitialVoBalances(2, 8)
          .withProgramProfiles(
            OffenderProgramProfileBuilder(),
            OffenderProgramProfileBuilder(
              courseActivityId = -3
            ),
            OffenderProgramProfileBuilder(
              programStatus = "WAIT",
              courseActivityId = -4
            ),
            // rejected waitlist decision should mean that this is ignored
            OffenderProgramProfileBuilder(
              programStatus = "WAIT",
              waitListDecisionCode = "REJ",
              courseActivityId = -5
            )
          )
      ).save(
        webTestClient = webTestClient,
        jwtAuthenticationHelper = jwtAuthenticationHelper,
        dataLoader = dataLoader
      ).also {
        offenderNo = it.offenderNo
        bookingId = it.bookingId
      }
    }

    @Test
    internal fun `Activities and waitlist are cancelled`() {
      val testEndDate = LocalDate.of(2022, 10, 1)
      transferOutToCourt(offenderNo, "COURT1", true)

      val offenderBooking = dataLoader.offenderBookingRepository.findByBookingId(bookingId).orElseThrow()
      val prison = dataLoader.agencyLocationRepository.findById("LEI").orElseThrow()

      assertThat(
        getActiveActivities(
          offenderBooking, prison, testEndDate
        )
      ).extracting(
        OffenderProgramProfile::getProgramStatus, { it.courseActivity.activityId }
      ).containsExactlyInAnyOrder(
        tuple("ALLOC", -1L), tuple("ALLOC", -3L)
      )

      assertThat(
        getActiveWaitList(
          offenderBooking, prison
        )
      ).extracting(
        OffenderProgramProfile::getProgramStatus,
        { it.courseActivity.activityId },
        OffenderProgramProfile::getWaitlistDecisionCode
      ).containsExactly(
        tuple("WAIT", -4L, null)
      )

      transferService.endActivitiesAndWaitlist(
        offenderBooking, prison, testEndDate, OffenderProgramEndReason.TRF.code
      )

      assertThat(
        getActiveActivities(
          offenderBooking, prison, testEndDate
        )
      ).isEmpty()

      assertThat(
        getActiveWaitList(
          offenderBooking, prison
        )
      ).isEmpty()
    }
  }

  fun getActiveActivities(
    offenderBooking: OffenderBooking,
    prison: AgencyLocation,
    testEndDate: LocalDate
  ): List<OffenderProgramProfile> =
    dataLoader.offenderProgramProfileRepository.findActiveActivitiesForBookingAtPrison(
      offenderBooking, prison, testEndDate
    )

  fun getActiveWaitList(
    offenderBooking: OffenderBooking,
    prison: AgencyLocation
  ): List<OffenderProgramProfile> =
    dataLoader.offenderProgramProfileRepository.findActiveWaitListActivitiesForBookingAtPrison(
      offenderBooking, prison
    )

  fun transferOutToCourt(offenderNo: String, toLocation: String, shouldReleaseBed: Boolean = false): LocalDateTime {
    val movementTime = LocalDateTime.now().minusHours(1)
    webTestClient.put().uri("/api/offenders/{nomsId}/court-transfer-out", offenderNo)
      .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER_ALPHA")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON).body(
        BodyInserters.fromValue(
          """
          {
            "transferReasonCode":"19",
            "commentText":"court appearance",
            "toLocation":"$toLocation",
            "movementTime": "${movementTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
            "shouldReleaseBed": $shouldReleaseBed
            
          }
          """.trimIndent()
        )
      ).exchange().expectStatus().isOk.expectBody().jsonPath("inOutStatus").isEqualTo("OUT").jsonPath("status")
      .isEqualTo("ACTIVE OUT").jsonPath("lastMovementTypeCode").isEqualTo("CRT").jsonPath("lastMovementReasonCode")
      .isEqualTo("19").jsonPath("assignedLivingUnit.agencyId").isEqualTo("LEI")

    return movementTime
  }
}
