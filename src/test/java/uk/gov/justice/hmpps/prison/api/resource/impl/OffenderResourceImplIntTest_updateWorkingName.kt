@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateService

class OffenderResourceImplIntTest_updateWorkingName : ResourceTest() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @MockitoSpyBean
  lateinit var prisonerProfileUpdateService: PrisonerProfileUpdateService

  @Nested
  @DisplayName("PUT /offenders/{offenderNo}/working-name")
  inner class UpdateWorkingName {

    @Nested
    @DisplayName("Authorisation checks")
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.put()
          .uri("api/offenders/A1072AA/working-name")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_WORKING_NAME_UPDATE)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.put()
          .uri("api/offenders/A1072AA/working-name")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_WORKING_NAME_UPDATE)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.put()
          .uri("api/offenders/A1072AA/working-name")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_WORKING_NAME_UPDATE)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 204 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.put()
          .uri("api/offenders/A1072AA/working-name")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_WORKING_NAME_UPDATE)
          .exchange()
          .expectStatus().isNoContent
      }
    }

    @Nested
    @DisplayName("Happy path")
    open inner class HappyPath {
      @Test
      @Transactional(readOnly = true)
      open fun `should update the current working name alias`() {
        webTestClient.put()
          .uri("api/offenders/A1073AA/working-name")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(FIRST_NAME_CHANGE)
          .exchange()
          .expectStatus().isNoContent

        with(offenderRepository.findById(-1073L).get()) {
          assertThat(firstName).isEqualTo("JOHN")
          assertThat(lastName).isEqualTo("OLD LAST NAME")
        }
      }

      @Test
      @Transactional(readOnly = true)
      open fun `should force creation of a new alias`() {
        webTestClient.put()
          .uri("api/offenders/A1074AA/working-name?forceAliasCreation=true")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(FIRST_NAME_CHANGE)
          .exchange()
          .expectStatus().isNoContent

        with(offenderRepository.findLinkedToLatestBookingForUpdate("A1074AA").get()) {
          assertThat(firstName).isEqualTo("JOHN")
          assertThat(lastName).isEqualTo("OLD LAST NAME")
        }

        // But... the old alias is retained...
        with(offenderRepository.findById(-1074L).get()) {
          assertThat(firstName).isEqualTo("OLD FIRST NAME")
          assertThat(lastName).isEqualTo("OLD LAST NAME")
        }
      }

      @Test
      @Transactional(readOnly = true)
      open fun `update to last name should create a new alias`() {
        webTestClient.put()
          .uri("api/offenders/A1075AA/working-name")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(ALL_NAME_CHANGE)
          .exchange()
          .expectStatus().isNoContent

        with(offenderRepository.findLinkedToLatestBookingForUpdate("A1075AA").get()) {
          assertThat(firstName).isEqualTo("JOHN")
          assertThat(middleName).isEqualTo("MIDDLEONE")
          assertThat(middleName2).isEqualTo("MIDDLETWO")
          assertThat(lastName).isEqualTo("SMITH")
        }

        // But... the old alias is retained...
        with(offenderRepository.findById(-1075L).get()) {
          assertThat(firstName).isEqualTo("OLD FIRST NAME")
          assertThat(lastName).isEqualTo("OLD LAST NAME")
        }
      }

      @Test
      @Transactional(readOnly = true)
      open fun `should return 304 NOT MODIFIED when no changes have been made`() {
        webTestClient.put()
          .uri("api/offenders/A1076AA/working-name")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(NO_CHANGE_WORKING_NAME_UPDATE)
          .exchange()
          .expectStatus().isNotModified

        with(offenderRepository.findById(-1076L).get()) {
          assertThat(firstName).isEqualTo("OLD FIRST NAME")
          assertThat(lastName).isEqualTo("OLD LAST NAME")
        }
      }
    }

    @Nested
    @DisplayName("Error conditions")
    inner class ErrorConditions {
      @Test
      fun shouldReturn404WhenOffenderDoesNotExist() {
        webTestClient.put()
          .uri("api/offenders/AAA444/working-name")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_WORKING_NAME_UPDATE)
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage")
          .isEqualTo("Prisoner with prisonerNumber AAA444 and existing booking not found")
      }

      @Test
      fun shouldReturn404WhenOffenderHasNoBooking() {
        webTestClient.put()
          .uri("api/offenders/A9880GH/working-name")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_WORKING_NAME_UPDATE)
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage")
          .isEqualTo("Prisoner with prisonerNumber A9880GH and existing booking not found")
      }

      @Test
      fun `returns status 423 (locked) when database row lock times out`() {
        doThrow(DatabaseRowLockedException("developer message"))
          .whenever(prisonerProfileUpdateService).updateWorkingName(anyString(), any(), anyBoolean())

        webTestClient.put()
          .uri("api/offenders/A1072AA/working-name")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_WORKING_NAME_UPDATE)
          .exchange()
          .expectStatus().isEqualTo(HttpStatus.LOCKED)
          .expectBody()
          .jsonPath("userMessage").isEqualTo("Resource locked, possibly in use in P-Nomis.")
          .jsonPath("developerMessage").isEqualTo("developer message")
      }

      @Nested
      @DisplayName("Validation checks")
      inner class Validation {

        @Nested
        @DisplayName("First name")
        inner class FirstName {
          @Test
          internal fun `first name must only contain valid characters`() {
            expectBadRequest("""{"firstName": "@@@", "lastName": "Smith"}""")
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - First name is not valid")
          }

          @Test
          internal fun `first name can not be greater than 35 characters`() {
            expectBadRequest("""{"firstName": "${"A".repeat(36)}", "lastName": "Smith"}""")
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - Value is too long: max length is 35")
          }

          @Test
          internal fun `first name can not be blank`() {
            expectBadRequest("""{"firstName": "   ", "lastName": "Smith"}""")
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - Value cannot be blank")
          }
        }

        @Nested
        @DisplayName("Last name")
        inner class LastName {
          @Test
          internal fun `last name must only contain valid characters`() {
            expectBadRequest("""{"firstName": "John", "lastName": "@@@"}""")
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Last name is not valid")
          }

          @Test
          internal fun `last name can not be greater than 35 characters`() {
            expectBadRequest("""{"firstName": "John", "lastName": "${"A".repeat(36)}"}""")
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Value is too long: max length is 35")
          }

          @Test
          internal fun `last name can not be blank`() {
            expectBadRequest("""{"firstName": "John", "lastName": "   "}""")
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Value cannot be blank")
          }
        }

        @Nested
        @DisplayName("Middle names")
        inner class MiddleNames {

          @Test
          internal fun `first middle name must only contain valid characters`() {
            expectBadRequest("""{"firstName": "John", "middleName1": "@@@", "lastName": "Smith"}""")
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName1 - Middle name 1 is not valid")
          }

          @Test
          internal fun `first middle can not be greater than 35 characters`() {
            expectBadRequest("""{"firstName": "John", "middleName1": "${"A".repeat(36)}", "lastName": "Smith"}""")
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName1 - Value is too long: max length is 35")
          }

          @Test
          internal fun `second middle name must only contain valid characters`() {
            expectBadRequest("""{"firstName": "John", "middleName2": "@@@", "lastName": "Smith"}""").jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName2 - Middle name 2 is not valid")
          }

          @Test
          internal fun `second middle can not be greater than 35 characters`() {
            expectBadRequest("""{"firstName": "John", "middleName2": "${"A".repeat(36)}", "lastName": "Smith"}""").jsonPath(
              "$.developerMessage",
            )
              .isEqualTo("Field: middleName2 - Value is too long: max length is 35")
          }
        }
      }
    }
  }

  fun expectBadRequest(body: Any): WebTestClient.BodyContentSpec = webTestClient.put()
    .uri("api/offenders/A1072AA/working-name")
    .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
    .header("Content-Type", APPLICATION_JSON_VALUE)
    .bodyValue(body)
    .exchange()
    .expectStatus().isBadRequest
    .expectBody().jsonPath("$.status").isEqualTo(400)

  private companion object {
    const val FIRST_NAME_CHANGE =
      // language=json
      """
        {
          "firstName": "John",
          "lastName": "Old Last Name"
        }
      """

    const val ALL_NAME_CHANGE =
      // language=json
      """
        {
          "firstName": "John",
          "middleName1": "Middleone",
          "middleName2": "Middletwo",
          "lastName": "Smith"
        }
      """

    const val VALID_WORKING_NAME_UPDATE = FIRST_NAME_CHANGE

    const val NO_CHANGE_WORKING_NAME_UPDATE =
      // language=json
      """
        {
          "firstName": "OLD FIRST NAME",
          "middleName1": "OLD MIDDLE NAME",
          "lastName": "OLD LAST NAME"
        }
      """
  }
}
