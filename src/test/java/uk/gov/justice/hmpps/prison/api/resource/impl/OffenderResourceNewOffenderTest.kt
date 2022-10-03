package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
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
    @DisplayName("when new offender is created")
    inner class Success {
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
    }

    @Nested
    @DisplayName("when new offender is rejected")
    inner class Failure {
      @Nested
      @DisplayName("when new request is not valid")
      inner class Validation {
        @Nested
        @DisplayName("PNC Number")
        inner class PNCNumber {
          @Test
          internal fun `must have a PNC in the correct format`() {
            expectBadRequest(requestToCreate(pncNumber = "AA/122343")).jsonPath("$.developerMessage")
              .isEqualTo("Field: pncNumber - PNC is not valid")
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
      }

      @Nested
      @DisplayName("Title")
      inner class Title {
        @Test
        internal fun `title can not be greater than 12 characters`() {
          expectBadRequest(requestToCreate(title = "A".repeat(13))).jsonPath("$.developerMessage")
            .isEqualTo("Field: title - Value is too long: max length is 12")
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
      }

      @Nested
      @DisplayName("Gender")
      inner class Gender {
        @Test
        internal fun `gender can not be greater than 12 characters`() {
          expectBadRequest(requestToCreate(gender = "A".repeat(13))).jsonPath("$.developerMessage")
            .isEqualTo("Field: gender - Value is too long: max length is 12")
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
      }
      @Nested
      @DisplayName("CRO Number")
      inner class CRONumber {
        @Test
        internal fun `croNumber can not be greater than 20 characters`() {
          expectBadRequest(requestToCreate(croNumber = "A".repeat(21))).jsonPath("$.developerMessage")
            .isEqualTo("Field: croNumber - Value is too long: max length is 20")
        }
      }
    }
  }

  fun expectBadRequest(body: Any): WebTestClient.BodyContentSpec =
    webTestClient.post().uri("/api/offenders").headers(setAuthorisation(listOf("ROLE_BOOKING_CREATE")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).bodyValue(body).accept(MediaType.APPLICATION_JSON)
      .exchange().expectStatus().isBadRequest.expectBody().jsonPath("$.status").isEqualTo(400)!!
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
