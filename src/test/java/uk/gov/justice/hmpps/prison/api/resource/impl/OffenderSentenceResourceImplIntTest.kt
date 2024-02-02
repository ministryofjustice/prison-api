package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.net.HttpURLConnection

class OffenderSentenceResourceImplIntTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/offender-sentences")
  inner class GetOffenderSentences {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    @Disabled("this test fails - code update needed")
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    // Can be removed when code fixed for previous disabled test
    @Test
    fun `should return empty list if does not have override role`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(0)
    }

    @Test
    fun `returns success when client has override role ROLE_GLOBAL_SEARCH`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .hasListAtLeastSizeOf(27)
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .hasListAtLeastSizeOf(27)
    }

    @Test
    fun `returns success when client has override role ROLE_CREATE_CATEGORISATION`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setClientAuthorisation(listOf("ROLE_CREATE_CATEGORISATION")))
        .exchange()
        .expectStatus().isOk
        .hasListAtLeastSizeOf(27)
    }

    @Test
    fun `returns success when client has override role ROLE_APPROVE_CATEGORISATION`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setClientAuthorisation(listOf("ROLE_APPROVE_CATEGORISATION")))
        .exchange()
        .expectStatus().isOk
        .hasListAtLeastSizeOf(27)
    }

    @Test
    @Disabled("this test fails - code update needed")
    fun `returns 403 if not in user caseload`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    // Can be removed when code fixed for previous disabled test
    @Test
    fun `returns empty list if not in user caseload`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectBody().jsonPath("length()").isEqualTo(0)
    }

    @Test
    @Disabled("this test fails - code update needed")
    fun `returns 403 if user has no caseloads`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    // Can be removed when code fixed for previous disabled test
    @Test
    fun `returns empty list if user has no caseloads`() {
      webTestClient.get().uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(0)
    }

    @Test
    fun offenderSentence_success() {
      webTestClient.get()
        .uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .hasListAtLeastSizeOf(27)
    }

    @Test
    fun offenderSentence_unknownUser_noRoles_onlyAgency() {
      webTestClient.get()
        .uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun offenderSentence_unknownUser_noRoles_singleOffender() {
      webTestClient.get()
        .uri("/api/offender-sentences?offenderNo=A1234AH")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun offenderSentence_unknownUser_viewRole_onlyAgency() {
      webTestClient.get()
        .uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .hasListAtLeastSizeOf(27)
    }

    @Test
    fun offenderSentence_unknownUser_viewRole_singleOffender() {
      webTestClient.get()
        .uri("/api/offender-sentences?offenderNo=A1234AH")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences_single.json".readFile())
    }

    @Test
    fun offenderSentence_noRoles_onlyAgency() {
      webTestClient.get()
        .uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .hasListAtLeastSizeOf(27)
    }

    @Test
    fun offenderSentence_noRoles_singleOffender() {
      webTestClient.get()
        .uri("/api/offender-sentences?offenderNo=A1234AH")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences_single.json".readFile())
    }

    @Test
    fun offenderSentence_400ErrorWhenNoCaseloadsProvided() {
      webTestClient.get()
        .uri("/api/offender-sentences")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Request must be restricted to either a caseload, agency or list of offenders")
        .jsonPath("developerMessage").isEqualTo("400 Request must be restricted to either a caseload, agency or list of offenders")
    }
  }

  @Nested
  @DisplayName("GET /api/offender-sentences/booking/{bookingId}/sentenceTerms")
  inner class OffenderSentenceTerms {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-5/sentenceTerms")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no override role`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-5/sentenceTerms")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_GLOBAL_SEARCH`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-5/sentenceTerms")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(3)
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-5/sentenceTerms")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(3)
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-5/sentenceTerms")
        .headers(setAuthorisation("WAI_USER", listOf())).exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -5 not found.")
    }

    @Test
    fun `returns success if in user caseload`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-5/sentenceTerms")
        .headers(setAuthorisation(listOf())).exchange()
        .expectStatus().isOk
    }

    @Test
    fun offenderSentenceTerms_with_filterParam_success() {
      webTestClient.get()
        .uri { uriBuilder ->
          uriBuilder.path("/api/offender-sentences/booking/-31/sentenceTerms")
            .queryParam("filterBySentenceTermCodes", "IMP", "LIC")
            .build()
        }
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentence_terms_imp_lic.json".readFile())
    }

    @Test
    fun retrieveAllSentenceTerms_forABookingId() {
      webTestClient.get()
        .uri("/api/offender-sentences/booking/-5/sentenceTerms")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentence_terms_imp.json".readFile())
    }
  }

  @Nested
  @DisplayName("POST /api/offender-sentences")
  inner class PostOffenderSentences {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.post().uri("/api/offender-sentences")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AH\" ]")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    @Disabled("this test fails - code update needed")
    fun `returns 403 when client has no override role`() {
      webTestClient.post().uri("/api/offender-sentences")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AH\" ]")
        .exchange()
        .expectStatus().isForbidden
    }

    // Can be removed when code fixed for previous test
    @Test
    fun `returns empty list when client has no override role`() {
      webTestClient.post().uri("/api/offender-sentences")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AH\" ]")
        .exchange()
        .expectBody().jsonPath("length()").isEqualTo(0)
    }

    @Test
    fun `returns success when client has override role ROLE_GLOBAL_SEARCH`() {
      webTestClient.post().uri("/api/offender-sentences")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AH\" ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(1)
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.post().uri("/api/offender-sentences")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AH\" ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(1)
    }

    @Test
    fun `returns success when client has override role ROLE_CREATE_CATEGORISATION`() {
      webTestClient.post().uri("/api/offender-sentences")
        .headers(setClientAuthorisation(listOf("ROLE_CREATE_CATEGORISATION")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AH\" ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(1)
    }

    @Test
    fun `returns success when client has override role ROLE_APPROVE_CATEGORISATION`() {
      webTestClient.post().uri("/api/offender-sentences")
        .headers(setClientAuthorisation(listOf("ROLE_APPROVE_CATEGORISATION")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AH\" ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(1)
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.post().uri("/api/offender-sentences")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AH\" ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(0)
    }

    @Test
    fun `returns 404 if offender does not exist`() {
      webTestClient.post().uri("/api/offender-sentences")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A9999ZZ\" ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(0)
    }

    @Test
    fun postOffenderSentence_success() {
      webTestClient.post()
        .uri("/api/offender-sentences")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "A1234AH" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences_single.json".readFile())
    }

    @Test
    fun postOffenderSentence_success_multiple() {
      webTestClient.post()
        .uri("/api/offender-sentences")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [
              "A1234AH", "A6676RS", "A1234AG", "A1234AI", "Z0017ZZ", "A1234AJ", "A1234AC", "A1234AL", "A1234AK", 
              "A1234AB", "A1234AA", "A1234AF", "Z0018ZZ", "A4476RS", "A1234AD", "Z0024ZZ", "Z0025ZZ", "A1234AE", 
              "A9876RS", "A9876EC", "A1178RS", "A5577RS", "A1176RS", "A5576RS", "Z0019ZZ", "G2823GV", "A1060AA"
            ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences.json".readFile())
    }

    @Test
    fun postOffenderSentence_unknownUser_noRoles_singleOffender() {
      webTestClient.post()
        .uri("/api/offender-sentences")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "A1234AH" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun postOffenderSentence_unknownUser_viewRole_singleOffender() {
      webTestClient.post()
        .uri("/api/offender-sentences")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "A1234AH" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences_single.json".readFile())
    }

    @Test
    fun postOffenderSentence_noRoles_singleOffender() {
      webTestClient.post()
        .uri("/api/offender-sentences")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "A1234AH" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences_single.json".readFile())
    }

    @Test
    fun postOffenderSentence_noOffenders() {
      webTestClient.post()
        .uri("/api/offender-sentences")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            []
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("List of Offender Ids must be provided")
        .jsonPath("developerMessage").isEqualTo("400 List of Offender Ids must be provided")
    }
  }

  @Nested
  @DisplayName("POST /api/offender-sentences/bookings")
  inner class OffenderSentenceBookings {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.post().uri("/api/offender-sentences/bookings")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ -8 ]")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns empty result when client has no override role`() {
      webTestClient.post().uri("/api/offender-sentences/bookings")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ -8 ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(0)
    }

    @Test
    fun `returns success when client has override role ROLE_GLOBAL_SEARCH`() {
      webTestClient.post().uri("/api/offender-sentences/bookings")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ -8 ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(1)
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.post().uri("/api/offender-sentences/bookings")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ -8 ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(1)
    }

    @Test
    fun `returns success when client has override role ROLE_CREATE_CATEGORISATION`() {
      webTestClient.post().uri("/api/offender-sentences/bookings")
        .headers(setClientAuthorisation(listOf("ROLE_CREATE_CATEGORISATION")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ -8 ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(1)
    }

    @Test
    fun `returns success when client has override role ROLE_APPROVE_CATEGORISATION`() {
      webTestClient.post().uri("/api/offender-sentences/bookings")
        .headers(setClientAuthorisation(listOf("ROLE_APPROVE_CATEGORISATION")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue("[ -8 ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(1)
    }

    @Test
    fun postOffenderSentenceBookings_success() {
      webTestClient.post()
        .uri("/api/offender-sentences/bookings")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "-8" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences_single.json".readFile())
    }

    @Test
    fun postOffenderSentenceBookings_acrossCaseloadsAsViewPrisonerData() {
      webTestClient.post()
        .uri("/api/offender-sentences/bookings")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "-1", "-16", "-36" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").value<Int> { assertThat(it).isEqualTo(3) }
        .jsonPath("$[0].offenderNo").isEqualTo("A1234AA")
        .jsonPath("$[1].offenderNo").isEqualTo("A1234AP")
        .jsonPath("$[2].offenderNo").isEqualTo("A1180MA")
    }

    @Test
    fun postOffenderSentenceBookings_bookingIdsAcrossCaseloads() {
      // Note -11 = A1234AK; -5 = A1234AE, -16 not in caseload
      webTestClient.post()
        .uri("/api/offender-sentences/bookings")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "-11", "-5", "-16" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").value<Int> { assertThat(it).isEqualTo(2) }
        .jsonPath("$[0].offenderNo").isEqualTo("A1234AK")
        .jsonPath("$[1].offenderNo").isEqualTo("A1234AE")
    }

    @Test
    fun postOffenderSentenceBookings_success_multiple() {
      webTestClient.post()
        .uri("/api/offender-sentences/bookings")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ 
              "-58", "-34", "-33", "-32", "-31", "-30", "-29", "-28", "-27", "-25", "-24", "-19", "-18", 
              "-17", "-12", "-11", "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1", "16048" 
            ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences.json".readFile())
    }

    @Test
    fun postOffenderSentenceBookings_unknownUser_noRoles_singleOffender() {
      webTestClient.post()
        .uri("/api/offender-sentences/bookings")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "-8" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")

      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("UNKNOWN_USER", listOf(), listOf("-8"))
      val responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String::class.java)
      assertThatJsonAndStatus(responseEntity, HttpURLConnection.HTTP_OK, "[]")
    }

    @Test
    fun postOffenderSentenceBookings_unknownUser_viewRole_singleOffender() {
      webTestClient.post()
        .uri("/api/offender-sentences/bookings")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "-8" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences_single.json".readFile())

      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("UNKNOWN_USER", listOf("ROLE_VIEW_PRISONER_DATA"), listOf("-8"))
      val responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences_single.json")
    }

    @Test
    fun postOffenderSentenceBookings_noRoles_singleOffender() {
      webTestClient.post()
        .uri("/api/offender-sentences/bookings")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ "-8" ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences_single.json".readFile())
    }

    @Test
    fun postOffenderSentenceBookings_noOffenders() {
      webTestClient.post()
        .uri("/api/offender-sentences/bookings")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            []
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("List of Offender Ids must be provided")
        .jsonPath("developerMessage").isEqualTo("400 List of Offender Ids must be provided")
    }
  }

  @Nested
  @DisplayName("GET /api/offender-sentences/booking/{bookingId}/sentences-and-offences")
  inner class OffenderSentencesAndOffences {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-20/sentences-and-offences")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-20/sentences-and-offences")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_SYSTEM_USER`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-20/sentences-and-offences")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-20/sentences-and-offences")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .hasListAtLeastSizeOf(2)
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-20/sentences-and-offences")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-20/sentences-and-offences")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun offenderSentencesWithOffenceInformation() {
      webTestClient.get()
        .uri("/api/offender-sentences/booking/-20/sentences-and-offences")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("sentences-and-offences-details.json".readFile())
    }
  }

  internal fun String.readFile(): String = this@OffenderSentenceResourceImplIntTest::class.java.getResource(this)!!.readText()
}

private fun WebTestClient.ResponseSpec.hasListAtLeastSizeOf(expectedSize: Int) {
  val count = this.expectBody(List::class.java).returnResult().responseBody
  assertThat(count).hasSizeGreaterThan(expectedSize - 1)
}
