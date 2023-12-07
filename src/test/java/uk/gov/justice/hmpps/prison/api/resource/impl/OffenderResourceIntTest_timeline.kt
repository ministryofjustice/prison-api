@file:Suppress("ktlint:filename")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import uk.gov.justice.hmpps.prison.util.builders.dsl.NomisDataBuilder
import uk.gov.justice.hmpps.prison.util.builders.dsl.OffenderBookingId
import uk.gov.justice.hmpps.prison.util.builders.dsl.OffenderId
import java.time.LocalDateTime

private const val REMAND_REASON = "N"
private const val CONDITIONAL_RELEASE_REASON = "CR"
private const val HOSPITAL_RELEASE_REASON = "HP"
private const val DAY_RELEASE_FUNERAL_REASON = "C3"
private const val DAY_RELEASE_DENTIST_REASON = "C6"
private const val RECALL_REASON = "24"
private const val COURT_APPEARANCE_REASON = "CRT"

@WithMockUser
class OffenderResourceTimelineIntTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/prison-timeline")
  inner class GetPrisonTimeline {
    @Nested
    inner class Security {
      private lateinit var prisoner: OffenderId

      @BeforeEach
      fun setUp() {
        if (!::prisoner.isInitialized) {
          NomisDataBuilder(testDataContext).build {
            prisoner = offender(lastName = "DUBOIS") {
              booking(
                prisonId = "MDI",
                bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
                movementReasonCode = REMAND_REASON,
              ) {}.bookingId
            }
          }
        }
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access unauthorised with no auth token`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class Validation {
      private lateinit var prisoner: OffenderId

      @BeforeEach
      fun setUp() {
        if (!::prisoner.isInitialized) {
          NomisDataBuilder(testDataContext).build {
            prisoner = offender(lastName = "DUBOIS") {}
          }
        }
      }

      @Test
      fun `404 when offender not found`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", "Z1234ZZ")
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `404 when offender has no booking`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isNotFound
      }
    }

    @Nested
    inner class SingleBookingWithSingleMovements {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeEach
      fun setUp() {
        if (!::prisoner.isInitialized) {
          NomisDataBuilder(testDataContext).build {
            prisoner = offender(lastName = "DUBOIS") {
              booking = booking(
                prisonId = "MDI",
                bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
                movementReasonCode = REMAND_REASON,
              ) {}
            }
          }
        }
      }

      @Test
      fun `will have a single period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod").isArray
          .jsonPath("prisonPeriod.size()").isEqualTo(1)
      }

      @Test
      fun `will have a single movement in that period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates").isArray
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(1)
      }

      @Test
      fun `will have a single prison in that period prison set`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].prisons").isArray
          .jsonPath("prisonPeriod[0].prisons.size()").isEqualTo(1)
      }

      @Test
      fun `prison period contains booking details`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].bookNumber").isNotEmpty
          .jsonPath("prisonPeriod[0].bookingId").isEqualTo(booking.bookingId)
          .jsonPath("prisonPeriod[0].entryDate").isEqualTo("2023-07-19T10:00:00")
      }

      @Test
      fun `movement contains details for entry`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates[0]").isNotEmpty
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
      }
    }

    @Nested
    inner class TwoBookingsAfterRelease {
      private lateinit var prisoner: OffenderId
      private lateinit var firstBooking: OffenderBookingId
      private lateinit var secondBooking: OffenderBookingId

      @BeforeEach
      fun setUp() {
        if (!::prisoner.isInitialized) {
          NomisDataBuilder(testDataContext).build {
            prisoner = offender(lastName = "DUBOIS") {
              firstBooking = booking(
                prisonId = "MDI",
                bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
                movementReasonCode = REMAND_REASON,
              ) {
                release(
                  releaseTime = LocalDateTime.parse("2023-07-20T10:00:00"),
                  movementReasonCode = CONDITIONAL_RELEASE_REASON,
                )
              }
              secondBooking = booking(
                prisonId = "LEI",
                bookingInTime = LocalDateTime.parse("2023-07-21T10:00:00"),
                movementReasonCode = REMAND_REASON,
              ) {}
            }
          }
        }
      }

      @Test
      fun `will have a two period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod.size()").isEqualTo(2)
      }

      @Test
      fun `will have a one movements in each period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[1].movementDates.size()").isEqualTo(1)
      }

      @Test
      fun `will have a single prison in each period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].prisons.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[0].prisons[0]").isEqualTo("MDI")
          .jsonPath("prisonPeriod[1].prisons.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[1].prisons[0]").isEqualTo("LEI")
      }

      @Test
      fun `first booking prison period contains entry and exit dates`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].bookingId").isEqualTo(firstBooking.bookingId)
          .jsonPath("prisonPeriod[0].entryDate").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].releaseDate").isEqualTo("2023-07-20T10:00:00")
      }

      @Test
      fun `current booking prison period only contains entry date`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[1].bookingId").isEqualTo(secondBooking.bookingId)
          .jsonPath("prisonPeriod[1].entryDate").isEqualTo("2023-07-21T10:00:00")
          .jsonPath("prisonPeriod[1].releaseDate").doesNotExist()
      }

      @Test
      fun `movement from first booking period contains details for entry and exit`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates[0]").isNotEmpty
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").isEqualTo("2023-07-20T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").isEqualTo("REL")
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison").isEqualTo("Conditional Release (CJA91) -SH Term>1YR")
      }

      @Test
      fun `movement from second booking period only contains details for entry`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[1].movementDates[0]").isNotEmpty
          .jsonPath("prisonPeriod[1].movementDates[0].dateInToPrison").isEqualTo("2023-07-21T10:00:00")
          .jsonPath("prisonPeriod[1].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[1].movementDates[0].admittedIntoPrisonId").isEqualTo("LEI")
          .jsonPath("prisonPeriod[1].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[1].movementDates[0].dateOutOfPrison").doesNotExist()
          .jsonPath("prisonPeriod[1].movementDates[0].outwardType").doesNotExist()
          .jsonPath("prisonPeriod[1].movementDates[0].releaseFromPrisonId").doesNotExist()
          .jsonPath("prisonPeriod[1].movementDates[0].reasonOutOfPrison").doesNotExist()
      }
    }

    @Nested
    inner class ReleaseRecallAndFinalRelease {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeEach
      fun setUp() {
        if (!::prisoner.isInitialized) {
          NomisDataBuilder(testDataContext).build {
            prisoner = offender(lastName = "DUBOIS") {
              booking = booking(
                prisonId = "MDI",
                bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
                movementReasonCode = REMAND_REASON,
              ) {
                release(
                  releaseTime = LocalDateTime.parse("2023-07-20T10:00:00"),
                  movementReasonCode = CONDITIONAL_RELEASE_REASON,
                )
                recall(
                  prisonId = "LEI",
                  recallTime = LocalDateTime.parse("2023-07-21T10:00:00"),
                  movementReasonCode = RECALL_REASON,
                )
                release(
                  releaseTime = LocalDateTime.parse("2023-07-22T10:00:00"),
                  movementReasonCode = HOSPITAL_RELEASE_REASON,
                )
              }
            }
          }
        }
      }

      @Test
      fun `will have a single period covering the release and recall`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod.size()").isEqualTo(1)
      }

      @Test
      fun `prison period contains entry and exit dates`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].bookingId").isEqualTo(booking.bookingId)
          .jsonPath("prisonPeriod[0].entryDate").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].releaseDate").isEqualTo("2023-07-22T10:00:00")
          .jsonPath("prisonPeriod[0].prisons[0]").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].prisons[1]").isEqualTo("LEI")
      }

      @Test
      fun `will have two movements due to the release and recall`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.length()").isEqualTo(2)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").isEqualTo("2023-07-20T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").isEqualTo("REL")
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison").isEqualTo("Conditional Release (CJA91) -SH Term>1YR")
          .jsonPath("prisonPeriod[0].movementDates[1].dateInToPrison").isEqualTo("2023-07-21T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[1].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[1].admittedIntoPrisonId").isEqualTo("LEI")
          .jsonPath("prisonPeriod[0].movementDates[1].reasonInToPrison").isEqualTo("Recall From Intermittent Custody")
          .jsonPath("prisonPeriod[0].movementDates[1].dateOutOfPrison").isEqualTo("2023-07-22T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[1].outwardType").isEqualTo("REL")
          .jsonPath("prisonPeriod[0].movementDates[1].releaseFromPrisonId").isEqualTo("LEI")
          .jsonPath("prisonPeriod[0].movementDates[1].reasonOutOfPrison").isEqualTo("Final Discharge To Hospital-Psychiatric")
      }
    }

    @Nested
    inner class CurrentlyInPrisonWithTAPs {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeEach
      fun setUp() {
        if (!::prisoner.isInitialized) {
          NomisDataBuilder(testDataContext).build {
            prisoner = offender(lastName = "DUBOIS") {
              booking = booking(
                prisonId = "MDI",
                bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
                movementReasonCode = REMAND_REASON,
              ) {
                temporaryAbsenceRelease(
                  releaseTime = LocalDateTime.parse("2023-07-20T10:00:00"),
                  movementReasonCode = DAY_RELEASE_FUNERAL_REASON,
                )
                temporaryAbsenceReturn(
                  prisonId = "MDI",
                  returnTime = LocalDateTime.parse("2023-07-20T22:00:00"),
                  movementReasonCode = DAY_RELEASE_FUNERAL_REASON,
                )
                temporaryAbsenceRelease(
                  releaseTime = LocalDateTime.parse("2023-07-21T23:00:00"),
                  movementReasonCode = DAY_RELEASE_DENTIST_REASON,
                )
                temporaryAbsenceReturn(
                  prisonId = "MDI",
                  returnTime = LocalDateTime.parse("2023-07-22T10:00:00"),
                  movementReasonCode = DAY_RELEASE_DENTIST_REASON,
                )
              }
            }
          }
        }
      }

      @Test
      fun `will have a single period covering the various temporary absences`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod.size()").isEqualTo(1)
      }

      @Test
      fun `prison period contains only entry dates`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].bookingId").isEqualTo(booking.bookingId)
          .jsonPath("prisonPeriod[0].entryDate").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].releaseDate").doesNotExist()
          .jsonPath("prisonPeriod[0].prisons[0]").isEqualTo("MDI")
      }

      @Test
      fun `will have three movements due to the two TAP movements`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.length()").isEqualTo(3)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").isEqualTo("2023-07-20T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").isEqualTo("TAP")
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison").isEqualTo("Funerals And Deaths")
          .jsonPath("prisonPeriod[0].movementDates[1].dateInToPrison").isEqualTo("2023-07-20T22:00:00")
          .jsonPath("prisonPeriod[0].movementDates[1].inwardType").isEqualTo("TAP")
          .jsonPath("prisonPeriod[0].movementDates[1].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[1].reasonInToPrison").isEqualTo("Funerals And Deaths")
          .jsonPath("prisonPeriod[0].movementDates[1].dateOutOfPrison").isEqualTo("2023-07-21T23:00:00")
          .jsonPath("prisonPeriod[0].movementDates[1].outwardType").isEqualTo("TAP")
          .jsonPath("prisonPeriod[0].movementDates[1].releaseFromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[1].reasonOutOfPrison").isEqualTo("Medical/Dental Inpatient Appointment")
          .jsonPath("prisonPeriod[0].movementDates[2].dateInToPrison").isEqualTo("2023-07-22T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[2].inwardType").isEqualTo("TAP")
          .jsonPath("prisonPeriod[0].movementDates[2].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[2].reasonInToPrison").isEqualTo("Medical/Dental Inpatient Appointment")
          .jsonPath("prisonPeriod[0].movementDates[2].dateOutOfPrison").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[2].outwardType").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[2].releaseFromPrisonId").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[2].reasonOutOfPrison").doesNotExist()
      }
    }

    @Nested
    inner class CurrentlyInPrisonWithCourtMovements {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeEach
      fun setUp() {
        if (!::prisoner.isInitialized) {
          NomisDataBuilder(testDataContext).build {
            prisoner = offender(lastName = "DUBOIS") {
              booking = booking(
                prisonId = "MDI",
                bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
                movementReasonCode = REMAND_REASON,
              ) {
                sendToCourt(
                  releaseTime = LocalDateTime.parse("2023-07-20T10:00:00"),
                  movementReasonCode = COURT_APPEARANCE_REASON,
                )
                returnFromCourt(
                  prisonId = "MDI",
                  returnTime = LocalDateTime.parse("2023-07-20T22:00:00"),
                  movementReasonCode = COURT_APPEARANCE_REASON,
                )
                sendToCourt(
                  releaseTime = LocalDateTime.parse("2023-07-21T23:00:00"),
                  movementReasonCode = COURT_APPEARANCE_REASON,
                )
                returnFromCourt(
                  prisonId = "MDI",
                  returnTime = LocalDateTime.parse("2023-07-22T10:00:00"),
                  movementReasonCode = COURT_APPEARANCE_REASON,
                )
              }
            }
          }
        }
      }

      @Test
      fun `will have a single period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod.size()").isEqualTo(1)
      }

      @Test
      fun `prison period contains only entry dates`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].bookingId").isEqualTo(booking.bookingId)
          .jsonPath("prisonPeriod[0].entryDate").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].releaseDate").doesNotExist()
          .jsonPath("prisonPeriod[0].prisons[0]").isEqualTo("MDI")
      }

      @Test
      fun `will have one movement since court movements are not significant`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.length()").isEqualTo(1)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison").doesNotExist()
      }
    }
  }
}
