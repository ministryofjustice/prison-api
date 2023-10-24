package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import java.net.HttpURLConnection

class OffenderSentenceResourceImplIntTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/offender-sentences")
  inner class GetOffenderSentences {
    @Test
    fun offenderSentence_success() {
      webTestClient.get()
        .uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences.json".readFile())
    }

    @Test
    fun offenderSentence_unknownUser_noRoles_onlyAgency() {
      webTestClient.get()
        .uri("/api/offender-sentences?agencyId=LEI")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
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
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
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
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences.json".readFile())
    }

    @Test
    fun offenderSentence_unknownUser_viewRole_singleOffender() {
      webTestClient.get()
        .uri("/api/offender-sentences?offenderNo=A1234AH")
        .headers(setAuthorisation("UNKNOWN_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
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
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("offender_sentences.json".readFile())
    }

    @Test
    fun offenderSentence_noRoles_singleOffender() {
      webTestClient.get()
        .uri("/api/offender-sentences?offenderNo=A1234AH")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
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
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
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
    fun offenderSentenceTerms_with_filterParam_success() {
      webTestClient.get()
        .uri { uriBuilder ->
          uriBuilder.path("/api/offender-sentences/booking/-31/sentenceTerms")
            .queryParam("filterBySentenceTermCodes", "IMP", "LIC")
            .build()
        }
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
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
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
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
        .jsonPath("$.length()").value<Int> { Assertions.assertThat(it).isEqualTo(3) }
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
        .jsonPath("$.length()").value<Int> { Assertions.assertThat(it).isEqualTo(2) }
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
  @DisplayName("GET /api/offender-sentences/bookings/{bookingId}/sentences-and-offences")
  inner class OffenderSentencesAndOffences {
    @Test
    fun offenderSentencesWithOffenceInformation() {
      webTestClient.get()
        .uri("/api/offender-sentences/booking/-20/sentences-and-offences")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("sentences-and-offences-details.json".readFile())
    }
  }

  @Nested
  @DisplayName("POST /api/offender-sentences/home-detention-curfews/latest")
  inner class LatestHDC {
    @Test
    fun postHdcLatestStatus_success_multiple() {
      webTestClient.post()
        .uri("/api/offender-sentences/home-detention-curfews/latest")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_SYSTEM_USER")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            [ -1, -2, -3 ]
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").value<Int> { Assertions.assertThat(it).isEqualTo(3) }
        .jsonPath("$[0].bookingId").isEqualTo("-3")
        .jsonPath("$[1].bookingId").isEqualTo("-2")
        .jsonPath("$[2].bookingId").isEqualTo("-1")
    }
  }

  @Nested
  @DisplayName("PUT /api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
  inner class SetHDCChecksPassedFlag {

    @AfterEach
    private fun resetTestData() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `should return success when user has SYSTEM_USER role`() {
      webTestClient.put()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
              {
                "passed": true,
                "date": "2018-12-31"
              }
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `should return 403 when user does not have required role`() {
      webTestClient.put()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
              {
                "passed": true,
                "date": "2018-12-31"
              }
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when user has MAINTAIN_HDC role`() {
      webTestClient.put()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
              {
                "passed": true,
                "date": "2018-12-31"
              }
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  @DisplayName("DELETE /api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
  inner class ClearHDCChecksPassedFlag {
    @Test
    fun `should return success when user has SYSTEM_USER role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `should return 403 when user does not have required role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when user has MAINTAIN_HDC role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  @DisplayName("PUT /api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/approval-status")
  inner class SetHDCApprovalStatus {

    @AfterEach
    private fun resetTestData() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-5/home-detention-curfews/latest/approval-status")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-5/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `should return success when user has SYSTEM_USER role`() {
      // Set checks first
      webTestClient.put()
        .uri("/api/offender-sentences/booking/-5/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
              {
                "passed": true,
                "date": "2018-12-30"
              }
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent

      webTestClient.put()
        .uri("/api/offender-sentences/booking/-5/home-detention-curfews/latest/approval-status")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
              {
                "approvalStatus": "APPROVED",
                "date": "2018-12-31"
              }
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `should return 403 when user does not have required role`() {
      webTestClient.put()
        .uri("/api/offender-sentences/booking/-5/home-detention-curfews/latest/approval-status")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
              {
                "approvalStatus": "APPROVED",
                "date": "2018-12-31"
              }
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when user has MAINTAIN_HDC role`() {
      // Set checks first
      webTestClient.put()
        .uri("/api/offender-sentences/booking/-5/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
              {
                "passed": true,
                "date": "2018-12-30"
              }
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent

      webTestClient.put()
        .uri("/api/offender-sentences/booking/-5/home-detention-curfews/latest/approval-status")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
              {
                "approvalStatus": "APPROVED",
                "date": "2018-12-31"
              }
          """,
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  @DisplayName("DELETE /api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/approval-status")
  inner class ClearHDCApprovalStatus {
    @Test
    fun `should return success when user has SYSTEM_USER role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/approval-status")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `should return 403 when user does not have required role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/approval-status")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when user has MAINTAIN_HDC role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/approval-status")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  internal fun String.readFile(): String = this@OffenderSentenceResourceImplIntTest::class.java.getResource(this).readText()
}
