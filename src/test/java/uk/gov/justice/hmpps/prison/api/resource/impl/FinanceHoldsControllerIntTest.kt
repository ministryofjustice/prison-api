package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.hmpps.prison.api.resource.AddHoldRequest
import uk.gov.justice.hmpps.prison.repository.storedprocs.AddFinanceHold
import uk.gov.justice.hmpps.prison.repository.storedprocs.RemoveFinanceHold
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata
import java.math.BigDecimal

class FinanceHoldsControllerIntTest : ResourceTest() {
  private val prisonId = "LEI"
  private val nomisId = "A1234AA"
  private val offenderId = -1001L
  private val holdNumber = 1234L

  private val holdRequest = AddHoldRequest(
    subAccountType = "spends",
    amount = 543,
    holdDescription = "Test Hold",
  )

  @MockitoBean
  private lateinit var addFinanceHold: AddFinanceHold

  @MockitoBean
  private lateinit var removeFinanceHold: RemoveFinanceHold

  @BeforeEach
  internal fun setup() {
  }

  @AfterEach
  internal fun tearDown() {
  }

  @Nested
  @DisplayName("POST /api/prison/{prisonId}/offenders/{nomsId}/finance/holds")
  inner class AddHold {
    @Nested
    inner class Security {
      @Test
      fun `access forbidden when no role`() {
        webTestClient.post().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(jsonString(holdRequest))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.post().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds")
          .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(jsonString(holdRequest))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access unauthorised with no auth token`() {
        webTestClient.post().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(jsonString(holdRequest))
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      internal fun `hold details are returned`() {
        whenever(addFinanceHold.execute(ArgumentMatchers.any(SqlParameterSource::class.java))).thenReturn(
          mapOf<String, Any>(StoreProcMetadata.P_HOLD_NUMBER to BigDecimal.valueOf(holdNumber)),
        )

        webTestClient.post().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(jsonString(holdRequest))
          .exchange()
          .expectStatus().isCreated
          .expectBody()
          .jsonPath("holdNumber").isEqualTo(holdNumber)
      }

      @Test
      internal fun `will call the stored procedure with params`() {
        val captor = argumentCaptor<SqlParameterSource>()
        whenever(addFinanceHold.execute(captor.capture())).thenReturn(
          mapOf(StoreProcMetadata.P_HOLD_NUMBER to BigDecimal.valueOf(holdNumber)),
        )

        webTestClient.post().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(jsonString(holdRequest))
          .exchange()
          .expectStatus().isCreated

        val captured = captor.firstValue
        assertThat(captured.getValue(StoreProcMetadata.P_NOMS_ID)).isEqualTo(nomisId)
        assertThat(captured.getValue(StoreProcMetadata.P_ROOT_OFFENDER_ID)).isEqualTo(offenderId)
        assertThat(captured.getValue(StoreProcMetadata.P_AGY_LOC_ID)).isEqualTo(prisonId)
        assertThat(captured.getValue(StoreProcMetadata.P_TXN_ENTRY_AMOUNT)).isEqualTo(BigDecimal.valueOf(holdRequest.amount).movePointLeft(2))
        // TODO add in
        // assertThat(captured.getValue(StoreProcMetadata.P_ACCOUNT_TYPE)).isEqualTo(holdRequest.subAccountType)
        assertThat(captured.getValue(StoreProcMetadata.P_TXN_ENTRY_DESC)).isEqualTo(holdRequest.holdDescription)
      }
    }

    @Nested
    inner class UnhappyPath {
      @Test
      @Disabled("can implement this if not using a stored procedure")
      internal fun `404 when prison does not exist`() {
        webTestClient.post().uri("/api/prison/XXX/offenders/$nomisId/finance/holds")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(jsonString(holdRequest))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      @Disabled("can implement this if not using a stored procedure")
      internal fun `404 when offender at a different prison`() {
        webTestClient.post().uri("/api/prison/ASI/offenders/$nomisId/finance/holds")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(jsonString(holdRequest))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      internal fun `account code is invalid`() {
        webTestClient.post().uri("/api/prison/ASI/offenders/$nomisId/finance/holds")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(jsonString(holdRequest.copy(subAccountType = "invalid")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage").isEqualTo("SubAccountCode not found")
      }

      @Test
      internal fun `404 when offender does not exist`() {
        webTestClient.post().uri("/api/prison/$prisonId/offenders/A9999BB/finance/holds")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(jsonString(holdRequest))
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage").isEqualTo("Offender not found")
      }
    }
  }

  @Nested
  @DisplayName("DELETE /api/prison/{prisonId}/offenders/{nomsId}/finance/holds/{holdNumber}")
  inner class RemoveHold {

    @Nested
    inner class Security {
      @Test
      fun `access forbidden when no role`() {
        webTestClient.delete().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds/$holdNumber")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.delete().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds/$holdNumber")
          .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access unauthorised with no auth token`() {
        webTestClient.delete().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds/$holdNumber")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `when success`() {
        doNothing().whenever(removeFinanceHold)
          .execute(ArgumentMatchers.any(SqlParameterSource::class.java))

        webTestClient.delete().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds/$holdNumber")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isNoContent
      }

      @Test
      internal fun `will call the stored procedure with params`() {
        val captor = argumentCaptor<SqlParameterSource>()

        doNothing().whenever(removeFinanceHold)
          .execute(captor.capture())

        webTestClient.delete().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds/$holdNumber")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isNoContent

        val captured = captor.firstValue
        assertThat(captured.getValue(StoreProcMetadata.P_HOLD_NUMBER)).isEqualTo(holdNumber)
      }
    }

    @Nested
    inner class UnhappyPath {
      @Test
      @Disabled("can implement this if not using a stored procedure")
      fun `when holdNumber does not exist`() {
        webTestClient.delete().uri("/api/prison/$prisonId/offenders/$nomisId/finance/holds/$holdNumber")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      @Disabled("can implement this if not using a stored procedure")
      internal fun `404 when offender at a different prison`() {
        webTestClient.delete().uri("/api/prison/MDI/offenders/$nomisId/finance/holds/$holdNumber")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      @Disabled("can implement this if not using a stored procedure")
      internal fun `404 when prison does not exist`() {
        webTestClient.delete().uri("/api/prison/XXX/offenders/$nomisId/finance/holds/$holdNumber")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      internal fun `404 when offender does not exist`() {
        webTestClient.delete().uri("/api/prison/$prisonId/offenders/A9999BB/finance/holds/$holdNumber")
          .headers(setAuthorisation(roles = listOf("NOMIS_API_V1")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isNotFound
      }
    }
  }
}
