package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.OffenderCalculatedKeyDates
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates
import uk.gov.justice.hmpps.prison.service.OffenderDatesServiceTest
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OffenderDatesResourceTest : ResourceTest() {
  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  companion object {
    private const val BOOKING_ID: Long = -2
  }

  @AfterEach
  fun tearDown() {
    // Restore db change as cannot rollback server transaction in client
    // Inserting into the database broke other tests, such as OffendersResourceTest
    // because they depend on seed data from R__4_19__OFFENDER_SENT_CALCULATIONS.sql
    jdbcTemplate.update("DELETE FROM OFFENDER_SENT_CALCULATIONS WHERE OFFENDER_BOOK_ID = -2 AND COMMENT_TEXT LIKE '%Calculate Release Dates%'")
  }

  @BeforeAll
  fun addAdditionalDataForTusedTests() {
    // Adding data to test the TUSED retrieval use case, prisoners used are A1234AF, A1234AG and A1180MA
    jdbcTemplate.update(
      """
            INSERT INTO OFFENDER_BOOKINGS (OFFENDER_BOOK_ID, BOOKING_BEGIN_DATE, BOOKING_NO, OFFENDER_ID, BOOKING_SEQ,
                                           DISCLOSURE_FLAG, IN_OUT_STATUS, ACTIVE_FLAG, YOUTH_ADULT_CODE, AGY_LOC_ID,
                                           ROOT_OFFENDER_ID, LIVING_UNIT_ID, BOOKING_STATUS)
            VALUES (-60, TO_DATE('2017-09-06', 'YYYY-MM-DD'), 'A00200', -1006, 2, 'N', 'IN', 'N', 'N', 'LEI', -1006, -4, 'O'),
                   (-61, TO_DATE('2017-09-06', 'YYYY-MM-DD'), 'A00201', -1007, 2, 'N', 'IN', 'N', 'N', 'LEI', -1007, -5, 'O');

            INSERT INTO OFFENDER_SENT_CALCULATIONS(OFFENDER_SENT_CALCULATION_ID, OFFENDER_BOOK_ID, CALCULATION_DATE,
                                                   CALC_REASON_CODE, CREATE_DATETIME, CREATE_USER_ID, TUSED_CALCULATED_DATE,
                                                   TUSED_OVERRIDED_DATE)
            VALUES (-23, -60, TO_DATE('2017-09-06', 'YYYY-MM-DD'), 'NEW' ,  SYSDATE, 'SA', TO_DATE('2018-09-06', 'YYYY-MM-DD'), null),
                   (-24, -61, TO_DATE('2017-09-06', 'YYYY-MM-DD'), 'NEW' ,  SYSDATE, 'SA', TO_DATE('2018-08-06', 'YYYY-MM-DD'), TO_DATE('2018-10-06', 'YYYY-MM-DD')),
                   (-2999, -36, TO_DATE('2017-09-06', 'YYYY-MM-DD'), 'NEW' ,  SYSDATE, 'SA', TO_DATE('2018-09-07', 'YYYY-MM-DD'), null);
            
      """.trimIndent(),
    )
  }

  @AfterAll
  fun removeAdditionalDataForTusedTests() {
    jdbcTemplate.update(
      """
            DELETE FROM OFFENDER_SENT_CALCULATIONS WHERE OFFENDER_SENT_CALCULATION_ID IN (-23, -24, -2999);
            DELETE FROM OFFENDER_BOOKINGS WHERE OFFENDER_BOOK_ID IN (-60, -61);
            
      """.trimIndent(),
    )
  }

  @Nested
  @DisplayName("POST /api/offender-dates/{bookingId}")
  inner class TestUpdateOffenderKeyDates {

    @Test
    fun testCanUpdateOffenderDates() {
      val body = RequestToUpdateOffenderDates.builder()
        .keyDates(
          OffenderDatesServiceTest.createOffenderKeyDates(
            LocalDate.of(2021, 11, 1), LocalDate.of(2021, 11, 2), LocalDate.of(2021, 11, 3),
            LocalDate.of(2021, 11, 4), LocalDate.of(2021, 11, 5), LocalDate.of(2021, 11, 6),
            LocalDate.of(2021, 11, 7), LocalDate.of(2021, 11, 8), LocalDate.of(2021, 11, 9),
            LocalDate.of(2021, 11, 10), LocalDate.of(2021, 11, 11), LocalDate.of(2021, 11, 12),
            LocalDate.of(2021, 11, 13), LocalDate.of(2021, 11, 14), LocalDate.of(2021, 11, 15), "11/00/00",
          ),
        )
        .submissionUser("ITAG_USER")
        .calculationUuid(UUID.randomUUID())
        .build()

      webTestClient.post()
        .uri("/api/offender-dates/{bookingId}", BOOKING_ID)
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .body(BodyInserters.fromValue(body))
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("bookingId").isEqualTo(-2)
        .jsonPath("sentenceExpiryDate").isEqualTo("2021-11-12")
        .jsonPath("conditionalReleaseDate").isEqualTo("2021-11-07")
        .jsonPath("licenceExpiryDate").isEqualTo("2021-11-10")
        .jsonPath("sentenceStartDate").isEqualTo("2017-05-22")
        .jsonPath("nonDtoReleaseDate").isEqualTo("2021-11-11")
        .jsonPath("nonDtoReleaseDateType").isEqualTo("PRRD")
        .jsonPath("confirmedReleaseDate").isEqualTo("2018-04-19")
        .jsonPath("releaseDate").isEqualTo("2018-04-19")

      webTestClient.get().uri("/api/offenders/{nomsId}/sentences", "A1234AB")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("bookingId").isEqualTo(-2)
        .jsonPath("offenderNo").isEqualTo("A1234AB")
        .jsonPath("sentenceDetail.sentenceExpiryDate").isEqualTo("2021-11-12")
        .jsonPath("sentenceDetail.conditionalReleaseDate").isEqualTo("2021-11-07")
        .jsonPath("sentenceDetail.licenceExpiryDate").isEqualTo("2021-11-10")
        .jsonPath("sentenceDetail.sentenceStartDate").isEqualTo("2017-05-22")
        .jsonPath("sentenceDetail.nonDtoReleaseDate").isEqualTo("2021-11-11")
        .jsonPath("sentenceDetail.nonDtoReleaseDateType").isEqualTo("PRRD")
        .jsonPath("sentenceDetail.confirmedReleaseDate").isEqualTo("2018-04-19")
        .jsonPath("sentenceDetail.releaseDate").isEqualTo("2018-04-19")

      webTestClient.get().uri("/api/offender-dates/{bookingId}", BOOKING_ID)
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("sentenceExpiryDate").isEqualTo("2021-11-12")
        .jsonPath("conditionalReleaseDate").isEqualTo("2021-11-07")
        .jsonPath("licenceExpiryDate").isEqualTo("2021-11-10")
        .jsonPath("homeDetentionCurfewEligibilityDate").isEqualTo("2021-11-01")
        .jsonPath("earlyTermDate").isEqualTo("2021-11-02")
        .jsonPath("midTermDate").isEqualTo("2021-11-03")
        .jsonPath("lateTermDate").isEqualTo("2021-11-04")
        .jsonPath("dtoPostRecallReleaseDate").isEqualTo("2021-11-05")
        .jsonPath("automaticReleaseDate").isEqualTo("2021-11-06")
        .jsonPath("paroleEligibilityDate").isEqualTo("2021-11-08")
        .jsonPath("nonParoleDate").isEqualTo("2021-11-09")
        .jsonPath("postRecallReleaseDate").isEqualTo("2021-11-11")
        .jsonPath("topupSupervisionExpiryDate").isEqualTo("2021-11-13")
        .jsonPath("earlyRemovalSchemeEligibilityDate").isEqualTo("2021-11-14")
        .jsonPath("effectiveSentenceEndDate").isEqualTo("2021-11-15")
        .jsonPath("sentenceLength").isEqualTo("11/00/00")
        .jsonPath("judiciallyImposedSentenceLength").isEqualTo("11/00/00")
    }

    @Test
    fun testCantUpdateOffenderDatesWithInvalidBookingId() {
      val body = RequestToUpdateOffenderDates.builder().build()

      webTestClient.post().uri("/api/offender-dates/{bookingId}", 0)
        .headers(setAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .body(BodyInserters.fromValue(body))
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource with id [0] not found.")
        .jsonPath("developerMessage").isEqualTo("Resource with id [0] not found.")
    }

    @Test
    fun testCantUpdateOffenderDatesWithInvalidStaff() {
      val body = RequestToUpdateOffenderDates.builder()
        .keyDates(OffenderDatesServiceTest.createOffenderKeyDates())
        .submissionUser("fake user")
        .calculationUuid(UUID.randomUUID())
        .build()

      webTestClient.post().uri("/api/offender-dates/{bookingId}", BOOKING_ID)
        .headers(setAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .body(BodyInserters.fromValue(body))
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource with id [fake user] not found.")
        .jsonPath("developerMessage").isEqualTo("Resource with id [fake user] not found.")
    }

    @Test
    fun testCantUpdateOffenderDatesWithIncorrectRole() {
      val body = mapOf("some key" to "some value")

      webTestClient.post().uri("/api/offender-dates/{bookingId}", BOOKING_ID)
        .headers(setAuthorisation(listOf("ROLE_BANANAS")))
        .body(BodyInserters.fromValue(body))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  @DisplayName("GET /api/offender-dates/calculations/{nomsId}")
  inner class TestGetKeyDatesForAnOffenderByNomisId {
    @Test
    fun testGetAllCalculationsForPrisoner() {
      webTestClient.get().uri("/api/offender-dates/calculations/{nomsId}", "A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(1)
        .jsonPath("[0].offenderNo").isEqualTo("A1234AA")
        .jsonPath("[0].lastName").isEqualTo("ANDERSON")
        .jsonPath("[0].agencyDescription").isEqualTo("LEEDS")
        .jsonPath("[0].commentText").isEqualTo("Some Comment Text")
        .jsonPath("[0].calculationReason").isEqualTo("New Sentence")
        .jsonPath("[0].calculatedByUserId").isEqualTo("PRISON_API_USER")
        .jsonPath("[0].calculatedByFirstName").isEqualTo("PRISON")
        .jsonPath("[0].calculatedByLastName").isEqualTo("USER")
    }
  }

  @Nested
  @DisplayName("GET /api/offender-dates/sentence-calculation/{offenderSentCalcId}")
  inner class TestGetKeyDatesForAnOffenderBySentenceCalId {
    @Test
    fun testGetOffenderKeyDatesForOffenderSentCalcId() {
      val nomisCalculations = webTestClient.get().uri("/api/offender-dates/sentence-calculation/{offenderSentCalcId}", "-16")
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isOk
        .expectBody(OffenderCalculatedKeyDates::class.java)
        .returnResult()
        .responseBody!!

      with(nomisCalculations) {
        assertThat(reasonCode).isEqualTo("NEW")
        assertThat(comment).isNull()
        assertThat(paroleEligibilityDate).isNull()
        assertThat(approvedParoleDate).isNull()
        assertThat(conditionalReleaseDate).isNull()
        assertThat(releaseOnTemporaryLicenceDate).isNull()
        assertThat(automaticReleaseDate).isNull()
        assertThat(lateTermDate).isNull()
        assertThat(postRecallReleaseDate).isNull()
        assertThat(tariffDate).isNull()
        assertThat(effectiveSentenceEndDate).isNull()
        assertThat(dtoPostRecallReleaseDate).isNull()
        assertThat(earlyRemovalSchemeEligibilityDate).isNull()
        assertThat(tariffExpiredRemovalSchemeEligibilityDate).isNull()
        assertThat(topupSupervisionExpiryDate).isNull()
        assertThat(nonParoleDate).isNull()
        assertThat(sentenceExpiryDate).isEqualTo(LocalDate.of(2022, 10, 20))
        assertThat(licenceExpiryDate).isEqualTo(LocalDate.of(2021, 9, 24))
        assertThat(midTermDate).isEqualTo(LocalDate.of(2021, 3, 25))
        assertThat(earlyTermDate).isEqualTo(LocalDate.of(2021, 2, 28))
        assertThat(homeDetentionCurfewApprovedDate).isEqualTo(LocalDate.of(2021, 1, 2))
        assertThat(calculatedAt).isEqualTo(LocalDateTime.of(2017, 9, 2, 0, 0))
        assertThat(homeDetentionCurfewEligibilityDate).isEqualTo(LocalDate.of(2020, 12, 30))
        assertThat(isHomeDetentionCurfewEligibilityDateOverridden).describedAs("isHomeDetentionCurfewEligibilityDateOverridden").isTrue()
        assertThat(isConditionalReleaseDateOverridden).describedAs("isConditionalReleaseDateOverridden").isFalse()
        assertThat(isLicenceExpiryDateOverridden).describedAs("isLicenceExpiryDateOverridden").isTrue()
        assertThat(isSentenceExpiryDateOverridden).describedAs("isSentenceExpiryDateOverridden").isTrue()
        assertThat(isNonParoleDateOverridden).describedAs("isNonParoleDateOverridden").isFalse()
        assertThat(isAutomaticReleaseDateOverridden).describedAs("isAutomaticReleaseDateOverridden").isFalse()
        assertThat(isTopupSupervisionExpiryDateOverridden).describedAs("isTopupSupervisionExpiryDateOverridden").isFalse()
        assertThat(isParoleEligibilityDateOverridden).describedAs("isParoleEligibilityDateOverridden").isFalse()
        assertThat(calculatedByUserId).describedAs("calculatedByUserId").isEqualTo("PRISON_API_USER")
        assertThat(calculatedByFirstName).describedAs("calculatedByFirstName").isEqualTo("PRISON")
        assertThat(calculatedByLastName).describedAs("calculatedByLastName").isEqualTo("USER")
      }
    }
  }

  @Nested
  @DisplayName("GET /api/offender-dates/latest-tused/{nomsId}")
  inner class TestGetLatestTusecByNomisId {
    @Test
    fun testGetLatestTusedForPrisoner() {
      webTestClient.get().uri("/api/offender-dates/latest-tused/{nomsId}", "A1234AF")
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("offenderNo").isEqualTo("A1234AF")
        .jsonPath("latestTused").isEqualTo("2018-09-06")
    }

    @Test
    fun testGetLatestTusedForPrisonerOnLatestBooking() {
      webTestClient.get().uri("/api/offender-dates/latest-tused/{nomsId}", "A1180MA")
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("offenderNo").isEqualTo("A1180MA")
        .jsonPath("latestTused").isEqualTo("2018-09-07")
    }

    @Test
    fun testGetLatestTusedForPrisonerWithOverridenTused() {
      webTestClient.get().uri("/api/offender-dates/latest-tused/{nomsId}", "A1234AG")
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("offenderNo").isEqualTo("A1234AG")
        .jsonPath("latestTused").isEqualTo("2018-08-06")
        .jsonPath("latestOverrideTused").isEqualTo("2018-10-06")
    }

    @Test
    fun testGetLatestTusedForPrisonerThatDoesntExist() {
      webTestClient.get().uri("/api/offender-dates/latest-tused/{nomsId}", "doesntExist")
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun testGetLatestTusedForPrisonerWithNoTused() {
      webTestClient.get().uri("/api/offender-dates/latest-tused/{nomsId}", "A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
