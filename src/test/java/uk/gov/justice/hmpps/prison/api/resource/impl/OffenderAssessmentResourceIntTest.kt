package uk.gov.justice.hmpps.prison.api.resource.impl

import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import uk.gov.justice.hmpps.prison.util.Extractors
import uk.gov.justice.hmpps.prison.util.Extractors.extractString
import java.time.LocalDate
import java.util.Date

class OffenderAssessmentResourceIntTest : ResourceTest() {
  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @Nested
  @DisplayName("PUT /api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}")
  inner class NextReviewDate {
    @Test
    fun testSystemUserCanUpdateCategoryNextReviewDate() {
      val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "-1",
        "2018-06-05",
      )
      assertThatStatus(response, OK.value())
    }

    @Test
    fun testNormalUserCannotUpdateCategoryNextReviewDate() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "-1",
        "2018-06-05",
      )
      assertThatStatus(response, FORBIDDEN.value())
    }

    @Test
    fun testUpdateCategoryNextReviewDateActiveCategorisationDoesNotExist() {
      val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "-56",
        "2018-06-05",
      )
      assertThatStatus(response, NOT_FOUND.value())
    }
  }

  @Nested
  @DisplayName("PUT /api/offender-assessments/category/{bookingId}/inactive")
  inner class Inactive {
    @Test
    fun testSystemUserCanUpdateCategorySetActiveInactive() {
      val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
      val httpEntity = createHttpEntity(token, null)

      // choose a booking that doesnt actually have any active
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/{bookingId}/inactive",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "-34",
      )
      assertThatStatus(response, OK.value())
    }

    @Test
    fun testSystemUserCanUpdateCategorySetPendingInactive() {
      val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
      val httpEntity = createHttpEntity(token, null)

      // choose a booking that doesnt actually have any active
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/{bookingId}/inactive?status=PENDING",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "-31",
      )
      assertThatStatus(response, OK.value())
    }

    @Test
    fun testSetPendingInactiveValidationError() {
      val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/{bookingId}/inactive?status=OTHER",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "-34",
      )
      assertThatStatus(response, BAD_REQUEST.value())
      assertThat(response.body).contains("Assessment status type is invalid: OTHER")
    }

    @Test
    fun testNormalUserCannotUpdateCategorySetInactive() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/{bookingId}/inactive",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "-1",
      )
      assertThatStatus(response, FORBIDDEN.value())
    }
  }

  @Nested
  @DisplayName("GET /api/offender-assessments/category/{agencyId}")
  inner class GetOffenderCategorisations {

    @Nested
    inner class Authorisation {

      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.get().uri("/api/offender-assessments/category/LEI?type=UNCATEGORISED")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `should return 404 if client does not have authorised role`() {
        webTestClient.get().uri("/api/offender-assessments/category/LEI?type=UNCATEGORISED")
          .headers(setClientAuthorisation(listOf("")))
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `should return success if has VIEW_ASSESSMENTS override role`() {
        webTestClient.get().uri("/api/offender-assessments/category/LEI?type=UNCATEGORISED")
          .headers(setClientAuthorisation(listOf("VIEW_ASSESSMENTS")))
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `should return success if has SYSTEM_USER override role`() {
        webTestClient.get().uri("/api/offender-assessments/category/LEI?type=UNCATEGORISED")
          .headers(setClientAuthorisation(listOf("SYSTEM_USER")))
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 404 if user has no caseloads`() {
        webTestClient.get().uri("/api/offender-assessments/category/LEI?type=UNCATEGORISED")
          .headers(setAuthorisation("RO_USER", listOf("")))
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [LEI] not found.")
      }

      @Test
      fun `returns 404 if not in user caseload`() {
        webTestClient.get().uri("/api/offender-assessments/category/LEI?type=UNCATEGORISED")
          .headers(setAuthorisation("WAI_USER", listOf()))
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [LEI] not found.")
      }

      @Test
      fun `returns 404 if booking not found`() {
        webTestClient.get().uri("/api/offender-assessments/category/LEI?type=UNCATEGORISED")
          .headers(setAuthorisation("WAI_USER", listOf()))
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [LEI] not found.")
      }

      @Test
      fun `returns success if  in user caseload`() {
        webTestClient.get().uri("/api/offender-assessments/category/LEI?type=UNCATEGORISED")
          .headers(setAuthorisation("ITAG_USER", listOf()))
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.length()").isEqualTo(24)
      }
    }
  }

  @Nested
  @DisplayName("POST /api/offender-assessments/category/{agencyId}")
  inner class PostOffenderCategorisations {
    @Test
    fun testGetOffenderCategorisationsPost() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, listOf("-1", "-2", "-3", "-38", "-39", "-40", "-41"))
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/LEI?latest=false",
        POST,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, OK.value())
      assertThatJson(response.body).isArray().hasSize(1)
      assertThatJson(response.body).node("[0].bookingId").isEqualTo(JsonAssertions.value(-1))
    }

    @Test
    fun testGetOffenderCategorisationsSystem() {
      val token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, listOf("-1", "-2", "-3", "-38", "-39", "-40", "-41"))
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category?latest=false",
        POST,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, OK.value())
      assertThatJson(response.body).isArray().hasSize(6)
      assertThatJson(response.body).node("[0].bookingId").isEqualTo(JsonAssertions.value(-1))
      assertThatJson(response.body).node("[1].bookingId").isEqualTo(JsonAssertions.value(-3))
      assertThatJson(response.body).node("[2].bookingId").isEqualTo(JsonAssertions.value(-38))
      assertThatJson(response.body).node("[3].bookingId").isEqualTo(JsonAssertions.value(-39))
      assertThatJson(response.body).node("[4].bookingId").isEqualTo(JsonAssertions.value(-40))
      assertThatJson(response.body).node("[5].bookingId").isEqualTo(JsonAssertions.value(-41))
    }
  }

  @Nested
  @DisplayName("POST /api/offender-assessments/csra/rating")
  inner class CRSARating {
    @Test
    fun testGetCsraRatings() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, listOf("A1183JE", "A1234BB"))
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/csra/rating",
        POST,
        httpEntity,
        String::class.java,
      )
      assertThatJsonFileAndStatus(response, OK.value(), "csra_ratings.json")
    }

    @Test
    fun testGetCsraRatingsInvalidOffenderNos() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, listOf<Any>())
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/csra/rating",
        POST,
        httpEntity,
        String::class.java,
      )
      assertThatStatus(response, BAD_REQUEST.value())
      assertThatJson(response.body).node("userMessage").asString().contains("postOffenderAssessmentsCsraRatings.offenderList: must not be empty")
    }
  }

  @Nested
  @DisplayName("GET /api/offender-assessments/csra/{bookingId}/assessment/{assessmentSeq}")
  inner class CRSAAssessment {

    @Test
    fun testGetCsraAssessment() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/csra/-43/assessment/2",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatJsonFileAndStatus(response, OK.value(), "csra_assessment.json")
    }

    @Test
    fun testGetCsraAssessmentNotAccessibleWithoutPermissions() {
      val httpEntity = createHttpEntity(NORMAL_USER, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/csra/-43/assessment/2",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatStatus(response, NOT_FOUND.value())
      assertThatJson(response.body).node("userMessage").asString().contains("Offender booking with id -43 not found.")
    }

    @Test
    fun testGetCsraAssessmentInvalidBookingId() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/csra/-999/assessment/2",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatStatus(response, NOT_FOUND.value())
      assertThatJson(response.body).node("userMessage").asString().contains("Offender booking with id -999 not found.")
    }

    @Test
    fun testGetCsraAssessmentInvalidAssessmentSeq() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/csra/-43/assessment/200",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatStatus(response, NOT_FOUND.value())
      assertThatJson(response.body).node("userMessage").asString().contains("Csra assessment for booking -43 and sequence 200 not found.")
    }
  }

  @Nested
  @DisplayName("GET /api/offender-assessments/csra/{offenderNo}")
  inner class CRSAOffenderAssessments {

    @Test
    fun testGetCsraAssessments() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/csra/A1183JE",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatJsonFileAndStatus(response, OK.value(), "csra_assessments_by_offender.json")
    }

    @Test
    fun testGetCsraAssessmentsNotAccessibleWithoutPermissions() {
      val httpEntity = createHttpEntity(NORMAL_USER, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/csra/A1183JE",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatStatus(response, NOT_FOUND.value())
      assertThatJson(response.body).node("userMessage").asString().contains("Offender booking with id -43 not found.")
    }

    @Test
    fun testGetCsraAssessmentsInvalidOffenderNo() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/csra/A1234BB",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatStatus(response, NOT_FOUND.value())
      assertThatJson(response.body).node("userMessage").asString().contains("Resource with id [A1234BB] not found.")
    }
  }

  @Nested
  @DisplayName("GET /api/offender-assessments/assessments")
  inner class CRSAAssessments {

    @Test
    fun testGetAssessments() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/assessments?offenderNo=A1234AD&latestOnly=false&activeOnly=false",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatJsonFileAndStatus(response, OK.value(), "assessments.json")
    }

    @Test
    fun testGetAssessmentsMostRecentTrue() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/assessments?offenderNo=A1234AD&latestOnly=false&activeOnly=false&mostRecentOnly=true",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatStatus(response, OK.value())
      assertThatJson(response.body).isArray().hasSize(1)
      assertThatJson(response.body).node("[0].assessmentSeq").isEqualTo(JsonAssertions.value(1))
    }

    @Test
    fun testGetAssessmentsMissingOffenderNo() {
      val httpEntity = createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/assessments?latestOnly=false&activeOnly=false",
        GET,
        httpEntity,
        String::class.java,
      )
      assertThatStatus(response, BAD_REQUEST.value())
      assertThatJson(response.body).node("userMessage").asString().contains("Required request parameter 'offenderNo' for method parameter type List is not present")
    }
  }

  @Nested
  @DisplayName("POST /api/offender-assessments/category/categorise")
  inner class CreateCategorisation {

    @Nested
    inner class Authorisation {

      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.post().uri("/api/offender-assessments/category/categorise")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
              "bookingId" : -35,
              "category": "D",
              "committee": "RECP"
            }
            """,
          )
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `should return 403 if does not have authorised role`() {
        webTestClient.post().uri("/api/offender-assessments/category/categorise")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -35,
              "category": "D",
              "committee": "RECP"
            }
            """,
          )
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 404 if user has no caseloads`() {
        webTestClient.post().uri("/api/offender-assessments/category/categorise")
          .headers(setAuthorisation("RO_USER", listOf("CREATE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -35,
              "category": "D",
              "committee": "RECP"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -35 not found.")
      }

      @Test
      fun `returns 404 if not in user caseload`() {
        webTestClient.post().uri("/api/offender-assessments/category/categorise")
          .headers(setAuthorisation("WAI_USER", listOf("CREATE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -35,
              "category": "D",
              "committee": "RECP"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -35 not found.")
      }

      @Test
      fun `returns 404 if booking not found`() {
        webTestClient.post().uri("/api/offender-assessments/category/categorise")
          .headers(setAuthorisation("WAI_USER", listOf("CREATE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -99999,
              "category": "D",
              "committee": "RECP"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
      }

      @Test
      fun `returns success if in user caseload and has correct CREATE_CATEGORISATION role`() {
        webTestClient.post().uri("/api/offender-assessments/category/categorise")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setAuthorisation("ITAG_USER", listOf("CREATE_CATEGORISATION")))
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -35,
              "category": "D",
              "committee": "RECP"
            }
            """,
          )
          .exchange()
          .expectStatus().isCreated

        resetCreatedCategorisation()
      }

      @Test
      fun `returns success if in user caseload and has correct CREATE_RECATEGORISATION role`() {
        webTestClient.post().uri("/api/offender-assessments/category/categorise")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setAuthorisation("ITAG_USER", listOf("CREATE_RECATEGORISATION")))
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -35,
              "category": "D",
              "committee": "RECP"
            }
            """,
          )
          .exchange()
          .expectStatus().isCreated

        resetCreatedCategorisation()
      }
    }

    @Test
    fun testCreateCategorisation() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_CREATE)
      val body = createHttpEntity(
        token,
        CategorisationDetail.builder()
          .bookingId(-35L)
          .category("D")
          .nextReviewDate(LocalDate.of(2020, 3, 16))
          .committee("RECP")
          .comment("test comment")
          .placementAgencyId("SYI")
          .build(),
      )
      try {
        val response = testRestTemplate.exchange(
          "/api/offender-assessments/category/categorise",
          POST,
          body,
          object : ParameterizedTypeReference<String>() {},
        )
        assertThatStatus(response, CREATED.value())
        val results = jdbcTemplate.queryForList(
          "SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -35 ORDER BY ASSESSMENT_SEQ DESC",
        )
        assertThat(results).asList()
          .extracting(
            extractString("CALC_SUP_LEVEL_TYPE"),
            extractString("ASSESS_COMMENT_TEXT"),
            extractString("ASSESS_COMMITTE_CODE"),
            extractString("PLACE_AGY_LOC_ID"),
          )
          .contains(Tuple.tuple("D", "test comment", "RECP", "SYI"))
        assertThat(results[0]["NEXT_REVIEW_DATE"] as Date).isCloseTo("2020-03-16", 1000L)
      } finally {
        // Restore db change as cannot rollback server transaction in client
        resetCreatedCategorisation()
      }
    }

    @Test
    fun testCreateCategorisationJSRValidation() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_CREATE)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/categorise",
        POST,
        createHttpEntity(
          token,
          CategorisationDetail.builder()
            .comment(StringUtils.repeat("B", 4001))
            .placementAgencyId("RUBBISH")
            .build(),
        ),
        object : ParameterizedTypeReference<String>() {},
      )

      assertThatStatus(response, BAD_REQUEST.value())
      val body = response.body
      assertThatJson(body).node("userMessage").asString().contains("bookingId must be provided")
      assertThatJson(body).node("userMessage").asString().contains("category must be provided")
      assertThatJson(body).node("userMessage").asString().contains("committee must be provided")
      assertThatJson(body).node("userMessage").asString().contains("Agency id must be a maximum of 6 characters")
      assertThatJson(body).node("userMessage").asString().contains("Comment text must be a maximum of 4000 characters")
    }

    @Test
    fun testCreateCategorisationAgencyValidation() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_CREATE)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/categorise",
        POST,
        createHttpEntity(
          token,
          CategorisationDetail.builder()
            .bookingId(-38L)
            .category("C")
            .committee("OCA")
            .placementAgencyId("WRONG")
            .build(),
        ),
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, BAD_REQUEST.value())
      assertThatJson(response.body).node("userMessage").asString().contains("Placement agency id not recognised.")
    }

    private fun resetCreatedCategorisation() =
      jdbcTemplate.update("DELETE FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -35 AND CALC_SUP_LEVEL_TYPE = 'D'")
  }

  @Nested
  @DisplayName("PUT /api/offender-assessments/category/categorise")
  inner class UpdateCategorisation {

    @Nested
    inner class Authorisation {

      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.put().uri("/api/offender-assessments/category/categorise")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "comment": "test comment"
            }
            """,
          )
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `should return 403 if does not have authorised role`() {
        webTestClient.put().uri("/api/offender-assessments/category/categorise")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "comment": "test comment"
            }
            """,
          )
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 if user has no role`() {
        webTestClient.put().uri("/api/offender-assessments/category/categorise")
          .headers(setAuthorisation("ITAG_USER", listOf("")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "comment": "test comment"
            }
            """,
          )
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 404 if user has no caseloads`() {
        webTestClient.put().uri("/api/offender-assessments/category/categorise")
          .headers(setAuthorisation("RO_USER", listOf("CREATE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "comment": "test comment"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -38 not found.")
      }

      @Test
      fun `returns 404 if not in user caseload`() {
        webTestClient.put().uri("/api/offender-assessments/category/categorise")
          .headers(setAuthorisation("WAI_USER", listOf("CREATE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "comment": "test comment"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -38 not found.")
      }

      @Test
      fun `returns 404 if booking not found`() {
        webTestClient.put().uri("/api/offender-assessments/category/categorise")
          .headers(setAuthorisation("WAI_USER", listOf("CREATE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -99999,
              "assessmentSeq": 3,
              "comment": "test comment"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
      }

      @Test
      fun `returns success if in user caseload and has correct CREATE_CATEGORISATION role`() {
        webTestClient.put().uri("/api/offender-assessments/category/categorise")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setAuthorisation("ITAG_USER", listOf("CREATE_CATEGORISATION")))
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "comment": "test comment"
            }
            """,
          )
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns success if in user caseload and has correct CREATE_RECATEGORISATION role`() {
        webTestClient.put().uri("/api/offender-assessments/category/categorise")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setAuthorisation("ITAG_USER", listOf("CREATE_RECATEGORISATION")))
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "comment": "test comment"
            }
            """,
          )
          .exchange()
          .expectStatus().isOk
      }
    }

    @Test
    fun testUpdateCategorisation() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_CREATE)
      val httpEntity = createHttpEntity(
        token,
        CategorisationUpdateDetail.builder()
          .bookingId(-38L)
          .assessmentSeq(3)
          .category("C")
          .nextReviewDate(LocalDate.of(2021, 3, 16))
          .committee("OCA")
          .comment("test comment")
          .build(),
      )
      try {
        val response = testRestTemplate.exchange(
          "/api/offender-assessments/category/categorise",
          PUT,
          httpEntity,
          object : ParameterizedTypeReference<String>() {},
        )
        assertThatStatus(response, OK.value())
        val results = jdbcTemplate.queryForList(
          "SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -38 AND ASSESSMENT_SEQ = 3",
        )
        assertThat(results).asList()
          .extracting(
            Extractors.extractInteger("ASSESSMENT_SEQ"),
            extractString("CALC_SUP_LEVEL_TYPE"),
            extractString("ASSESS_COMMENT_TEXT"),
            extractString("ASSESS_COMMITTE_CODE"),
          )
          .containsExactly(Tuple.tuple(3, "C", "test comment", "OCA"))
        assertThat(results[0]["NEXT_REVIEW_DATE"] as Date).isCloseTo("2021-03-16", 1000L)
      } finally {
        // Restore cat and nextReviewDate as cannot rollback transaction in client
        val response2 = testRestTemplate.exchange(
          "/api/offender-assessments/category/categorise",
          PUT,
          createHttpEntity(
            token,
            CategorisationUpdateDetail.builder()
              .bookingId(-38L)
              .assessmentSeq(3)
              .category("B")
              .nextReviewDate(LocalDate.of(2019, 6, 8))
              .build(),
          ),
          object : ParameterizedTypeReference<String>() {},
        )
        assertThat(response2.statusCode.value()).isEqualTo(OK.value())
      }
    }

    @Test
    fun testUpdateCategorisationAnnotationValidation() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_CREATE)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/categorise",
        PUT,
        createHttpEntity(
          token,
          CategorisationUpdateDetail.builder()
            .comment(StringUtils.repeat("A", 4001))
            .build(),
        ),
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, BAD_REQUEST.value())
      val body = response.body
      assertThatJson(body).node("userMessage").asString().contains("bookingId must be provided")
      assertThatJson(body).node("userMessage").asString().contains("Sequence number must be provided")
      assertThatJson(body).node("userMessage").asString().contains("Comment text must be a maximum of 4000 characters")
    }

    @Test
    fun testUpdateCategorisationReferenceDataValidation1() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_CREATE)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/categorise",
        PUT,
        createHttpEntity(
          token,
          CategorisationUpdateDetail.builder()
            .bookingId(-38L)
            .assessmentSeq(3)
            .category("INVALID")
            .committee("OCA")
            .build(),
        ),
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, BAD_REQUEST.value())
      assertThatJson(response.body).node("userMessage").isEqualTo("Category not recognised.")
    }

    @Test
    fun testUpdateCategorisationReferenceDataValidation2() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_CREATE)
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/categorise",
        PUT,
        createHttpEntity(
          token,
          CategorisationUpdateDetail.builder()
            .bookingId(-38L)
            .assessmentSeq(3)
            .category("C")
            .committee("INVALID")
            .build(),
        ),
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, BAD_REQUEST.value())
      assertThatJson(response.body).node("userMessage").isEqualTo("Committee Code not recognised.")
    }
  }

  @Nested
  @DisplayName("PUT /api/offender-assessments/category/approve")
  inner class ApproveCategorisation {

    @Nested
    inner class Authorisation {

      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.put().uri("/api/offender-assessments/category/approve")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
              "bookingId" : -34,
              "assessmentSeq": 1,
              "category": "D",
              "approvedCategoryComment": "approved",
              "evaluationDate": "2019-03-21",
              "reviewCommitteeCode": "GOV"
            }
            """,
          )
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `should return 403 if does not have authorised role`() {
        webTestClient.put().uri("/api/offender-assessments/category/approve")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -34,
              "assessmentSeq": 1,
              "category": "D",
              "approvedCategoryComment": "approved",
              "evaluationDate": "2019-03-21",
              "reviewCommitteeCode": "GOV"
            }
            """,
          )
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `should return success if has MAINTAIN_ASSESSMENTS override role`() {
        webTestClient.put().uri("/api/offender-assessments/category/approve")
          .headers(setClientAuthorisation(listOf("MAINTAIN_ASSESSMENTS")))
          .accept(MediaType.APPLICATION_JSON)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
              "bookingId" : -34,
              "assessmentSeq": 1,
              "category": "D",
              "approvedCategoryComment": "approved",
              "evaluationDate": "2019-03-21",
              "reviewCommitteeCode": "GOV"
            }
            """,
          )
          .exchange()
          .expectStatus().isCreated

        resetApprovedCategorisation()
      }

      @Test
      fun `should return success if has SYSTEM_USER override role`() {
        webTestClient.put().uri("/api/offender-assessments/category/approve")
          .headers(setClientAuthorisation(listOf("SYSTEM_USER")))
          .accept(MediaType.APPLICATION_JSON)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
              "bookingId" : -34,
              "assessmentSeq": 1,
              "category": "D",
              "approvedCategoryComment": "approved",
              "evaluationDate": "2019-03-21",
              "reviewCommitteeCode": "GOV"
            }
            """,
          )
          .exchange()
          .expectStatus().isCreated

        resetApprovedCategorisation()
      }

      @Test
      fun `returns 403 if user has no role`() {
        webTestClient.put().uri("/api/offender-assessments/category/approve")
          .headers(setAuthorisation("ITAG_USER", listOf("")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -34,
              "assessmentSeq": 1,
              "category": "D",
              "approvedCategoryComment": "approved",
              "evaluationDate": "2019-03-21",
              "reviewCommitteeCode": "GOV"
            }
            """,
          )
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 404 if user has no caseloads`() {
        webTestClient.put().uri("/api/offender-assessments/category/approve")
          .headers(setAuthorisation("RO_USER", listOf("APPROVE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -34,
              "assessmentSeq": 1,
              "category": "D",
              "approvedCategoryComment": "approved",
              "evaluationDate": "2019-03-21",
              "reviewCommitteeCode": "GOV"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -34 not found.")
      }

      @Test
      fun `returns 404 if not in user caseload`() {
        webTestClient.put().uri("/api/offender-assessments/category/approve")
          .headers(setAuthorisation("WAI_USER", listOf("APPROVE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -34,
              "assessmentSeq": 1,
              "category": "D",
              "approvedCategoryComment": "approved",
              "evaluationDate": "2019-03-21",
              "reviewCommitteeCode": "GOV"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -34 not found.")
      }

      @Test
      fun `returns 404 if booking not found`() {
        webTestClient.put().uri("/api/offender-assessments/category/approve")
          .headers(setAuthorisation("WAI_USER", listOf("APPROVE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -99999,
              "assessmentSeq": 1,
              "category": "D",
              "approvedCategoryComment": "approved",
              "evaluationDate": "2019-03-21",
              "reviewCommitteeCode": "GOV"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
      }

      @Test
      fun `returns success if in user caseload and has correct APPROVE_CATEGORISATION role`() {
        webTestClient.put().uri("/api/offender-assessments/category/approve")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setAuthorisation("ITAG_USER", listOf("APPROVE_CATEGORISATION")))
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -34,
              "assessmentSeq": 1,
              "category": "D",
              "approvedCategoryComment": "approved",
              "evaluationDate": "2019-03-21",
              "reviewCommitteeCode": "GOV"
            }
            """,
          )
          .exchange()
          .expectStatus().isCreated

        resetApprovedCategorisation()
      }
    }

    @Test
    fun testApproveCategorisation() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_APPROVE)
      val requestBody = createHttpEntity(
        token,
        CategoryApprovalDetail.builder()
          .bookingId(-34L)
          .assessmentSeq(1)
          .category("D")
          .evaluationDate(LocalDate.of(2019, 3, 21))
          .reviewCommitteeCode("GOV")
          .approvedCategoryComment("approved")
          .committeeCommentText("committee comment")
          .nextReviewDate(LocalDate.of(2020, 2, 17))
          .approvedPlacementAgencyId("BXI")
          .approvedPlacementText("placement comment")
          .build(),
      )
      try {
        val response = testRestTemplate.exchange(
          "/api/offender-assessments/category/approve",
          PUT,
          requestBody,
          object : ParameterizedTypeReference<String>() {},
        )
        assertThatStatus(response, CREATED.value())
        val results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -34 AND ASSESSMENT_SEQ = 1")
        assertThat(results).asList()
          .extracting(
            Extractors.extractInteger("ASSESSMENT_SEQ"),
            extractString("ASSESS_STATUS"),
            extractString("EVALUATION_RESULT_CODE"),
            extractString("COMMITTE_COMMENT_TEXT"),
            extractString("REVIEW_SUP_LEVEL_TYPE"),
            extractString("REVIEW_PLACE_AGY_LOC_ID"),
            extractString("REVIEW_PLACEMENT_TEXT"),
            extractString("REVIEW_SUP_LEVEL_TEXT"),
            extractString("REVIEW_COMMITTE_CODE"),
          )
          .containsExactly(Tuple.tuple(1, "A", "APP", "committee comment", "D", "BXI", "placement comment", "approved", "GOV"))
        assertThat(results[0]["EVALUATION_DATE"] as Date).isCloseTo("2019-03-21", 1000L)
        assertThat(results[0]["NEXT_REVIEW_DATE"] as Date).isCloseTo("2020-02-17", 1000L)
      } finally {
        // Restore db change as cannot rollback server transaction in client!
        resetApprovedCategorisation()
      }
    }

    @Test
    fun testApproveCategorisationCommitteCodeInvalid() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_APPROVE)
      val requestBody = createHttpEntity(
        token,
        CategoryApprovalDetail.builder()
          .bookingId(-38L)
          .assessmentSeq(3)
          .category("C")
          .evaluationDate(LocalDate.of(2020, 3, 21))
          .reviewCommitteeCode("INVALID")
          .build(),
      )
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/approve",
        PUT,
        requestBody,
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, BAD_REQUEST.value())
      val body = response.body
      assertThatJson(body).node("userMessage").asString().contains("Committee Code not recognised.")
    }

    @Test
    fun testApproveCategorisationPlacementAgencyInvalid() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_APPROVE)
      val requestBody = createHttpEntity(
        token,
        CategoryApprovalDetail.builder()
          .bookingId(-38L)
          .assessmentSeq(3)
          .category("C")
          .evaluationDate(LocalDate.of(2020, 3, 21))
          .reviewCommitteeCode("RECP")
          .approvedPlacementAgencyId("WRONG")
          .build(),
      )
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/approve",
        PUT,
        requestBody,
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, BAD_REQUEST.value())
      val body = response.body
      assertThatJson(body).node("userMessage").asString().contains("Review placement agency id not recognised.")
    }

    private fun resetApprovedCategorisation() =
      jdbcTemplate.update("UPDATE OFFENDER_ASSESSMENTS SET ASSESS_STATUS='P', EVALUATION_RESULT_CODE=null WHERE OFFENDER_BOOK_ID = -34 AND ASSESSMENT_SEQ = 1")
  }

  @Nested
  @DisplayName("PUT /api/offender-assessments/category/reject")
  inner class RejectCategorisation {

    @Nested
    inner class Authorisation {

      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.put().uri("/api/offender-assessments/category/reject")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "committeeCommentText": "committeeCommentText",
              "evaluationDate": "2020-06-15",
              "reviewCommitteeCode": "MED"
            }
            """,
          )
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `should return 403 if does not have authorised role`() {
        webTestClient.put().uri("/api/offender-assessments/category/reject")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "committeeCommentText": "committeeCommentText",
              "evaluationDate": "2020-06-15",
              "reviewCommitteeCode": "MED"
            }
            """,
          )
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `should return success if has MAINTAIN_ASSESSMENTS override role`() {
        webTestClient.put().uri("/api/offender-assessments/category/reject")
          .headers(setClientAuthorisation(listOf("MAINTAIN_ASSESSMENTS")))
          .accept(MediaType.APPLICATION_JSON)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "committeeCommentText": "committeeCommentText",
              "evaluationDate": "2020-06-15",
              "reviewCommitteeCode": "MED"
            }
            """,
          )
          .exchange()
          .expectStatus().isCreated
      }

      @Test
      fun `should return success if has SYSTEM_USER override role`() {
        webTestClient.put().uri("/api/offender-assessments/category/reject")
          .headers(setClientAuthorisation(listOf("SYSTEM_USER")))
          .accept(MediaType.APPLICATION_JSON)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "committeeCommentText": "committeeCommentText",
              "evaluationDate": "2020-06-15",
              "reviewCommitteeCode": "MED"
            }
            """,
          )
          .exchange()
          .expectStatus().isCreated
      }

      @Test
      fun `returns 403 if user has no role`() {
        webTestClient.put().uri("/api/offender-assessments/category/reject")
          .headers(setAuthorisation("ITAG_USER", listOf("")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "committeeCommentText": "committeeCommentText",
              "evaluationDate": "2020-06-15",
              "reviewCommitteeCode": "MED"
            }
            """,
          )
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 404 if user has no caseloads`() {
        webTestClient.put().uri("/api/offender-assessments/category/reject")
          .headers(setAuthorisation("RO_USER", listOf("APPROVE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "committeeCommentText": "committeeCommentText",
              "evaluationDate": "2020-06-15",
              "reviewCommitteeCode": "MED"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -38 not found.")
      }

      @Test
      fun `returns 404 if not in user caseload`() {
        webTestClient.put().uri("/api/offender-assessments/category/reject")
          .headers(setAuthorisation("WAI_USER", listOf("APPROVE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "committeeCommentText": "committeeCommentText",
              "evaluationDate": "2020-06-15",
              "reviewCommitteeCode": "MED"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -38 not found.")
      }

      @Test
      fun `returns 404 if booking not found`() {
        webTestClient.put().uri("/api/offender-assessments/category/reject")
          .headers(setAuthorisation("WAI_USER", listOf("APPROVE_CATEGORISATION")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -99999,
              "assessmentSeq": 3,
              "committeeCommentText": "committeeCommentText",
              "evaluationDate": "2020-06-15",
              "reviewCommitteeCode": "MED"
            }
            """,
          )
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
      }

      @Test
      fun `returns success if in user caseload and has correct APPROVE_CATEGORISATION role`() {
        webTestClient.put().uri("/api/offender-assessments/category/reject")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setAuthorisation("ITAG_USER", listOf("APPROVE_CATEGORISATION")))
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(
            """
            {
              "bookingId" : -38,
              "assessmentSeq": 3,
              "committeeCommentText": "committeeCommentText",
              "evaluationDate": "2020-06-15",
              "reviewCommitteeCode": "MED"
            }
            """,
          )
          .exchange()
          .expectStatus().isCreated
      }
    }

    @Test
    fun testRejectCategorisation() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_APPROVE)
      val httpEntity = createHttpEntity(
        token,
        CategoryRejectionDetail.builder()
          .bookingId(-38L)
          .assessmentSeq(3)
          .committeeCommentText("committeeCommentText")
          .evaluationDate(LocalDate.of(2020, 6, 15))
          .reviewCommitteeCode("MED")
          .build(),
      )
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/reject",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, CREATED.value())
      val results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -38 AND ASSESSMENT_SEQ = 3")
      assertThat(results).asList()
        .extracting(
          Extractors.extractInteger("ASSESSMENT_SEQ"),
          extractString("EVALUATION_RESULT_CODE"),
          extractString("REVIEW_COMMITTE_CODE"),
          extractString("COMMITTE_COMMENT_TEXT"),
        )
        .containsExactly(Tuple.tuple(3, "REJ", "MED", "committeeCommentText"))
      assertThat(results[0]["EVALUATION_DATE"] as Date).isCloseTo("2020-06-15", 1000L)
    }

    @Test
    fun testRejectCategorisationValidation() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_APPROVE)
      val httpEntity = createHttpEntity(
        token,
        CategoryRejectionDetail.builder()
          .committeeCommentText(StringUtils.repeat("B", 241))
          .build(),
      )
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/reject",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, BAD_REQUEST.value())
      val body = response.body
      assertThatJson(body).node("userMessage").asString().contains("bookingId must be provided")
      assertThatJson(body).node("userMessage").asString().contains("Sequence number must be provided")
      assertThatJson(body).node("userMessage").asString().contains("Comment text must be a maximum of 240 characters")
      assertThatJson(body).node("userMessage").asString().contains("Department must be provided")
      assertThatJson(body).node("userMessage").asString().contains("Date of rejection must be provided")
    }

    @Test
    fun testRejectCategorisationReferenceDataValidation() {
      val token = authTokenHelper.getToken(AuthToken.CATEGORISATION_APPROVE)
      val httpEntity = createHttpEntity(
        token,
        CategoryRejectionDetail.builder()
          .bookingId(-38L)
          .assessmentSeq(3)
          .committeeCommentText("committeeCommentText")
          .evaluationDate(LocalDate.of(2020, 6, 15))
          .reviewCommitteeCode("INVALID")
          .build(),
      )
      val response = testRestTemplate.exchange(
        "/api/offender-assessments/category/reject",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, BAD_REQUEST.value())
      assertThatJson(response.body).node("userMessage").isEqualTo("Committee Code not recognised.")
    }
  }
}