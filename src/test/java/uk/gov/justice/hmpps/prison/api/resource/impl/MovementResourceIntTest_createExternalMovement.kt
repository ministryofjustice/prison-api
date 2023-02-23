@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import uk.gov.justice.hmpps.prison.api.model.CreateExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.MovementsService
import java.time.LocalDateTime

class MovementResourceIntTest_createExternalMovement : ResourceTest() {
  @MockBean
  private lateinit var movementsService: MovementsService

  @Test
  fun createExternalMovement() {
    makeRequest().expectStatus().isCreated
    verify(movementsService).createExternalMovement(
      1134751L,
      CreateExternalMovement.builder()
        .bookingId(1134751L)
        .fromAgencyId("HAZLWD")
        .toAgencyId("OUT")
        .movementTime(LocalDateTime.parse("2020-02-28T14:40:00"))
        .movementType("TRN")
        .movementReason("SEC")
        .directionCode(MovementDirection.OUT)
        .build()
    )
  }

  @Test
  fun returnsNotAuthorised() {
    webTestClient.post()
      .uri("/api/movements")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
                    {
                       "bookingId": 1134751,
                       "fromAgencyId": "HAZLWD",
                       "toAgencyId": "OUT",
                       "movementTime": "2020-02-28T14:40:00",
                       "movementType": "TRN",
                       "movementReason": "SEC",
                       "directionCode": "OUT"
                    }
                    
        """.trimIndent()
      )
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun returnsNotFound() {
    whenever(movementsService.createExternalMovement(ArgumentMatchers.anyLong(), ArgumentMatchers.any()))
      .thenThrow(EntityNotFoundException::class.java)
    makeRequest().expectStatus().isNotFound
  }

  @Test
  fun returnsBadRequest() {
    whenever(movementsService.createExternalMovement(ArgumentMatchers.anyLong(), ArgumentMatchers.any()))
      .thenThrow(IllegalStateException::class.java)
    makeRequest().expectStatus().isBadRequest
  }

  @Test
  fun returnsInternalServerError() {
    whenever(movementsService.createExternalMovement(ArgumentMatchers.anyLong(), ArgumentMatchers.any()))
      .thenThrow(RuntimeException::class.java)
    makeRequest().expectStatus().is5xxServerError
  }

  @Test
  fun handlesMissingFields() {
    webTestClient.post()
      .uri("/api/movements")
      .headers(setAuthorisation(listOf("ROLE_INACTIVE_BOOKINGS")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
          {
            "bookingId": 1134751
          }
        """.trimIndent()
      )
      .exchange().expectStatus().isBadRequest
  }

  private fun makeRequest(): ResponseSpec = webTestClient.post()
    .uri("/api/movements")
    .headers(setAuthorisation(listOf("ROLE_INACTIVE_BOOKINGS")))
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(
      """
          {
             "bookingId": 1134751,
             "fromAgencyId": "HAZLWD",
             "toAgencyId": "OUT",
             "movementTime": "2020-02-28T14:40:00",
             "movementType": "TRN",
             "movementReason": "SEC",
             "directionCode": "OUT"
          }
      """.trimIndent()
    )
    .exchange()
}
