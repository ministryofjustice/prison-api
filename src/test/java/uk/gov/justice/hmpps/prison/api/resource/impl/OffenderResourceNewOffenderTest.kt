package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestToCreate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@WithMockUser
class OffenderResourceNewOffenderTest : ResourceTest() {

  @Nested
  @DisplayName("POST /offenders")
  inner class NewOffender {

    @Nested
    inner class Authorisation {
      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.post().uri("/api/offenders")
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(requestToCreate()).accept(MediaType.APPLICATION_JSON).exchange()
          .expectStatus().isUnauthorized
      }
      @Test
      fun `should return 403 when user does not have any roles`() {
        webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf()))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(requestToCreate()).accept(MediaType.APPLICATION_JSON).exchange()
          .expectStatus().isForbidden
      }
      @Test
      fun `should return 403 when user does not have required role`() {
        webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BANANAS")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(requestToCreate()).accept(MediaType.APPLICATION_JSON).exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    @DisplayName("when new offender is created")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Success {
      lateinit var existingPrisoner: InmateDetail

      @BeforeAll
      internal fun createExistingOffender() {
        existingPrisoner =
          webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).bodyValue(
              requestToCreate(
                lastName = "Lavigne",
                firstName = "Arnaud",
                dateOfBirth = LocalDate.parse("1985-05-13"),
                pncNumber = "94/65156B",
                croNumber = "16677/94P"
              )
            ).accept(MediaType.APPLICATION_JSON).exchange()
            .expectStatus().isOk.returnResult<InmateDetail>().responseBody.blockFirst()!!
      }

      @Test
      internal fun `will create a new offender`() {
        val createdPrisoner =
          webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).bodyValue(
              """
            {
              "pncNumber" : "07/61835T",
              "lastName" : "Dupont",
              "firstName" : "Lya",
              "middleName1" : "Sophia",
              "middleName2" : "Anna",
              "title" : "MS",
              "suffix" : "VIII",
              "dateOfBirth" : "1995-10-04",
              "gender" : "F",
              "ethnicity" : "M3",
              "croNumber" : "163984/21E"  
            }
              """.trimIndent()
            ).accept(MediaType.APPLICATION_JSON).exchange()
            .expectStatus().isOk.returnResult(InmateDetail::class.java).responseBody.blockFirst()!!

        assertThat(createdPrisoner.offenderNo).isNotNull

        val prisoner = webTestClient.get().uri("/api/offenders/${createdPrisoner.offenderNo}")
          .headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON).exchange()
          .expectStatus().isOk.returnResult(InmateDetail::class.java).responseBody.blockFirst()!!

        assertThat(prisoner.offenderNo).isEqualTo(createdPrisoner.offenderNo)
        assertThat(prisoner.lastName).isEqualTo("DUPONT")
        assertThat(prisoner.firstName).isEqualTo("LYA")
        assertThat(prisoner.middleName).isEqualTo("SOPHIA ANNA")
        assertThat(prisoner.age).isEqualTo(ChronoUnit.YEARS.between(prisoner.dateOfBirth, LocalDate.now()).toInt())
        assertThat(prisoner.isActiveFlag).isFalse
        assertThat(prisoner.offenderId).isNotNull
        assertThat(prisoner.offenderId).isEqualTo(prisoner.rootOffenderId)
        assertThat(prisoner.dateOfBirth).isEqualTo(LocalDate.parse("1995-10-04"))
        assertThat(prisoner.identifiers).anyMatch { it.type == "PNC" && it.value == "2007/61835T" }
        assertThat(prisoner.identifiers).anyMatch { it.type == "CRO" && it.value == "163984/21E" }
        assertThat(prisoner.physicalAttributes.sexCode).isEqualTo("F")
        assertThat(prisoner.physicalAttributes.gender).isEqualTo("Female")
        assertThat(prisoner.physicalAttributes.ethnicity).isEqualTo("Mixed: White and Asian")
        assertThat(prisoner.physicalAttributes.raceCode).isEqualTo("M3")

        assertThat(prisoner.bookingNo).isNull()
        assertThat(prisoner.inOutStatus).isNull()
        assertThat(prisoner.status).isNull()
      }

      @Test
      internal fun `will create a new offender with minimal data`() {
        webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).bodyValue(
            """
            {
              "lastName" : "Tremblay",
              "firstName" : "Lya",
              "dateOfBirth" : "1993-08-04",
              "gender" : "F"
            }
            """.trimIndent()
          ).accept(MediaType.APPLICATION_JSON).exchange()
          .expectStatus().isOk
      }

      @Test
      internal fun `prisoner with same name and dob allowed so long as pnc or cro is supplied`() {
        webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).bodyValue(
            """
            {
              "lastName" : "Lavigne",
              "firstName" : "Arnaud",
              "dateOfBirth" : "1985-05-13",
              "pncNumber" : "84/153133B",
              "gender" : "F"
            }
            """.trimIndent()
          ).accept(MediaType.APPLICATION_JSON).exchange()
          .expectStatus().isOk
      }

      @Test
      internal fun `prisoner with similar names and dob allowed`() {
        webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).bodyValue(
            """
            {
              "lastName" : "Lavigne",
              "firstName" : "Arnaud",
              "dateOfBirth" : "1985-05-14",
              "gender" : "F"
            }
            """.trimIndent()
          ).accept(MediaType.APPLICATION_JSON).exchange()
          .expectStatus().isOk
      }
    }

    @Nested
    @DisplayName("when new offender is rejected")
    inner class Failure {

      @Nested
      @DisplayName("when new request is not valid")
      @TestInstance(TestInstance.Lifecycle.PER_CLASS)
      inner class Validation {
        lateinit var existingPrisoner: InmateDetail

        @BeforeAll
        internal fun createExistingOffender() {
          existingPrisoner =
            webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
              .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).bodyValue(
                requestToCreate(
                  lastName = "Rochefort",
                  firstName = "Fabien",
                  dateOfBirth = LocalDate.parse("1987-09-12"),
                  pncNumber = "1988/69560X",
                  croNumber = "69560/88G"
                )
              ).accept(MediaType.APPLICATION_JSON).exchange()
              .expectStatus().isOk.returnResult<InmateDetail>().responseBody.blockFirst()!!
        }

        @Nested
        @DisplayName("PNC Number")
        inner class PNCNumber {
          @Test
          internal fun `must have a PNC in the correct format`() {
            expectBadRequest(requestToCreate(pncNumber = "AA/122343")).jsonPath("$.developerMessage")
              .isEqualTo("Field: pncNumber - PNC is not valid")
          }

          @Test
          internal fun `must not have the same PNC as an existing prisoner`() {
            expectBadRequest(requestToCreate(pncNumber = "88/69560X")).jsonPath("$.developerMessage")
              .isEqualTo("Prisoner with PNC 1988/69560X already exists with ID ${existingPrisoner.offenderNo}")
            expectBadRequest(requestToCreate(pncNumber = "1988/69560X")).jsonPath("$.developerMessage")
              .isEqualTo("Prisoner with PNC 1988/69560X already exists with ID ${existingPrisoner.offenderNo}")
          }
        }

        @Nested
        @DisplayName("Last name")
        inner class LastName {

          @Test
          internal fun `last name must only contain valid characters`() {
            expectBadRequest(requestToCreate(lastName = "@@@@@")).jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Last name is not valid")
          }

          @Test
          internal fun `last name can not be greater than 35 characters`() {
            expectBadRequest(requestToCreate(lastName = "A".repeat(36))).jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Value is too long: max length is 35")
          }

          @Test
          internal fun `last name can not be blank`() {
            expectBadRequest(requestToCreate(lastName = "  ")).jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Value cannot be blank")
          }
        }

        @Nested
        @DisplayName("First name")
        inner class FirstName {
          @Test
          internal fun `first name must only contain valid characters`() {
            expectBadRequest(requestToCreate(firstName = "@@@@@")).jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - First name is not valid")
          }

          @Test
          internal fun `first name can not be greater than 35 characters`() {
            expectBadRequest(requestToCreate(firstName = "A".repeat(36))).jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - Value is too long: max length is 35")
          }

          @Test
          internal fun `first name can not be blank`() {
            expectBadRequest(requestToCreate(firstName = "  ")).jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - Value cannot be blank")
          }
        }

        @Nested
        @DisplayName("Middle names")
        inner class MiddleNames {

          @Test
          internal fun `first middle name must only contain valid characters`() {
            expectBadRequest(requestToCreate(middleName1 = "@@@@@")).jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName1 - Middle name is not valid")
          }

          @Test
          internal fun `first middle can not be greater than 35 characters`() {
            expectBadRequest(requestToCreate(middleName1 = "A".repeat(36))).jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName1 - Value is too long: max length is 35")
          }

          @Test
          internal fun `second middle name must only contain valid characters`() {
            expectBadRequest(requestToCreate(middleName2 = "@@@@@")).jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName2 - Middle name 2 is not valid")
          }

          @Test
          internal fun `second middle can not be greater than 35 characters`() {
            expectBadRequest(requestToCreate(middleName2 = "A".repeat(36))).jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName2 - Value is too long: max length is 35")
          }
        }

        @Nested
        @DisplayName("Title")
        inner class Title {
          @Test
          internal fun `title can not be greater than 12 characters`() {
            expectBadRequest(requestToCreate(title = "A".repeat(13))).jsonPath("$.developerMessage")
              .isEqualTo("Field: title - Value is too long: max length is 12")
          }

          @Test
          internal fun `title must be one of the valid reference data types`() {
            expectNotFoundRequest(requestToCreate(title = "NONE")).jsonPath("$.developerMessage")
              .isEqualTo("Title NONE not found")
          }
        }

        @Nested
        @DisplayName("Suffix")
        inner class Suffix {

          @Test
          internal fun `suffix can not be greater than 12 characters`() {
            expectBadRequest(requestToCreate(suffix = "A".repeat(13))).jsonPath("$.developerMessage")
              .isEqualTo("Field: suffix - Value is too long: max length is 12")
          }

          @Test
          internal fun `suffix must be one of the valid reference data types`() {
            expectNotFoundRequest(requestToCreate(suffix = "NONE")).jsonPath("$.developerMessage")
              .isEqualTo("Suffix NONE not found")
          }
        }

        @Nested
        @DisplayName("Date of birth")
        inner class DateOfBirth {

          @Test
          internal fun `must be a valid date`() {
            expectBadRequest(
              """
            {
              "lastName" : "Tremblay",
              "firstName" : "Lya",
              "dateOfBirth" : "1993-02-31",
              "gender" : "F"
            }
              """.trimIndent()

            ).jsonPath("$.developerMessage")
              .isEqualTo("Malformed request")
          }

          @Test
          internal fun `can not be older than 110 years old`() {
            expectBadRequest(
              requestToCreate(
                dateOfBirth = LocalDate.now().minusYears(110).minusDays(1)
              )
            ).jsonPath("$.developerMessage")
              .isEqualTo(
                "Date of birth must be between ${LocalDate.now().minusYears(110)} and ${
                LocalDate.now().minusYears(16)
                }"
              )
          }

          @Test
          internal fun `can not be younger than 16 years old`() {
            expectBadRequest(
              requestToCreate(
                dateOfBirth = LocalDate.now().minusYears(16).plusDays(1)
              )
            ).jsonPath("$.developerMessage")
              .isEqualTo(
                "Date of birth must be between ${LocalDate.now().minusYears(110)} and ${
                LocalDate.now().minusYears(16)
                }"
              )
          }
        }

        @Nested
        @DisplayName("Duplicate prisoners")
        inner class DuplicatePrisoners {

          @Test
          internal fun `must not create prisoners with same names and date of birth of existing prisoner`() {
            expectBadRequest(
              requestToCreate(
                lastName = "Rochefort",
                firstName = "Fabien",
                dateOfBirth = LocalDate.parse("1987-09-12"),
                pncNumber = null,
                croNumber = null
              )
            ).jsonPath("$.developerMessage")
              .isEqualTo("Prisoner with lastname ROCHEFORT, firstname FABIEN and dob 1987-09-12 already exists with ID ${existingPrisoner.offenderNo}")
          }
        }

        @Nested
        @DisplayName("Gender")
        inner class Gender {
          @Test
          internal fun `gender can not be greater than 12 characters`() {
            expectBadRequest(requestToCreate(gender = "A".repeat(13))).jsonPath("$.developerMessage")
              .isEqualTo("Field: gender - Value is too long: max length is 12")
          }

          @Test
          internal fun `gender must match one of the reference data types`() {
            expectNotFoundRequest(requestToCreate(gender = "NONE")).jsonPath("$.developerMessage")
              .isEqualTo("Gender NONE not found")
          }
        }

        @Nested
        @DisplayName("Ethnicity")
        inner class Ethnicity {
          @Test
          internal fun `ethnicity can not be greater than 12 characters`() {
            expectBadRequest(requestToCreate(ethnicity = "A".repeat(13))).jsonPath("$.developerMessage")
              .isEqualTo("Field: ethnicity - Value is too long: max length is 12")
          }

          @Test
          internal fun `ethnicity must match one of the reference data types`() {
            expectNotFoundRequest(requestToCreate(ethnicity = "NONE")).jsonPath("$.developerMessage")
              .isEqualTo("Ethnicity NONE not found")
          }
        }

        @Nested
        @DisplayName("CRO Number")
        inner class CRONumber {
          @Test
          internal fun `croNumber can not be greater than 20 characters`() {
            expectBadRequest(requestToCreate(croNumber = "A".repeat(21))).jsonPath("$.developerMessage")
              .isEqualTo("Field: croNumber - Value is too long: max length is 20")
          }

          @Test
          internal fun `must not have the same croNumber as an existing prisoner`() {
            expectBadRequest(requestToCreate(croNumber = "69560/88G")).jsonPath("$.developerMessage")
              .isEqualTo("Prisoner with CRO 69560/88G already exists with ID ${existingPrisoner.offenderNo}")
          }
        }
      }
    }
  }

  fun expectBadRequest(body: Any): WebTestClient.BodyContentSpec =
    webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).bodyValue(body).accept(MediaType.APPLICATION_JSON)
      .exchange().expectStatus().isBadRequest.expectBody().jsonPath("$.status").isEqualTo(400)

  fun expectNotFoundRequest(body: Any): WebTestClient.BodyContentSpec =
    webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).bodyValue(body).accept(MediaType.APPLICATION_JSON)
      .exchange().expectStatus().isNotFound.expectBody().jsonPath("$.status").isEqualTo(404)
}

private fun requestToCreate(
  pncNumber: String? = "2005/471839U",
  lastName: String? = "Toussaint",
  firstName: String? = "Manon",
  middleName1: String? = "Gabriel",
  middleName2: String? = "Antoine",
  title: String? = "MR",
  suffix: String? = "SR",
  dateOfBirth: LocalDate? = LocalDate.parse("1965-07-19"),
  gender: String? = "M",
  ethnicity: String? = "M1",
  croNumber: String? = "159049/05L",
) = RequestToCreate(
  pncNumber, lastName, firstName, middleName1, middleName2, title, suffix, dateOfBirth, gender, ethnicity, croNumber
)
