package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doThrow
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import uk.gov.justice.hmpps.prison.service.AgencyService
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now

class CellResourceHistoryTest : ResourceTest() {

  companion object {
    private const val CELL_LOCATION_ID = -16
    private const val AGENCY_ID = "LEI"
    private val ASSIGNMENT_DATE = LocalDate.of(2020, 4, 3).toString()
  }

  @MockBean
  private lateinit var agencyService: AgencyService

  @Test
  fun returnAllBedHistoriesForDateAndAgency() {
    val response = makeRequest(AGENCY_ID, ASSIGNMENT_DATE)
    response.expectStatus().isOk
    response.expectBody()
      .jsonPath("$.length()").isEqualTo(2)
      .jsonPath("[*].agencyId").value<List<String>> { assertThat(it).containsOnly(AGENCY_ID) }
      .jsonPath("[*].assignmentDate").value<List<String>> { assertThat(it).containsOnly(ASSIGNMENT_DATE) }
  }

  @Test
  fun returnsHttpNotFoundForAgenciesOutsideOfCurrentUsersCaseload() {
    doThrow(EntityNotFoundException("Not found")).whenever(agencyService).verifyAgencyAccess(anyString())
    makeRequest(AGENCY_ID, ASSIGNMENT_DATE).expectStatus().isNotFound
  }

  @Test
  fun returnAllBedHistoriesForDateRangeOnly() {
    val fromDateTime = LocalDateTime.of(2000, 10, 16, 10, 10, 10)
    val toDateTime = LocalDateTime.of(2020, 10, 10, 11, 11, 11)
    val response = makeRequest(CELL_LOCATION_ID, fromDateTime.toString(), toDateTime.toString())
    response.expectStatus().isOk
    response.expectBody()
      .jsonPath("$.length()").isEqualTo(3)
      .jsonPath("[*].livingUnitId").value<List<Int>> { assertThat(it).containsOnly(CELL_LOCATION_ID) }
      .jsonPath("[0].assignmentDate").isEqualTo("2019-10-17")
      .jsonPath("[1].assignmentDate").isEqualTo("2020-04-03")
      .jsonPath("[2].assignmentDate").isEqualTo("1985-04-03")
  }

  @Test
  fun handleInvalidFromDate() {
    makeRequest(CELL_LOCATION_ID, "hello", now().toString()).expectStatus().isBadRequest
  }

  @Test
  fun handleInvalidToDate() {
    makeRequest(CELL_LOCATION_ID, now().toString(), "hello").expectStatus().isBadRequest
  }

  @Test
  fun handleCellNotFound() {
    makeRequest(-991873, now().toString(), now().toString()).expectStatus().isNotFound
  }

  private fun makeRequest(locationId: Int, fromDate: String, toDate: String): ResponseSpec {
    return webTestClient.get()
      .uri("/api/cell/{cellLocationId}/history?fromDate={fromDate}&toDate={toDate}", locationId, fromDate, toDate)
      .headers(setAuthorisation(listOf()))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .exchange()
  }

  private fun makeRequest(agencyId: String, assignmentDate: String): ResponseSpec {
    return webTestClient.get()
      .uri("/api/cell/{agencyId}/history/{assignmentDate}", agencyId, assignmentDate)
      .headers(setAuthorisation(listOf()))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .exchange()
  }
}
