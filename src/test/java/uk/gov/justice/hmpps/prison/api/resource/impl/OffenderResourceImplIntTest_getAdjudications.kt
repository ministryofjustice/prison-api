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

class OffenderResourceImplIntTest_getAdjudications : ResourceTest() {

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/adjudications")
  inner class OffenderAdjudications {

    @Nested
    inner class Authorisation {

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
      fun `returns 403 if not in user caseload`() {
        webTestClient.get().uri("/api/offenders/A1234AA/adjudications")
          .headers(setAuthorisation("WAI_USER", listOf()))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -1.")
      }

      @Test
      fun `returns 403 if user has no caseloads`() {
        webTestClient.get().uri("/api/offenders/A1234AA/adjudications")
          .headers(setAuthorisation("RO_USER", listOf()))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -1.")
      }

      @Test
      fun shouldReturn403WhenNoPrivileges() {
        // run with user that doesn't have access to the caseload

        val response = testRestTemplate.exchange(
          "/api/offenders/A1234AA/adjudications",
          HttpMethod.GET,
          createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf(), mapOf()),
          ErrorResponse::class.java,
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
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
    }

    @Test
    fun shouldReturnListOfAdjudicationsForUserWithCaseload() {
      val response = testRestTemplate.exchange(
        "/api/offenders/A1234AA/adjudications",
        HttpMethod.GET,
        createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf()),
        object : ParameterizedTypeReference<String>() {
        },
      )

      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      val json = getBodyAsJsonContent<Any>(response)
      assertThat(json).extractingJsonPathArrayValue<Any>("results").isNotEmpty()
      assertThat(json).extractingJsonPathArrayValue<Any>("offences").isNotEmpty()
      assertThat(json).extractingJsonPathArrayValue<Any>("agencies").isNotEmpty()
    }

    @Test
    fun `returns 404 if offender does not exist`() {
      webTestClient.get().uri("/api/offenders/Does Not Exist/adjudications")
        .headers(setAuthorisation("PRISON_API_USER", listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `A staff user views the adjudications for an existing offender`() {
      webTestClient.get().uri("/api/offenders/A1181HH/adjudications")
        .headers(setAuthorisation("PRISON_API_USER", listOf("ROLE_VIEW_ADJUDICATIONS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("results.size()").isEqualTo(2)
        .jsonPath("results[0].adjudicationNumber").isEqualTo(-7)
        .jsonPath("results[0].reportTime").isEqualTo("2019-08-25T00:03:00")
        .jsonPath("results[0].agencyId").isEqualTo("MDI")
        .jsonPath("results[0].adjudicationCharges[0].offenceCode").isEqualTo("51:2D")
        .jsonPath("results[0].adjudicationCharges[1].offenceCode").isEqualTo("51:2D")
        .jsonPath("results[0].adjudicationCharges[0].findingCode").isEqualTo("PROVED")
        .jsonPath("results[0].adjudicationCharges[1].findingCode").isEqualTo("PROVED")
        .jsonPath("results[1].adjudicationNumber").isEqualTo(-2)
        .jsonPath("results[1].reportTime").isEqualTo("2017-02-23T00:01:00")
        .jsonPath("results[1].agencyId").isEqualTo("LEI")
        .jsonPath("results[1].adjudicationCharges[0].offenceCode").isEqualTo("51:2C")
        .jsonPath("results[1].adjudicationCharges[0].findingCode").isEqualTo("NOT_PROVEN")
        .jsonPath("offences[0].code").isEqualTo("51:2C")
        .jsonPath("offences[1].code").isEqualTo("51:2D")
        .jsonPath("agencies[0].agencyId").isEqualTo("LEI")
        .jsonPath("agencies[1].agencyId").isEqualTo("MDI")
    }

    @Test
    fun `A staff user views the adjudications for an existing offender by agencyId and offence id`() {
      webTestClient.get().uri("/api/offenders/A1181GG/adjudications?agencyId=LEI&offenceId=86")
        .headers(setAuthorisation("PRISON_API_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("results.size()").isEqualTo(1)
        .jsonPath("results[0].adjudicationNumber").isEqualTo(-3001)
        .jsonPath("results[0].reportTime").isEqualTo("2019-09-25T00:04:00")
        .jsonPath("results[0].agencyId").isEqualTo("LEI")
        .jsonPath("results[0].adjudicationCharges[0].offenceCode").isEqualTo("51:8D")
        .jsonPath("results[0].adjudicationCharges[0].findingCode").doesNotExist()
        .jsonPath("offences[0].code").isEqualTo("51:2D")
        .jsonPath("offences[1].code").isEqualTo("51:8D")
        .jsonPath("agencies[0].agencyId").isEqualTo("LEI")
        .jsonPath("agencies[1].agencyId").isEqualTo("MDI")
        .jsonPath("agencies[2].agencyId").isEqualTo("BXI")
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
        .expectBodyList(ParameterizedTypeReference.forType(OffenderAdjudicationHearing::class.java))
        .isEqualTo<ListBodySpec<Any>>(
          listOf<Any>(
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
        .expectBodyList(ParameterizedTypeReference.forType(OffenderAdjudicationHearing::class.java))
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
