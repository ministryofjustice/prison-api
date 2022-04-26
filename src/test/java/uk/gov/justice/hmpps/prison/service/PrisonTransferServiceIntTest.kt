package uk.gov.justice.hmpps.prison.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourseActivityRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import uk.gov.justice.hmpps.prison.service.transfer.PrisonTransferService
import uk.gov.justice.hmpps.prison.util.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.OffenderProgramProfileBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * KOTLIN
 */
@WithMockUser
class PrisonTransferServiceIntTest : ResourceTest() {
  @Autowired
  private lateinit var externalMovementRepository: ExternalMovementRepository

  @Autowired
  private lateinit var bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository

  @Autowired
  private lateinit var bookingRepository: BookingRepository

  @Autowired
  private lateinit var offenderProgramProfileRepository: OffenderProgramProfileRepository

  @Autowired
  private lateinit var courseActivityRepository: CourseActivityRepository

  @Autowired
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  @Autowired
  private lateinit var agencyLocationRepository: AgencyLocationRepository

  @Autowired
  private lateinit var transferService: PrisonTransferService

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
          prisonId = "LEI",
          bookingInTime = bookingInTime
        ).withIEPLevel("ENH").withInitialVoBalances(2, 8)
      ).save(
        webTestClient = webTestClient,
        jwtAuthenticationHelper = jwtAuthenticationHelper,
        bookingRepository = bookingRepository
      ).also {
        offenderNo = it.offenderNo
        OffenderProgramProfileBuilder(offenderBookingId = it.bookingId, prisonId = it.agencyId).save(
          courseActivityRepository = courseActivityRepository, bookingRepository = offenderBookingRepository, agencyLocationRepository = agencyLocationRepository, offenderProgramProfileRepository = offenderProgramProfileRepository
        )
      }
    }

    @Test
    internal fun `Activities and waitlist are cancelled`() {
      transferOutToCourt(offenderNo, "COURT1", true)

      // TODO assert activities are active at original prison

      transferService.transferViaCourt(
        offenderNo,
        RequestForCourtTransferIn.builder().agencyId("MDI").movementReasonCode("TRF").commentText("comments")
          .dateTime(LocalDateTime.of(2022, 10, 1, 5, 8, 0)).build()
      )

      // TODO assert activities and waitlist are cleared at original prison
    }
  }

  fun transferOutToCourt(offenderNo: String, toLocation: String, shouldReleaseBed: Boolean = false): LocalDateTime {
    val movementTime = LocalDateTime.now().minusHours(1)
    webTestClient.put()
      .uri("/api/offenders/{nomsId}/court-transfer-out", offenderNo)
      .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER_ALPHA")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
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
      )
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("inOutStatus").isEqualTo("OUT")
      .jsonPath("status").isEqualTo("ACTIVE OUT")
      .jsonPath("lastMovementTypeCode").isEqualTo("CRT")
      .jsonPath("lastMovementReasonCode").isEqualTo("19")
      .jsonPath("assignedLivingUnit.agencyId").isEqualTo("LEI")

    return movementTime
  }

  private fun getMovements(bookingId: Long) = externalMovementRepository.findAllByOffenderBooking_BookingId(bookingId)
  private fun getBedAssignments(bookingId: Long) =
    bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId)
}
