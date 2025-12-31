package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.joda.time.LocalDate
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.json.JsonCompareMode
import uk.gov.justice.hmpps.prison.api.model.IncidentTypeConfiguration
import uk.gov.justice.hmpps.prison.api.model.questionnaire.AnswerRequest
import uk.gov.justice.hmpps.prison.api.model.questionnaire.CreateIncidentTypeConfigurationRequest
import uk.gov.justice.hmpps.prison.api.model.questionnaire.PrisonerRole
import uk.gov.justice.hmpps.prison.api.model.questionnaire.PrisonerRoleRequest
import uk.gov.justice.hmpps.prison.api.model.questionnaire.QuestionRequest
import uk.gov.justice.hmpps.prison.api.model.questionnaire.UpdateIncidentTypeConfigurationRequest

@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class IncidentsResourceTest : ResourceTest() {

  @DisplayName("GET /api/incidents/{incidentId}")
  @Nested
  @Order(1)
  inner class RetrieveIncidentsTest {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/incidents/-1")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no authorised role`() {
      webTestClient.get().uri("/api/incidents/-4")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has authorised role`() {
      webTestClient.get().uri("/api/incidents/-1")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_INCIDENTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("responses.length()").isEqualTo(19)
        .jsonPath("parties.length()").isEqualTo(6)
        .jsonPath("incidentCaseId").isEqualTo(-1)
        .jsonPath("incidentTitle").isEqualTo("Big Fight")
        .jsonPath("incidentType").isEqualTo("ASSAULT")
    }

    @Test
    fun `returns success when no parties`() {
      webTestClient.get().uri("/api/incidents/-4")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_INCIDENTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("incidentCaseId").isEqualTo(-4)
        .jsonPath("incidentTitle").isEqualTo("Medium sized fight")
        .jsonPath("parties").doesNotExist()
    }
  }

  @DisplayName("GET /api/incidents/configuration")
  @Nested
  @Order(2)
  inner class RetrieveIncidentTypeConfigurationTest {

    @Nested
    inner class Security {
      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.get().uri("/api/incidents/configuration")
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class Validation {
      @Test
      fun `returns success when client has a token`() {
        webTestClient.get().uri("/api/incidents/configuration")
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 404 when incorrect incident type selected`() {
        webTestClient.get().uri("/api/incidents/configuration?incident-type=XXXX")
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().is4xxClientError
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      fun `returns success when individual incident type selected`() {
        webTestClient.get().uri("/api/incidents/configuration?incident-type=ASSAULT")
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
            [
              {
                "incidentType": "ASSAULT",
                "incidentTypeDescription": "ASSAULTS",
                "prisonerRoles": [
                  {
                    "prisonerRole": "FIGHT",
                    "singleRole": false,
                    "active": true
                  },
                  {
                    "prisonerRole": "VICT",
                    "singleRole": false,
                    "active": true
                  }
                ],
                "active": true
              }
            ]
              """,
            JsonCompareMode.LENIENT,
          )
      }
    }
  }

  @DisplayName("POST /api/incidents/configuration")
  @Nested
  @Order(3)
  inner class CreateNewIncidentTypeConfigurationTest {

    @Nested
    inner class Security {
      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.post().uri("/api/incidents/configuration")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class Validation {
      @Test
      fun `returns failure no body`() {
        webTestClient.post().uri("/api/incidents/configuration")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .bodyValue("{}")
          .exchange()
          .expectStatus().isBadRequest
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      fun `returns success when new incident type created`() {
        webTestClient.post().uri("/api/incidents/configuration")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .bodyValue(
            jsonString(
              CreateIncidentTypeConfigurationRequest(
                incidentType = "NEW_TYPE_1",
                incidentTypeDescription = "The new incident type",
                questions = listOf(
                  QuestionRequest(
                    code = 500000,
                    question = "Where did it happen?",
                    multipleAnswers = false,
                    answers = listOf(
                      AnswerRequest(
                        code = 500001,
                        response = "Cell",
                        dateRequired = false,
                        commentRequired = true,
                        nextQuestionCode = 500010,
                      ),
                      AnswerRequest(
                        code = 500002,
                        response = "Vehicle",
                        dateRequired = false,
                        commentRequired = false,
                        nextQuestionCode = 500020,
                      ),
                    ),
                  ),
                  QuestionRequest(
                    code = 500010,
                    question = "Type of cell:",
                    multipleAnswers = false,
                    answers = listOf(
                      AnswerRequest(
                        code = 500011,
                        response = "Normal",
                        dateRequired = false,
                        commentRequired = false,
                      ),
                      AnswerRequest(
                        code = 500012,
                        response = "Seg",
                        dateRequired = false,
                        commentRequired = false,
                      ),
                    ),
                  ),
                  QuestionRequest(
                    code = 500020,
                    question = "What was the reg of the vehicle and date taxed until?",
                    multipleAnswers = true,
                    answers = listOf(
                      AnswerRequest(
                        code = 500021,
                        response = "Registration",
                        dateRequired = false,
                        commentRequired = true,
                      ),
                      AnswerRequest(
                        code = 500022,
                        response = "Date taxed",
                        dateRequired = true,
                        commentRequired = false,
                      ),
                    ),
                  ),
                ),
                prisonerRoles = listOf(
                  PrisonerRoleRequest(
                    prisonerRole = PrisonerRole.PERPETRATOR,
                    singleRole = true,
                  ),
                  PrisonerRoleRequest(
                    prisonerRole = PrisonerRole.VICTIM,
                    singleRole = false,
                  ),
                ),
              ),
            ),
          )
          .exchange()
          .expectStatus().isCreated
          .expectBody().json(
            """
          {
            "incidentType": "NEW_TYPE_1",
            "incidentTypeDescription": "The new incident type",
            "active": true,
            "questions": [
              {
                "questionnaireQueId": 500000,
                "questionSeq": 1,
                "questionDesc": "Where did it happen?",
                "questionListSeq": 1,
                "questionActiveFlag": true,
                "multipleAnswerFlag": false,
                "answers": [
                  {
                    "questionnaireAnsId": 500001,
                    "answerSeq": 1,
                    "answerDesc": "Cell",
                    "answerListSeq": 1,
                    "answerActiveFlag": true,
                    "dateRequiredFlag": false,
                    "commentRequiredFlag": true,
                    "nextQuestionnaireQueId": 500010
                  },
                  {
                    "questionnaireAnsId": 500002,
                    "answerSeq": 2,
                    "answerDesc": "Vehicle",
                    "answerListSeq": 2,
                    "answerActiveFlag": true,
                    "dateRequiredFlag": false,
                    "commentRequiredFlag": false,
                    "nextQuestionnaireQueId": 500020
                  }
                ]
              },
              {
                "questionnaireQueId": 500010,
                "questionSeq": 2,
                "questionDesc": "Type of cell:",
                "questionListSeq": 2,
                "questionActiveFlag": true,
                "multipleAnswerFlag": false,
                "answers": [
                  {
                    "questionnaireAnsId": 500011,
                    "answerSeq": 1,
                    "answerDesc": "Normal",
                    "answerListSeq": 1,
                    "answerActiveFlag": true,
                    "dateRequiredFlag": false,
                    "commentRequiredFlag": false
                  },
                  {
                    "questionnaireAnsId": 500012,
                    "answerSeq": 2,
                    "answerDesc": "Seg",
                    "answerListSeq": 2,
                    "answerActiveFlag": true,
                    "dateRequiredFlag": false,
                    "commentRequiredFlag": false
                  }
                ]
              },
              {
                "questionnaireQueId": 500020,
                "questionSeq": 3,
                "questionDesc": "What was the reg of the vehicle and date taxed until?",
                "questionListSeq": 3,
                "questionActiveFlag": true,
                "multipleAnswerFlag": true,
                "answers": [
                  {
                    "questionnaireAnsId": 500021,
                    "answerSeq": 1,
                    "answerDesc": "Registration",
                    "answerListSeq": 1,
                    "answerActiveFlag": true,
                    "dateRequiredFlag": false,
                    "commentRequiredFlag": true
                  },
                  {
                    "questionnaireAnsId": 500022,
                    "answerSeq": 2,
                    "answerDesc": "Date taxed",
                    "answerListSeq": 2,
                    "answerActiveFlag": true,
                    "dateRequiredFlag": true,
                    "commentRequiredFlag": false
                  }
                ]
              }
            ],
            "prisonerRoles": [
              {
                "prisonerRole": "PERP",
                "singleRole": true,
                "active": true
              },
              {
                "prisonerRole": "VICT",
                "singleRole": false,
                "active": true
              }
            ]
          }
              """,
            JsonCompareMode.LENIENT,
          )
      }
    }
  }

  @DisplayName("PUT /api/incidents/configuration/ASSAULT")
  @Nested
  @Order(4)
  inner class UpdateNewIncidentTypeConfigurationTest {

    @Nested
    inner class Security {
      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.put().uri("/api/incidents/configuration/ASSAULT")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class Validation {
      @Test
      fun `returns failure no body`() {
        webTestClient.put().uri("/api/incidents/configuration/ASSAULT")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .exchange()
          .expectStatus().isBadRequest
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      fun `returns success when incident type update`() {
        val incidentType = webTestClient.get().uri("/api/incidents/configuration?incident-type=ASSAULT")
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .exchange()
          .expectStatus().isOk
          .expectBodyList(ParameterizedTypeReference.forType<IncidentTypeConfiguration>(IncidentTypeConfiguration::class.java))
          .returnResult().responseBody!![0]!!

        webTestClient.put().uri("/api/incidents/configuration/ASSAULT")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .bodyValue(
            jsonString(
              UpdateIncidentTypeConfigurationRequest(
                incidentTypeDescription = "Changed incident type description",
                active = false,
                questions = incidentType.questions.map {
                  QuestionRequest(
                    code = it.questionnaireQueId,
                    question = it.questionDesc,
                    active = false,
                    multipleAnswers = it.multipleAnswerFlag,
                    answers = it.answers.map { answer ->
                      AnswerRequest(
                        code = answer.questionnaireAnsId,
                        response = answer.answerDesc,
                        active = false,
                        dateRequired = answer.dateRequiredFlag,
                        commentRequired = answer.commentRequiredFlag,
                        nextQuestionCode = answer.nextQuestionnaireQueId,
                      )
                    },
                  )
                },
                prisonerRoles = listOf(
                  PrisonerRoleRequest(
                    prisonerRole = PrisonerRole.FIGHTER,
                    singleRole = true,
                    active = false,
                  ),
                  PrisonerRoleRequest(
                    prisonerRole = PrisonerRole.VICTIM,
                    singleRole = false,
                    active = false,
                  ),
                ),
              ),
            ),
          )
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
          {
            "incidentType": "ASSAULT",
            "incidentTypeDescription": "Changed incident type description",
            "active": false,
            "expiryDate": "${LocalDate.now().toString("yyyy-MM-dd")}",
            "prisonerRoles": [
              {
                "prisonerRole": "FIGHT",
                "singleRole": true,
                "active": false
              },
              {
                "prisonerRole": "VICT",
                "singleRole": false,
                "active": false
              }
            ]
          }
              """,
            JsonCompareMode.LENIENT,
          )
      }

      @Test
      fun `can resequence the questionnaire order`() {
        webTestClient.put().uri("/api/incidents/configuration/ASSAULT?resequenceQuestionnaires=true")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .bodyValue(
            jsonString(
              UpdateIncidentTypeConfigurationRequest(
                incidentTypeDescription = "ZZ Assault",
                active = true,
              ),
            ),
          )
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
              {
                "incidentType": "ASSAULT",
                "incidentTypeDescription": "ZZ Assault",
                "active": true
              }
              """,
            JsonCompareMode.LENIENT,
          )

        assertThat(
          webTestClient.get().uri("/api/incidents/configuration")
            .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<List<IncidentTypeConfiguration>>() {})
            .returnResult()
            .responseBody!!.last().incidentTypeDescription,
        ).isEqualTo("ZZ Assault")

        webTestClient.put().uri("/api/incidents/configuration/ASSAULT?resequenceQuestionnaires=true")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
          .bodyValue(
            jsonString(
              UpdateIncidentTypeConfigurationRequest(
                incidentTypeDescription = "A1 Assault",
              ),
            ),
          )
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
              {
                "incidentType": "ASSAULT",
                "incidentTypeDescription": "A1 Assault",
                "active": true
              }
              """,
            JsonCompareMode.LENIENT,
          )

        assertThat(
          webTestClient.get().uri("/api/incidents/configuration")
            .headers(setClientAuthorisation(listOf("PRISON_API__INCIDENT_TYPE_CONFIGURATION_RW")))
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<List<IncidentTypeConfiguration>>() {})
            .returnResult()
            .responseBody!!.first().incidentTypeDescription,
        ).isEqualTo("A1 Assault")
      }
    }
  }
}
