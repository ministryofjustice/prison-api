package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.builders.transferOutToCourt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@WithMockUser
class OffenderResourceIntTest_releasePrisoner : ResourceTest() {

  @Nested
  @DisplayName("POST /offenders/{offenderNo}/release")
  inner class ReleasePrisoner {

    @Nested
    @DisplayName("when release is rejected")
    inner class Failure {
      @Test
      internal fun `404 when offender not found`() {
        val offenderNo = "Z9999ZZ"

        // Given offender does not exist
        getPrisoner(offenderNo)
          .expectStatus().isNotFound

        // then the release is rejected
        releasePrisoner(offenderNo)
          .expectStatus().isNotFound
      }

      @Test
      internal fun `400 when offender is inactive`() {
        val offenderNo = createInactiveBooking()

        // then the release is rejected
        // need system role since we need pretend we can access OUT prisoners
        releasePrisoner(offenderNo, roles = listOf("ROLE_RELEASE_PRISONER", "ROLE_SYSTEM_USER"))
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Prisoner is not currently active")
      }

      @Test
      internal fun `400 when offender is out at court`() {
        val offenderNo =
          createActiveBooking(prisonId = "LEI").also { testDataContext.transferOutToCourt(it, toLocation = "COURT1") }

        // then the release is rejected
        releasePrisoner(offenderNo)
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Prisoner is not currently IN")
      }

      @Test
      internal fun `404 when trying release to location that doesn't exist`() {
        val offenderNo = createActiveBooking()

        // then the release is rejected
        releasePrisoner(offenderNo, toLocationCode = "XX")
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("No XX agency found")
      }

      @Test
      internal fun `404 when supplying a movement reason that is not a release`() {
        val offenderNo = createActiveBooking()

        // then the release is rejected
        releasePrisoner(offenderNo, movementReasonCode = "XX")
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("No movement type found for MovementTypeAndReason.Pk(type=REL, reasonCode=XX)")
      }

      @Test
      internal fun `400 when trying to release a prisoner in the future`() {
        val offenderNo = createActiveBooking()

        releasePrisoner(
          offenderNo,
          releaseTime = LocalDateTime.now().plusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Transfer cannot be done in the future")
      }

      @Test
      internal fun `400 when trying to release a prisoner before the previous movement`() {
        val offenderNo = createActiveBooking(bookingInTime = LocalDateTime.now().minusMinutes(1))

        releasePrisoner(
          offenderNo,
          releaseTime = LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be before the previous active movement")
      }
    }

    @Nested
    @DisplayName("when release is successful")
    inner class SideEffects {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        offenderNo = createActiveBooking()
      }

      @Test
      internal fun `will set prisoner release from Moorland and inactive`() {
        releasePrisoner(offenderNo, movementReasonCode = "CR")

        // then we have an active booking
        getPrisoner(offenderNo)
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("inOutStatus").isEqualTo("OUT")
          .jsonPath("status").isEqualTo("INACTIVE OUT")
          .jsonPath("lastMovementTypeCode").isEqualTo("REL")
          .jsonPath("lastMovementReasonCode").isEqualTo("CR")
          .jsonPath("statusReason").isEqualTo("REL-CR")
          .jsonPath("agencyId").isEqualTo("OUT")
          .jsonPath("locationDescription").isEqualTo("Outside - released from MOORLAND")
          .jsonPath("latestLocationId").isEqualTo("MDI")
      }
    }
  }

  fun createActiveBooking(
    prisonId: String = "MDI",
    bookingInTime: LocalDateTime = LocalDateTime.now().minusDays(1)
  ): String = OffenderBuilder().withBooking(
    OffenderBookingBuilder(
      prisonId = prisonId,
      bookingInTime = bookingInTime,
    )
  ).save(testDataContext).offenderNo

  fun createInactiveBooking(iepLevel: String = "ENH"): String = OffenderBuilder().withBooking(
    OffenderBookingBuilder(
      prisonId = "MDI",
      released = true
    ).withIEPLevel(iepLevel)
  ).save(testDataContext).offenderNo

  private fun getPrisoner(offenderNo: String) = webTestClient.get()
    .uri("/api/offenders/{offenderNo}", offenderNo)
    .headers(
      setAuthorisation(
        listOf("ROLE_SYSTEM_USER")
      )
    )
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .exchange()

  private fun releasePrisoner(
    offenderNo: String,
    movementReasonCode: String = "CR",
    toLocationCode: String = "OUT",
    releaseTime: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    roles: List<String> = listOf("ROLE_RELEASE_PRISONER")
  ) = webTestClient.put()
    .uri("/api/offenders/{offenderNo}/release", offenderNo)
    .headers(setAuthorisation(roles))
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .bodyValue(
      """
            {
               "movementReasonCode": "$movementReasonCode", 
               "commentText": "Gone..", 
               "toLocationCode": "$toLocationCode",
                "releaseTime": "$releaseTime"
            }
      """.trimIndent()
    )
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
}
