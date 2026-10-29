@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.hmpps.prison.dsl.NomisDataBuilder
import uk.gov.justice.hmpps.prison.dsl.OffenderBookingId
import uk.gov.justice.hmpps.prison.dsl.OffenderId
import java.time.LocalDateTime

class OffenderResourceCellHistoryIntTest : ResourceTest() {
  @Autowired
  private lateinit var builder: NomisDataBuilder

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/cell-history")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  inner class GetCellHistory {
    private lateinit var prisoner: OffenderId
    private lateinit var firstBooking: OffenderBookingId
    private lateinit var secondBooking: OffenderBookingId

    @BeforeAll
    fun setUp() {
      builder.build {
        prisoner = offender(lastName = "DUBOIS") {
          firstBooking = booking(
            prisonId = "MDI",
            bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
          ) {
            release(releaseTime = LocalDateTime.parse("2023-07-20T10:00:00"))
          }
          secondBooking = booking(
            prisonId = "LEI",
            bookingInTime = LocalDateTime.parse("2023-07-25T10:00:00"),
          ) {}
        }
      }
    }

    @AfterAll
    fun deletePrisoner() {
      builder.deletePrisoner(prisoner.offenderNo)
    }

    @Test
    fun `returns stays from every booking that overlap the period, most recent first`() {
      webTestClient.get()
        .uri(
          "/api/offenders/{offenderNo}/cell-history?fromDate={fromDate}&toDate={toDate}",
          prisoner.offenderNo,
          "2023-07-19T12:00:00",
          "2023-07-26T00:00:00",
        )
        .headers(setClientAuthorisation(listOf("MAINTAIN_CELL_MOVEMENTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[0].bookingId").isEqualTo(secondBooking.bookingId.toInt())
        .jsonPath("[0].agencyId").isEqualTo("LEI")
        .jsonPath("[0].assignmentEndDateTime").doesNotExist()
        .jsonPath("[0].offenderNo").isEqualTo(prisoner.offenderNo)
        .jsonPath("[1].bookingId").isEqualTo(firstBooking.bookingId.toInt())
        .jsonPath("[1].agencyId").isEqualTo("MDI")
        .jsonPath("[1].assignmentDateTime").isEqualTo("2023-07-19T10:00:00")
        .jsonPath("[1].assignmentEndDateTime").isEqualTo("2023-07-20T10:00:00")
    }

    @Test
    fun `returns only the earlier booking when the period ends before readmission`() {
      webTestClient.get()
        .uri(
          "/api/offenders/{offenderNo}/cell-history?fromDate={fromDate}&toDate={toDate}",
          prisoner.offenderNo,
          "2023-07-18T00:00:00",
          "2023-07-21T00:00:00",
        )
        .headers(setClientAuthorisation(listOf("MAINTAIN_CELL_MOVEMENTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[0].bookingId").isEqualTo(firstBooking.bookingId.toInt())
    }

    @Test
    fun `returns nothing for a period when the prisoner was not in prison`() {
      webTestClient.get()
        .uri(
          "/api/offenders/{offenderNo}/cell-history?fromDate={fromDate}&toDate={toDate}",
          prisoner.offenderNo,
          "2023-07-21T00:00:00",
          "2023-07-24T00:00:00",
        )
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `returns a stay that started before the period and is still open`() {
      webTestClient.get()
        .uri(
          "/api/offenders/{offenderNo}/cell-history?fromDate={fromDate}&toDate={toDate}",
          prisoner.offenderNo,
          LocalDateTime.now().minusDays(1).toString(),
          LocalDateTime.now().toString(),
        )
        .headers(setClientAuthorisation(listOf("MAINTAIN_CELL_MOVEMENTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[0].bookingId").isEqualTo(secondBooking.bookingId.toInt())
    }

    @Test
    fun `bad request when fromDate is after toDate`() {
      webTestClient.get()
        .uri(
          "/api/offenders/{offenderNo}/cell-history?fromDate={fromDate}&toDate={toDate}",
          prisoner.offenderNo,
          "2023-07-26T00:00:00",
          "2023-07-19T00:00:00",
        )
        .headers(setClientAuthorisation(listOf("MAINTAIN_CELL_MOVEMENTS")))
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `bad request when a date is missing`() {
      webTestClient.get()
        .uri("/api/offenders/{offenderNo}/cell-history?fromDate={fromDate}", prisoner.offenderNo, "2023-07-19T00:00:00")
        .headers(setClientAuthorisation(listOf("MAINTAIN_CELL_MOVEMENTS")))
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `not found when the prisoner does not exist`() {
      webTestClient.get()
        .uri(
          "/api/offenders/{offenderNo}/cell-history?fromDate={fromDate}&toDate={toDate}",
          "Z9999ZZ",
          "2023-07-19T00:00:00",
          "2023-07-26T00:00:00",
        )
        .headers(setClientAuthorisation(listOf("MAINTAIN_CELL_MOVEMENTS")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `forbidden when the client has no role giving access to the prisoner`() {
      webTestClient.get()
        .uri(
          "/api/offenders/{offenderNo}/cell-history?fromDate={fromDate}&toDate={toDate}",
          prisoner.offenderNo,
          "2023-07-19T00:00:00",
          "2023-07-26T00:00:00",
        )
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `unauthorised with no auth token`() {
      webTestClient.get()
        .uri(
          "/api/offenders/{offenderNo}/cell-history?fromDate={fromDate}&toDate={toDate}",
          prisoner.offenderNo,
          "2023-07-19T00:00:00",
          "2023-07-26T00:00:00",
        )
        .exchange()
        .expectStatus().isUnauthorized
    }
  }
}
