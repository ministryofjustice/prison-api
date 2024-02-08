@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.adjudications.OffenderAdjudicationHearing
import java.time.LocalDateTime
import java.util.List
import java.util.Map

class OffenderResourceImplIntTest_getAdjudications : ResourceTest() {

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/adjudications")
  inner class OffenderAdjudications {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/offenders/A1234AA/adjudications")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.get().uri("/api/offenders/A1234AA/adjudications")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when has ROLE_SYSTEM_USER override role`() {
      webTestClient.get().uri("/api/offenders/A1234AA/adjudications")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/offenders/A1234AA/adjudications")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_VIEW_ADJUDICATIONS override role`() {
      webTestClient.get().uri("/api/offenders/A1234AA/adjudications")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_ADJUDICATIONS")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/offenders/A1234AA/adjudications")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/offenders/A1234AA/adjudications")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
    }

    @Test
    fun shouldReturnListOfAdjudicationsForUserWithCaseload() {
      val response = testRestTemplate.exchange(
        "/api/offenders/A1234AA/adjudications",
        HttpMethod.GET,
        createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), Map.of()),
        object : ParameterizedTypeReference<String?>() {
        },
      )

      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      val json = getBodyAsJsonContent<Any>(response)
      assertThat(json).extractingJsonPathArrayValue<Any>("results").isNotEmpty()
      assertThat(json).extractingJsonPathArrayValue<Any>("offences").isNotEmpty()
      assertThat(json).extractingJsonPathArrayValue<Any>("agencies").isNotEmpty()
    }

    @Test
    fun shouldReturn404WhenNoPrivileges() {
      // run with user that doesn't have access to the caseload

      val response = testRestTemplate.exchange(
        "/api/offenders/A1234AA/adjudications",
        HttpMethod.GET,
        createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf(), Map.of()),
        ErrorResponse::class.java,
      )

      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun shouldReturnListOfAdjudicationsForViewAdjudicationsRole() {
      val response = testRestTemplate.exchange(
        "/api/offenders/A1234AA/adjudications",
        HttpMethod.GET,
        createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf("ROLE_VIEW_ADJUDICATIONS"), Map.of()),
        object : ParameterizedTypeReference<String?>() {
        },
      )

      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      val json = getBodyAsJsonContent<Any>(response)
      assertThat(json).extractingJsonPathArrayValue<Any>("results").isNotEmpty()
    }
  }

  @Nested
  @DisplayName("POST /api/offenders/adjudication-hearings")
  inner class OffenderAdjudicationHearings {
    @Test
    fun shouldReturnOffenderAdjudicationHearingsWhenMatch() {
      webTestClient.post().uri("/api/offenders/adjudication-hearings?agencyId=LEI&fromDate=2015-01-02&toDate=2015-01-03")
        .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .bodyValue(setOf("A1181HH"))
        .exchange()
        .expectBodyList(ParameterizedTypeReference.forType<Any>(OffenderAdjudicationHearing::class.java))
        .isEqualTo<ListBodySpec<Any>>(
          List.of<Any>(
            OffenderAdjudicationHearing(
              "LEI",
              "A1181HH",
              -1,
              "Governor's Hearing Adult",
              LocalDateTime.of(2015, 1, 2, 14, 0),
              -1000,
              "LEI-AABCW-1",
              "SCH",
            ),
            OffenderAdjudicationHearing(
              "LEI",
              "A1181HH",
              -2,
              "Governor's Hearing Adult",
              LocalDateTime.of(2015, 1, 2, 14, 0),
              -1001,
              "LEI-A-1-1001",
              "SCH",
            ),
          ),
        )
    }

    @Test
    fun shouldReturnNoOffenderAdjudicationHearingsWhenNoMatch() {
      webTestClient.post().uri("/api/offenders/adjudication-hearings?agencyId=LEI&fromDate=2015-01-02&toDate=2015-01-03")
        .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .bodyValue(setOf("XXXXXXX"))
        .exchange()
        .expectBodyList(ParameterizedTypeReference.forType<Any>(OffenderAdjudicationHearing::class.java))
        .isEqualTo<ListBodySpec<Any>>(emptyList())
    }

    @Test
    fun shouldReturn403WhenInsufficientPrivileges() {
      webTestClient.post().uri("/api/offenders/adjudication-hearings?agencyId=LEI&fromDate=2015-01-02&toDate=2015-01-03")
        .headers(setAuthorisation(listOf()))
        .bodyValue(setOf("A1181HH"))
        .exchange()
        .expectStatus()
        .isForbidden()
    }
  }
}
