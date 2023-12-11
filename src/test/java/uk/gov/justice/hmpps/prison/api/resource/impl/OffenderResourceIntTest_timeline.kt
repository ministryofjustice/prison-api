@file:Suppress("ktlint:filename")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import uk.gov.justice.hmpps.prison.dsl.NomisDataBuilder
import uk.gov.justice.hmpps.prison.dsl.OffenderBookingId
import uk.gov.justice.hmpps.prison.dsl.OffenderId
import java.time.LocalDateTime

private const val REMAND_REASON = "N"
private const val CONDITIONAL_RELEASE_REASON = "CR"
private const val HOSPITAL_RELEASE_REASON = "HP"
private const val DAY_RELEASE_FUNERAL_REASON = "C3"
private const val DAY_RELEASE_DENTIST_REASON = "C6"
private const val RECALL_REASON = "24"
private const val COURT_APPEARANCE_REASON = "CRT"
private const val TRANSFER_REASON = "NOTR"

@WithMockUser
class OffenderResourceTimelineIntTest : ResourceTest() {
  @Autowired
  private lateinit var builder: NomisDataBuilder

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/prison-timeline")
  inner class GetPrisonTimeline {
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Security {
      private lateinit var prisoner: OffenderId

      @BeforeAll
      fun setUp() {
        builder.build {
          prisoner = offender(lastName = "DUBOIS") {
            booking(
              prisonId = "MDI",
              bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
              movementReasonCode = REMAND_REASON,
            ) {}
          }
        }
      }

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
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
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Validation {
      private lateinit var prisoner: OffenderId

      @BeforeAll
      fun setUp() {
        builder.build {
          prisoner = offender(lastName = "DUBOIS") {}
        }
      }

      @AfterAll
      fun deletePrisoner() {
        // for now we can't delete a prisoner with no bookings
        // we are unlikely to have an issues of leaving this dangling person since
        // they are not associated with in prison
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
    @DisplayName("Person currently in prison with no movements out")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SingleBookingWithSingleMovement {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeAll
      fun setUp() {
        builder.build {
          prisoner = offender(lastName = "DUBOIS") {
            booking = booking(
              prisonId = "MDI",
              bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
              movementReasonCode = REMAND_REASON,
            ) {}
          }
        }
      }

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
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
      fun `will have a single movement in that period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(1)
      }

      @Test
      fun `will have no transfers during that period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers.size()").isEqualTo(0)
      }

      @Test
      fun `will have a single prison in that period prison set`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].prisons.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[0].prisons[0]").isEqualTo("MDI")
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
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
      }
    }

    @Nested
    @DisplayName("Person currently in prison after a previous inactive booking")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class TwoBookingsAfterRelease {
      private lateinit var prisoner: OffenderId
      private lateinit var firstBooking: OffenderBookingId
      private lateinit var secondBooking: OffenderBookingId

      @BeforeAll
      fun createPrisoner() {
        builder.build {
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

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
      }

      @Test
      fun `will have a two periods`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod.size()").isEqualTo(2)
      }

      @Test
      fun `will have one movement in each of the two periods`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[1].movementDates.size()").isEqualTo(1)
      }

      @Test
      fun `will have no transfers in either period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers.size()").isEqualTo(0)
          .jsonPath("prisonPeriod[1].transfers.size()").isEqualTo(0)
      }

      @Test
      fun `will have a single prison in each of the two periods`() {
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
      fun `movement from first inactive booking period contains details for entry and exit`() {
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
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison")
          .isEqualTo("Conditional Release (CJA91) -SH Term>1YR")
      }

      @Test
      fun `movement from second current booking period only contains details for entry`() {
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
    @DisplayName("Person has been released from prison after previously being recalled")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ReleaseRecallAndFinalRelease {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeAll
      fun createPrisoner() {
        builder.build {
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

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
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
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(2)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").isEqualTo("2023-07-20T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").isEqualTo("REL")
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison")
          .isEqualTo("Conditional Release (CJA91) -SH Term>1YR")
          .jsonPath("prisonPeriod[0].movementDates[1].dateInToPrison").isEqualTo("2023-07-21T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[1].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[1].admittedIntoPrisonId").isEqualTo("LEI")
          .jsonPath("prisonPeriod[0].movementDates[1].reasonInToPrison").isEqualTo("Recall From Intermittent Custody")
          .jsonPath("prisonPeriod[0].movementDates[1].dateOutOfPrison").isEqualTo("2023-07-22T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[1].outwardType").isEqualTo("REL")
          .jsonPath("prisonPeriod[0].movementDates[1].releaseFromPrisonId").isEqualTo("LEI")
          .jsonPath("prisonPeriod[0].movementDates[1].reasonOutOfPrison")
          .isEqualTo("Final Discharge To Hospital-Psychiatric")
      }

      @Test
      fun `will have no transfers in this period even though they have been in different prisons`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers.size()").isEqualTo(0)
      }
    }

    @Nested
    @DisplayName("Person is currently in prison but has a number of temporary absences")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CurrentlyInPrisonWithTAPs {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeAll
      fun createPrisoner() {
        builder.build {
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

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
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
      fun `will have three movements due to the two TAP movements and the initial entry`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(3)
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
          .jsonPath("prisonPeriod[0].movementDates[1].reasonOutOfPrison")
          .isEqualTo("Medical/Dental Inpatient Appointment")
          .jsonPath("prisonPeriod[0].movementDates[2].dateInToPrison").isEqualTo("2023-07-22T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[2].inwardType").isEqualTo("TAP")
          .jsonPath("prisonPeriod[0].movementDates[2].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[2].reasonInToPrison")
          .isEqualTo("Medical/Dental Inpatient Appointment")
          .jsonPath("prisonPeriod[0].movementDates[2].dateOutOfPrison").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[2].outwardType").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[2].releaseFromPrisonId").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[2].reasonOutOfPrison").doesNotExist()
      }

      @Test
      fun `will have no transfers in this period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers.size()").isEqualTo(0)
      }
    }

    @Nested
    @DisplayName("Person is currently in prison but returned to a different prison after a temporary absence")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CurrentlyInPrisonWithTAPReturningToDifferentPrison {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeAll
      fun createPrisoner() {
        builder.build {
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
                prisonId = "LEI",
                returnTime = LocalDateTime.parse("2023-07-20T22:00:00"),
                movementReasonCode = DAY_RELEASE_FUNERAL_REASON,
              )
            }
          }
        }
      }

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
      }

      @Test
      fun `will have a single period covering the temporary absences`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod.size()").isEqualTo(1)
      }

      @Test
      fun `prison period contains only entry dates but both prisons`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].bookingId").isEqualTo(booking.bookingId)
          .jsonPath("prisonPeriod[0].entryDate").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].releaseDate").doesNotExist()
          .jsonPath("prisonPeriod[0].prisons.size()").isEqualTo("2")
          .jsonPath("prisonPeriod[0].prisons[0]").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].prisons[1]").isEqualTo("LEI")
      }

      @Test
      fun `will have two movements due to the single TAP movement which is classed as an admission into a different prison`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(2)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").isEqualTo("2023-07-20T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").isEqualTo("TAP")
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison").isEqualTo("Funerals And Deaths")
          .jsonPath("prisonPeriod[0].movementDates[1].dateInToPrison").isEqualTo("2023-07-20T22:00:00")
          .jsonPath("prisonPeriod[0].movementDates[1].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[1].admittedIntoPrisonId").isEqualTo("LEI")
          .jsonPath("prisonPeriod[0].movementDates[1].reasonInToPrison").isEqualTo("Transfer Via Temporary Release")
          .jsonPath("prisonPeriod[0].movementDates[1].dateOutOfPrison").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[1].outwardType").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[1].releaseFromPrisonId").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[1].reasonOutOfPrison").doesNotExist()
      }

      @Test
      fun `will have also have a transfer in this period since the readmission from TAP is like a transfer`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers.size()").isEqualTo(1)
      }

      @Test
      fun `will have details of the transfer prison and dates`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers[0].dateOutOfPrison").isEqualTo("2023-07-20T10:00:00")
          .jsonPath("prisonPeriod[0].transfers[0].dateInToPrison").isEqualTo("2023-07-20T22:00:00")
          .jsonPath("prisonPeriod[0].transfers[0].transferReason").isEqualTo("Transfer Via Temporary Release")
          .jsonPath("prisonPeriod[0].transfers[0].fromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].transfers[0].toPrisonId").isEqualTo("LEI")
      }
    }

    @Nested
    @DisplayName("Person is currently in prison but has a number of court appearances")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CurrentlyInPrisonWithCourtMovements {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeAll
      fun createPrisoner() {
        builder.build {
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

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
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
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison").doesNotExist()
      }

      @Test
      fun `will have no have a transfers in this period`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers.size()").isEqualTo(0)
      }
    }

    @Nested
    @DisplayName("Person is currently in prison but returned to a different prison after a court appearance")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CurrentlyInPrisonWithCourtMovementReturningToDifferentPrison {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeAll
      fun createPrisoner() {
        builder.build {
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
                prisonId = "LEI",
                returnTime = LocalDateTime.parse("2023-07-20T22:00:00"),
                movementReasonCode = COURT_APPEARANCE_REASON,
              )
            }
          }
        }
      }

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
      }

      @Test
      fun `will have a single period covering the various court movements`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod.size()").isEqualTo(1)
      }

      @Test
      fun `prison period contains only entry dates and both prisons`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].bookingId").isEqualTo(booking.bookingId)
          .jsonPath("prisonPeriod[0].entryDate").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].releaseDate").doesNotExist()
          .jsonPath("prisonPeriod[0].prisons[0]").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].prisons[1]").isEqualTo("LEI")
      }

      @Test
      fun `will have one movements due to the court movement not being significant`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison").doesNotExist()
      }

      @Test
      fun `will have also have a transfer in this period since the readmission from court is like a transfer`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers.size()").isEqualTo(1)
      }

      @Test
      fun `will have details of the transfer prison and dates`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers[0].dateOutOfPrison").isEqualTo("2023-07-20T10:00:00")
          .jsonPath("prisonPeriod[0].transfers[0].dateInToPrison").isEqualTo("2023-07-20T22:00:00")
          .jsonPath("prisonPeriod[0].transfers[0].transferReason").isEqualTo("Transfer Via Court")
          .jsonPath("prisonPeriod[0].transfers[0].fromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].transfers[0].toPrisonId").isEqualTo("LEI")
      }
    }

    @Nested
    @DisplayName("Person has been released from prison after previously being transferred between prisons")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ReleasedFromPrisonAfterMultipleTransfers {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeAll
      fun createPrisoner() {
        builder.build {
          prisoner = offender(lastName = "DUBOIS") {
            booking = booking(
              prisonId = "MDI",
              bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
              movementReasonCode = REMAND_REASON,
            ) {
              transferOut(
                prisonId = "LEI",
                transferTime = LocalDateTime.parse("2023-07-20T10:00:00"),
                movementReasonCode = TRANSFER_REASON,
              )
              transferIn(
                receiveTime = LocalDateTime.parse("2023-07-20T11:00:00"),
                movementReasonCode = TRANSFER_REASON,
              )
              transferOut(
                prisonId = "SYI",
                transferTime = LocalDateTime.parse("2023-07-21T10:00:00"),
                movementReasonCode = TRANSFER_REASON,
              )
              transferIn(
                receiveTime = LocalDateTime.parse("2023-07-21T11:00:00"),
                movementReasonCode = TRANSFER_REASON,
              )
              release(
                releaseTime = LocalDateTime.parse("2023-07-22T10:00:00"),
                movementReasonCode = HOSPITAL_RELEASE_REASON,
              )
            }
          }
        }
      }

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
      }

      @Test
      fun `will have a single period covering the various transfers`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod.size()").isEqualTo(1)
      }

      @Test
      fun `prison period containing entry and exits dates and all prisons`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].bookingId").isEqualTo(booking.bookingId)
          .jsonPath("prisonPeriod[0].entryDate").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].releaseDate").isEqualTo("2023-07-22T10:00:00")
          .jsonPath("prisonPeriod[0].prisons.size()").isEqualTo("3")
          .jsonPath("prisonPeriod[0].prisons[0]").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].prisons[1]").isEqualTo("LEI")
          .jsonPath("prisonPeriod[0].prisons[2]").isEqualTo("SYI")
      }

      @Test
      fun `will have one movement due to the transfers not being significant`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").isEqualTo("2023-07-22T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").isEqualTo("REL")
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").isEqualTo("SYI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison")
          .isEqualTo("Final Discharge To Hospital-Psychiatric")
      }

      @Test
      fun `will have two transfer details`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers.size()").isEqualTo(2)
      }

      @Test
      fun `will have details of the prisons and dates`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers[0].dateOutOfPrison").isEqualTo("2023-07-20T10:00:00")
          .jsonPath("prisonPeriod[0].transfers[0].dateInToPrison").isEqualTo("2023-07-20T11:00:00")
          .jsonPath("prisonPeriod[0].transfers[0].transferReason").isEqualTo("Normal Transfer")
          .jsonPath("prisonPeriod[0].transfers[0].fromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].transfers[0].toPrisonId").isEqualTo("LEI")
          .jsonPath("prisonPeriod[0].transfers[1].dateOutOfPrison").isEqualTo("2023-07-21T10:00:00")
          .jsonPath("prisonPeriod[0].transfers[1].dateInToPrison").isEqualTo("2023-07-21T11:00:00")
          .jsonPath("prisonPeriod[0].transfers[1].transferReason").isEqualTo("Normal Transfer")
          .jsonPath("prisonPeriod[0].transfers[1].fromPrisonId").isEqualTo("LEI")
          .jsonPath("prisonPeriod[0].transfers[1].toPrisonId").isEqualTo("SYI")
      }
    }

    @Nested
    @DisplayName("Person has been tranferred out of prison but hasn't arrived in the new prison")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class TransferOutOnly {
      private lateinit var prisoner: OffenderId
      private lateinit var booking: OffenderBookingId

      @BeforeAll
      fun createPrisoner() {
        builder.build {
          prisoner = offender(lastName = "DUBOIS") {
            booking = booking(
              prisonId = "MDI",
              bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
              movementReasonCode = REMAND_REASON,
            ) {
              transferOut(
                prisonId = "LEI",
                transferTime = LocalDateTime.parse("2023-07-20T10:00:00"),
                movementReasonCode = TRANSFER_REASON,
              )
            }
          }
        }
      }

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(prisoner.offenderNo)
      }

      @Test
      fun `will have a single period covering transfer out`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod.size()").isEqualTo(1)
      }

      @Test
      fun `prison period containing just entry dates and single prison`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].bookingId").isEqualTo(booking.bookingId)
          .jsonPath("prisonPeriod[0].entryDate").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].releaseDate").doesNotExist()
          .jsonPath("prisonPeriod[0].prisons.size()").isEqualTo("1")
          .jsonPath("prisonPeriod[0].prisons[0]").isEqualTo("MDI")
      }

      @Test
      fun `will have one movement due to the transfer out not being significant`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].movementDates.size()").isEqualTo(1)
          .jsonPath("prisonPeriod[0].movementDates[0].dateInToPrison").isEqualTo("2023-07-19T10:00:00")
          .jsonPath("prisonPeriod[0].movementDates[0].inwardType").isEqualTo("ADM")
          .jsonPath("prisonPeriod[0].movementDates[0].admittedIntoPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].movementDates[0].reasonInToPrison").isEqualTo("Unconvicted Remand")
          .jsonPath("prisonPeriod[0].movementDates[0].dateOutOfPrison").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].outwardType").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].releaseFromPrisonId").doesNotExist()
          .jsonPath("prisonPeriod[0].movementDates[0].reasonOutOfPrison").doesNotExist()
      }

      @Test
      fun `will have single transfer detail`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers.size()").isEqualTo(1)
      }

      @Test
      fun `will have details only the prison they have left`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", prisoner.offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk.expectBody()
          .jsonPath("prisonPeriod[0].transfers[0].dateOutOfPrison").isEqualTo("2023-07-20T10:00:00")
          .jsonPath("prisonPeriod[0].transfers[0].dateInToPrison").doesNotExist()
          .jsonPath("prisonPeriod[0].transfers[0].transferReason").isEqualTo("Normal Transfer")
          .jsonPath("prisonPeriod[0].transfers[0].fromPrisonId").isEqualTo("MDI")
          .jsonPath("prisonPeriod[0].transfers[0].toPrisonId").doesNotExist()
      }
    }
  }
}
