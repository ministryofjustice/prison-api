@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GET /api/bookings/{bookingId}/cell-history")
class BookingResourceIntTest_getBedAssignmentHistory : ResourceTest() {

  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .exchange().expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if no override role`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .headers(setClientAuthorisation(listOf())).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 403 if has override role ROLE_SYSTEM_USER`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER"))).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 200 if has override role ROLE_MAINTAIN_CELL_MOVEMENTS`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_CELL_MOVEMENTS"))).exchange().expectStatus().isOk
  }

  @Test
  fun `returns 200 if has override role ROLE_VIEW_PRISONER_DATA`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange().expectStatus().isOk
  }

  @Test
  fun `returns 200 if not in user caseload but has ROLE_VIEW_PRISONER_DATA`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA"))).exchange().expectStatus().isOk
  }

  @Test
  fun `returns 200 if in user caseload`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .headers(setAuthorisation(listOf())).exchange().expectStatus().isOk
  }

  @Test
  fun `returns 403 if not in user caseload`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .headers(setAuthorisation("WAI_USER", listOf())).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 403 if user has no caseloads`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .headers(setAuthorisation("RO_USER", listOf())).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 404 if client has override role and booking does not exist`() {
    webTestClient.get().uri("/api/bookings/-99999/cell-history")
      .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA"))).exchange().expectStatus().isNotFound
  }

  @Test
  fun `returns 403 if client does not have override role and booking does not exist`() {
    webTestClient.get().uri("/api/bookings/-99999/cell-history")
      .headers(setClientAuthorisation(listOf())).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 403 if user has caseloads and booking does not exist`() {
    webTestClient.get().uri("/api/bookings/-99999/cell-history")
      .headers(setAuthorisation("ITAG_USER", listOf())).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 403 if user does not have any caseloads and booking does not exist`() {
    webTestClient.get().uri("/api/bookings/-99999/cell-history")
      .headers(setAuthorisation("RO_USER", listOf())).exchange().expectStatus().isForbidden
  }

  @Test
  fun `bed assignment history with default sorting`() {
    webTestClient.get().uri("/api/bookings/-36/cell-history")
      .headers(setAuthorisation(listOf())).exchange().expectStatus().isOk.expectBody()
      .jsonPath("$.content[0].bookingId").isEqualTo(-36)
      .jsonPath("$.content[0].livingUnitId").isEqualTo(-18)
      .jsonPath("$.content[0].assignmentDate").isEqualTo("2060-10-17")
      .jsonPath("$.content[0].assignmentDateTime").isEqualTo("2060-10-17T11:00:00")
      .jsonPath("$.content[0].assignmentReason").isEqualTo("ADM")
      .jsonPath("$.content[0].agencyId").isEqualTo("LEI")
      .jsonPath("$.content[0].description").isEqualTo("LEI-H-1-4")
      .jsonPath("$.content[0].bedAssignmentHistorySequence").isEqualTo(4)
      .jsonPath("$.content[0].movementMadeBy").isEqualTo("SA")
      .jsonPath("$.content[0].offenderNo").isEqualTo("A1180MA")
      .jsonPath("$.content[1].bookingId").isEqualTo(-36)
      .jsonPath("$.content[1].livingUnitId").isEqualTo(-17)
      .jsonPath("$.content[1].assignmentDate").isEqualTo("2050-10-17")
      .jsonPath("$.content[1].assignmentDateTime").isEqualTo("2050-10-17T11:00:00")
      .jsonPath("$.content[1].assignmentReason").isEqualTo("ADM")
      .jsonPath("$.content[1].agencyId").isEqualTo("LEI")
      .jsonPath("$.content[1].description").isEqualTo("LEI-H-1-3")
      .jsonPath("$.content[1].bedAssignmentHistorySequence").isEqualTo(3)
      .jsonPath("$.content[1].movementMadeBy").isEqualTo("SA")
      .jsonPath("$.content[1].offenderNo").isEqualTo("A1180MA")
      .jsonPath("$.content[2].bookingId").isEqualTo(-36)
      .jsonPath("$.content[2].livingUnitId").isEqualTo(-16)
      .jsonPath("$.content[2].assignmentDate").isEqualTo("2040-10-17")
      .jsonPath("$.content[2].assignmentDateTime").isEqualTo("2040-10-17T11:00:00")
      .jsonPath("$.content[2].assignmentReason").isEqualTo("ADM")
      .jsonPath("$.content[2].agencyId").isEqualTo("LEI")
      .jsonPath("$.content[2].description").isEqualTo("LEI-H-1-2")
      .jsonPath("$.content[2].bedAssignmentHistorySequence").isEqualTo(2)
      .jsonPath("$.content[2].movementMadeBy").isEqualTo("SA")
      .jsonPath("$.content[2].offenderNo").isEqualTo("A1180MA")
  }
}
