package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.util.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.OffenderBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * KOTLIN
 */

class OffendersResourceTransferImpTest : ResourceTest() {
  @Nested
  @DisplayName("PUT /{offenderNo}/transfer-in")
  inner class TransferIn {
    @Nested
    @DisplayName("Successful transfer in")
    inner class Success {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        offenderNo =
          OffenderBuilder().withBooking(
            OffenderBookingBuilder(
              prisonId = "LEI",
              bookingInTime = LocalDateTime.now().minusDays(1)
            )
          ).save(webTestClient = webTestClient, jwtAuthenticationHelper = jwtAuthenticationHelper).offenderNo

        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-out", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "transferReasonCode":"NOTR",
            "commentText":"transferred prisoner today",
            "toLocation":"MDI",
            "movementTime": "${LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("inOutStatus").isEqualTo("TRN")
          .jsonPath("status").isEqualTo("INACTIVE TRN")
          .jsonPath("lastMovementTypeCode").isEqualTo("TRN")
          .jsonPath("lastMovementReasonCode").isEqualTo("NOTR")
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("TRN")
          .jsonPath("assignedLivingUnit.description").doesNotExist()
      }

      @Test
      internal fun `can transfer a prisoner out and back in to prison`() {
        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "transferReasonCode":"NOTR",
            "commentText":"admitted",
            "cellLocation":"MDI-1-3-022",
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("inOutStatus").isEqualTo("IN")
          .jsonPath("status").isEqualTo("ACTIVE IN")
          .jsonPath("lastMovementTypeCode").isEqualTo("ADM")
          .jsonPath("lastMovementReasonCode").isEqualTo("INT")
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("MDI")
          .jsonPath("assignedLivingUnit.description").isEqualTo("1-3-022")
      }
    }

    @Nested
    @DisplayName("Failed transfer in")
    inner class Failed {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        offenderNo =
          OffenderBuilder().withBooking(
            OffenderBookingBuilder(
              prisonId = "LEI",
              bookingInTime = LocalDateTime.now().minusDays(1)
            )
          ).save(webTestClient = webTestClient, jwtAuthenticationHelper = jwtAuthenticationHelper).offenderNo

        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-out", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "transferReasonCode":"NOTR",
            "commentText":"transferred prisoner today",
            "toLocation":"MDI",
            "movementTime": "${LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("inOutStatus").isEqualTo("TRN")
          .jsonPath("status").isEqualTo("INACTIVE TRN")
          .jsonPath("lastMovementTypeCode").isEqualTo("TRN")
          .jsonPath("lastMovementReasonCode").isEqualTo("NOTR")
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("TRN")
          .jsonPath("assignedLivingUnit.description").doesNotExist()
      }

      @Test
      internal fun `cannot transfer a prisoner in to a full cell`() {
        webTestClient.put()
          .uri("/api/offenders/{nomsId}/transfer-in", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              """
          {
            "transferReasonCode":"NOTR",
            "commentText":"admitted",
            "cellLocation":"MDI-FULL",
            "receiveTime": "${
              LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              }"
            
          }
              """.trimIndent()
            )
          )
          .exchange()
          .expectStatus().isEqualTo(409)
          .expectBody()
          .jsonPath("userMessage").isEqualTo("The cell MDI-FULL does not have any available capacity")
      }
    }
  }

  @Nested
  @DisplayName("PUT /{offenderNo}/court-transfer-in")
  inner class CourtTransferIn
}
