@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.CorePersonRecordAlias
import uk.gov.justice.hmpps.prison.api.model.UpdateAlias
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateService
import java.time.LocalDate

class AliasResourceImplIntTest : ResourceTest() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @MockitoSpyBean
  lateinit var prisonerProfileUpdateService: PrisonerProfileUpdateService

  @Nested
  @DisplayName("GET /offender/{offenderNo}/aliases")
  inner class GetAliases {

    @Nested
    @DisplayName("Authorisation checks")
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.get()
          .uri("api/offenders/A1072AA/aliases")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.get()
          .uri("api/offenders/A1072AA/aliases")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.get()
          .uri("api/offenders/A1072AA/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 200 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.get()
          .uri("api/offenders/A1072AA/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isOk
      }
    }

    @Nested
    @DisplayName("Happy path")
    open inner class HappyPath {

      @Test
      open fun `should retrieve aliases`() {
        val aliases = webTestClient.get()
          .uri("api/offenders/A1234AL/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isOk
          .returnResult<List<CorePersonRecordAlias>>()
          .responseBody
          .blockFirst()!!

        assertThat(aliases).hasSize(2)
        assertThat(aliases.map { it.offenderId }).containsExactlyInAnyOrder(-1012L, -1013L)
        assertThat(aliases.firstOrNull { it.offenderId == -1012L }?.isWorkingName).isTrue()
        assertThat(aliases.firstOrNull { it.offenderId == -1013L }?.isWorkingName).isFalse()
      }

      @Test
      open fun `should populate all fields of alias`() {
        val aliases = webTestClient.get()
          .uri("api/offenders/A1072AA/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isOk
          .returnResult<List<CorePersonRecordAlias>>()
          .responseBody
          .blockFirst()!!

        with(aliases.first()) {
          assertThat(prisonerNumber).isEqualTo("A1072AA")
          assertThat(offenderId).isEqualTo(-1072)
          assertThat(isWorkingName).isTrue()
          assertThat(firstName).isEqualTo("OLD FIRST NAME")
          assertThat(middleName1).isEqualTo("OLD MIDDLE NAME")
          assertThat(middleName2).isEqualTo("OLD SECOND MIDDLE NAME")
          assertThat(lastName).isEqualTo("OLD LAST NAME")
          assertThat(dateOfBirth).isEqualTo(LocalDate.of(2000, 12, 1))
          assertThat(nameType?.code).isEqualTo("CN")
          assertThat(title?.code).isEqualTo("MR")
          assertThat(sex?.code).isEqualTo("M")
          assertThat(ethnicity?.code).isEqualTo("W1")
        }
      }
    }
  }

  @Nested
  @DisplayName("GET /aliases/{offenderId}")
  inner class GetAlias {
    @Nested
    @DisplayName("Authorisation checks")
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.get()
          .uri("api/aliases/-1072")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.get()
          .uri("api/aliases/-1072")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.get()
          .uri("api/aliases/-1072")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 200 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.get()
          .uri("api/aliases/-1072")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isOk
      }
    }

    @Nested
    @DisplayName("Happy path")
    open inner class HappyPath {

      @Test
      open fun `should retrieve alias`() {
        val alias = webTestClient.get()
          .uri("api/aliases/-1072")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isOk
          .returnResult<CorePersonRecordAlias>()
          .responseBody
          .blockFirst()!!

        assertThat(alias.offenderId).isEqualTo(-1072L)
        assertThat(alias.prisonerNumber).isEqualTo("A1072AA")
        assertThat(alias.isWorkingName).isTrue()
      }

      @Test
      open fun `should populate all fields of alias`() {
        val alias = webTestClient.get()
          .uri("api/aliases/-1072")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isOk
          .returnResult<CorePersonRecordAlias>()
          .responseBody
          .blockFirst()!!

        with(alias) {
          assertThat(prisonerNumber).isEqualTo("A1072AA")
          assertThat(offenderId).isEqualTo(-1072)
          assertThat(isWorkingName).isTrue()
          assertThat(firstName).isEqualTo("OLD FIRST NAME")
          assertThat(middleName1).isEqualTo("OLD MIDDLE NAME")
          assertThat(middleName2).isEqualTo("OLD SECOND MIDDLE NAME")
          assertThat(lastName).isEqualTo("OLD LAST NAME")
          assertThat(dateOfBirth).isEqualTo(LocalDate.of(2000, 12, 1))
          assertThat(nameType?.code).isEqualTo("CN")
          assertThat(title?.code).isEqualTo("MR")
          assertThat(sex?.code).isEqualTo("M")
          assertThat(ethnicity?.code).isEqualTo("W1")
        }
      }
    }
  }

  @Nested
  @DisplayName("POST /offender/{offenderNo}/aliases")
  inner class CreateAlias {

    @Nested
    @DisplayName("Authorisation checks")
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.post()
          .uri("api/offenders/A1072AA/aliases")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_CREATION)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.post()
          .uri("api/offenders/A1072AA/aliases")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_CREATION)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.post()
          .uri("api/offenders/A1072AA/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_CREATION)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 201 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.post()
          .uri("api/offenders/A1072AA/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_CREATION)
          .exchange()
          .expectStatus().isCreated
      }
    }

    @Nested
    @DisplayName("Happy path")
    open inner class HappyPath {

      @Test
      @Transactional(readOnly = true)
      open fun `should create a new non-working name alias`() {
        val newAlias = webTestClient.post()
          .uri("api/offenders/A1073AA/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(NON_WORKING_NAME_ALIAS_CREATION)
          .exchange()
          .expectStatus().isCreated
          .returnResult<CorePersonRecordAlias>()
          .responseBody
          .blockFirst()

        assertThat(newAlias).isNotNull
        assertThat(newAlias!!.offenderId).isNotEqualTo(-1073)

        with(offenderRepository.findById(newAlias.offenderId).get()) {
          assertThat(firstName).isEqualTo("JOHN")
          assertThat(middleName).isEqualTo("MIDDLEONE")
          assertThat(middleName2).isEqualTo("MIDDLETWO")
          assertThat(lastName).isEqualTo("SMITH")
          assertThat(birthDate).isEqualTo(LocalDate.of(1990, 1, 1))
          assertThat(gender.code).isEqualTo("M")
          assertThat(title.code).isEqualTo("MR")
          assertThat(ethnicity.code).isEqualTo("W1")
          assertThat(aliasNameType.code).isEqualTo("CN")
        }

        assertThat(offenderRepository.findLinkedToLatestBookingForUpdate("A1073AA").get().id)
          .isEqualTo(-1073)
      }

      @Test
      @Transactional(readOnly = true)
      open fun `should create a new working name alias`() {
        val newAlias = webTestClient.post()
          .uri("api/offenders/A1074AA/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(WORKING_NAME_ALIAS_CREATION)
          .exchange()
          .expectStatus().isCreated
          .returnResult<CorePersonRecordAlias>()
          .responseBody
          .blockFirst()

        assertThat(newAlias).isNotNull
        assertThat(newAlias!!.offenderId).isNotEqualTo(-1074)

        with(offenderRepository.findLinkedToLatestBookingForUpdate("A1074AA").get()) {
          assertThat(id).isEqualTo(newAlias.offenderId)
          assertThat(firstName).isEqualTo("JOHN")
          assertThat(middleName).isEqualTo("MIDDLEONE")
          assertThat(middleName2).isEqualTo("MIDDLETWO")
          assertThat(lastName).isEqualTo("SMITH")
          assertThat(birthDate).isEqualTo(LocalDate.of(1990, 1, 1))
          assertThat(gender.code).isEqualTo("M")
          assertThat(title.code).isEqualTo("MR")
          assertThat(ethnicity.code).isEqualTo("W1")
          assertThat(aliasNameType.code).isEqualTo("CN")
        }
      }

      @Test
      @Transactional(readOnly = true)
      open fun `should accept minimal number of fields`() {
        val newAlias = webTestClient.post()
          .uri("api/offenders/A1075AA/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(MINIMAL_ALIAS_CREATION)
          .exchange()
          .expectStatus().isCreated
          .returnResult<CorePersonRecordAlias>()
          .responseBody
          .blockFirst()

        assertThat(newAlias).isNotNull
        assertThat(newAlias!!.offenderId).isNotEqualTo(-1075)

        with(offenderRepository.findLinkedToLatestBookingForUpdate("A1075AA").get()) {
          assertThat(id).isEqualTo(newAlias.offenderId)
          assertThat(firstName).isEqualTo("JOHN")
          assertThat(middleName).isNull()
          assertThat(middleName2).isNull()
          assertThat(lastName).isEqualTo("SMITH")
          assertThat(birthDate).isEqualTo(LocalDate.of(1990, 1, 1))
          assertThat(gender.code).isEqualTo("M")
          assertThat(title).isNull()
          assertThat(ethnicity).isNull()
          assertThat(aliasNameType).isNull()
        }
      }
    }

    @Nested
    @DisplayName("Error conditions")
    inner class ErrorConditions {
      @Test
      fun shouldReturn404WhenPrisonerDoesNotExist() {
        webTestClient.post()
          .uri("api/offenders/XXXX/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_CREATION)
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage")
          .isEqualTo("Prisoner with prisonerNumber XXXX and existing booking not found")
      }

      @Test
      fun `returns status 423 (locked) when database row lock times out`() {
        doThrow(DatabaseRowLockedException("developer message"))
          .whenever(prisonerProfileUpdateService).createAlias(anyString(), any())

        webTestClient.post()
          .uri("api/offenders/A1072AA/aliases")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_CREATION)
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
            expectBadRequest(createRequest(firstName = "@@@"))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - First name is not valid")
          }

          @Test
          internal fun `first name can not be greater than 35 characters`() {
            expectBadRequest(createRequest(firstName = "A".repeat(36)))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - Value is too long: max length is 35")
          }

          @Test
          internal fun `first name can not be blank`() {
            expectBadRequest(createRequest(firstName = "   "))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - Value cannot be blank")
          }
        }

        @Nested
        @DisplayName("Last name")
        inner class LastName {
          @Test
          internal fun `last name must only contain valid characters`() {
            expectBadRequest(createRequest(lastName = "###"))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Last name is not valid")
          }

          @Test
          internal fun `last name can not be greater than 35 characters`() {
            expectBadRequest(createRequest(lastName = "A".repeat(36)))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Value is too long: max length is 35")
          }

          @Test
          internal fun `last name can not be blank`() {
            expectBadRequest(createRequest(lastName = "   "))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Value cannot be blank")
          }
        }

        @Nested
        @DisplayName("Middle name 1")
        inner class MiddleName1 {

          @Test
          internal fun `first middle name must only contain valid characters`() {
            expectBadRequest(createRequest(middleName1 = "@@@"))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName1 - Middle name 1 is not valid")
          }

          @Test
          internal fun `first middle can not be greater than 35 characters`() {
            expectBadRequest(createRequest(middleName1 = "A".repeat(36)))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName1 - Value is too long: max length is 35")
          }
        }

        @Nested
        @DisplayName("Middle name 2")
        inner class MiddleName2 {

          @Test
          internal fun `second middle name must only contain valid characters`() {
            expectBadRequest(createRequest(middleName2 = "@@@"))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName2 - Middle name 2 is not valid")
          }

          @Test
          internal fun `second middle can not be greater than 35 characters`() {
            expectBadRequest(createRequest(middleName2 = "A".repeat(36)))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName2 - Value is too long: max length is 35")
          }
        }
      }
    }

    private fun createRequest(
      firstName: String = "John",
      middleName1: String? = "Middleone",
      middleName2: String? = "Middletwo",
      lastName: String = "Smith",
      title: String? = "MR",
      dateOfBirth: LocalDate = LocalDate.parse("1990-01-02"),
      sex: String = "M",
      ethnicity: String? = "M1",
      nameType: String? = "CN",
    ) = UpdateAlias(
      firstName,
      middleName1,
      middleName2,
      lastName,
      dateOfBirth,
      sex,
      title,
      ethnicity,
      nameType,
    )

    fun expectBadRequest(body: Any): WebTestClient.BodyContentSpec = webTestClient.post()
      .uri("api/offenders/A1072AA/aliases")
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .bodyValue(body)
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("$.status").isEqualTo(400)
  }

  @Nested
  @DisplayName("PUT /aliases/{offenderId}")
  inner class UpdateAlias {

    @Nested
    @DisplayName("Authorisation checks")
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.put()
          .uri("api/aliases/-1072")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_UPDATE)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.put()
          .uri("api/aliases/-1072")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_UPDATE)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.put()
          .uri("api/aliases/-1072")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_UPDATE)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 200 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.put()
          .uri("api/aliases/-1072")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_UPDATE)
          .exchange()
          .expectStatus().isOk
      }
    }

    @Nested
    @DisplayName("Happy path")
    open inner class HappyPath {
      @Test
      @Transactional(readOnly = true)
      open fun `should update the current working name alias`() {
        webTestClient.put()
          .uri("api/aliases/-1073")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_UPDATE)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("offenderId").isEqualTo(-1073)

        with(offenderRepository.findById(-1073L).get()) {
          assertThat(firstName).isEqualTo("JOHN")
          assertThat(middleName).isEqualTo("MIDDLEONE")
          assertThat(middleName2).isEqualTo("MIDDLETWO")
          assertThat(lastName).isEqualTo("SMITH")
          assertThat(birthDate).isEqualTo(LocalDate.of(1990, 1, 1))
          assertThat(gender.code).isEqualTo("M")
          assertThat(title.code).isEqualTo("MR")
          assertThat(ethnicity.code).isEqualTo("W1")
          assertThat(aliasNameType.code).isEqualTo("CN")
        }
      }

      @Test
      @Transactional(readOnly = true)
      open fun `should accept minimal number of fields`() {
        webTestClient.put()
          .uri("api/aliases/-1073")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(MINIMAL_ALIAS_UPDATE)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("offenderId").isEqualTo(-1073)

        with(offenderRepository.findById(-1073L).get()) {
          assertThat(firstName).isEqualTo("JOHN")
          assertThat(middleName).isNull()
          assertThat(middleName2).isNull()
          assertThat(lastName).isEqualTo("SMITH")
          assertThat(birthDate).isEqualTo(LocalDate.of(1990, 1, 1))
          assertThat(gender.code).isEqualTo("M")
          assertThat(title).isNull()
          assertThat(ethnicity).isNull()
          assertThat(aliasNameType).isNull()
        }
      }
    }

    @Nested
    @DisplayName("Error conditions")
    inner class ErrorConditions {
      @Test
      fun shouldReturn404WhenAliasDoesNotExist() {
        webTestClient.put()
          .uri("api/aliases/9999")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_UPDATE)
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage")
          .isEqualTo("Alias with offenderId 9999 not found")
      }

      @Test
      fun `returns status 423 (locked) when database row lock times out`() {
        doThrow(DatabaseRowLockedException("developer message"))
          .whenever(prisonerProfileUpdateService).updateAlias(anyLong(), any())

        webTestClient.put()
          .uri("api/aliases/-1072")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(VALID_ALIAS_UPDATE)
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
            expectBadRequest(updateRequest(firstName = "@@@"))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - First name is not valid")
          }

          @Test
          internal fun `first name can not be greater than 35 characters`() {
            expectBadRequest(updateRequest(firstName = "A".repeat(36)))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - Value is too long: max length is 35")
          }

          @Test
          internal fun `first name can not be blank`() {
            expectBadRequest(updateRequest(firstName = "   "))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: firstName - Value cannot be blank")
          }
        }

        @Nested
        @DisplayName("Last name")
        inner class LastName {
          @Test
          internal fun `last name must only contain valid characters`() {
            expectBadRequest(updateRequest(lastName = "###"))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Last name is not valid")
          }

          @Test
          internal fun `last name can not be greater than 35 characters`() {
            expectBadRequest(updateRequest(lastName = "A".repeat(36)))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Value is too long: max length is 35")
          }

          @Test
          internal fun `last name can not be blank`() {
            expectBadRequest(updateRequest(lastName = "   "))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: lastName - Value cannot be blank")
          }
        }

        @Nested
        @DisplayName("Middle name 1")
        inner class MiddleName {

          @Test
          internal fun `first middle name must only contain valid characters`() {
            expectBadRequest(updateRequest(middleName1 = "@@@"))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName1 - Middle name 1 is not valid")
          }

          @Test
          internal fun `first middle can not be greater than 35 characters`() {
            expectBadRequest(updateRequest(middleName1 = "A".repeat(36)))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName1 - Value is too long: max length is 35")
          }
        }

        @Nested
        @DisplayName("Middle name 2")
        inner class MiddleName2 {

          @Test
          internal fun `second middle name must only contain valid characters`() {
            expectBadRequest(updateRequest(middleName2 = "@@@"))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName2 - Middle name 2 is not valid")
          }

          @Test
          internal fun `second middle can not be greater than 35 characters`() {
            expectBadRequest(updateRequest(middleName2 = "A".repeat(36)))
              .jsonPath("$.developerMessage")
              .isEqualTo("Field: middleName2 - Value is too long: max length is 35")
          }
        }
      }
    }

    private fun updateRequest(
      firstName: String = "John",
      middleName1: String? = "Middleone",
      middleName2: String? = "Middletwo",
      lastName: String = "Smith",
      title: String? = "MR",
      dateOfBirth: LocalDate = LocalDate.parse("1990-01-02"),
      sex: String = "M",
      ethnicity: String? = "M1",
      nameType: String? = "CN",
    ) = UpdateAlias(
      firstName,
      middleName1,
      middleName2,
      lastName,
      dateOfBirth,
      sex,
      title,
      ethnicity,
      nameType,
    )

    fun expectBadRequest(body: Any): WebTestClient.BodyContentSpec = webTestClient.put()
      .uri("api/aliases/-1072")
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .bodyValue(body)
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("$.status").isEqualTo(400)
  }

  private companion object {
    const val NON_WORKING_NAME_ALIAS_CREATION =
      // language=json
      """
        {
          "firstName": "John",
          "middleName1": "Middleone",
          "middleName2": "Middletwo",
          "lastName": "Smith",
          "dateOfBirth": "1990-01-01",
          "nameType": "CN",
          "title": "MR",
          "sex": "M",
          "ethnicity": "W1",
          "isWorkingName": false 
        }
      """

    const val WORKING_NAME_ALIAS_CREATION =
      // language=json
      """
        {
          "firstName": "John",
          "middleName1": "Middleone",
          "middleName2": "Middletwo",
          "lastName": "Smith",
          "dateOfBirth": "1990-01-01",
          "nameType": "CN",
          "title": "MR",
          "sex": "M",
          "ethnicity": "W1",
          "isWorkingName": true 
        }
      """

    const val MINIMAL_ALIAS_CREATION =
      // language=json
      """
        {
          "firstName": "John",
          "lastName": "Smith",
          "dateOfBirth": "1990-01-01",
          "sex": "M",
          "isWorkingName": true 
        }
      """

    const val VALID_ALIAS_CREATION = NON_WORKING_NAME_ALIAS_CREATION

    const val MINIMAL_ALIAS_UPDATE =
      // language=json
      """
        {
          "firstName": "John",
          "lastName": "Smith",
          "dateOfBirth": "1990-01-01",
          "sex": "M"
        }
      """

    const val VALID_ALIAS_UPDATE =
      // language=json
      """
        {
          "firstName": "John",
          "middleName1": "Middleone",
          "middleName2": "Middletwo",
          "lastName": "Smith",
          "dateOfBirth": "1990-01-01",
          "nameType": "CN",
          "title": "MR",
          "sex": "M",
          "ethnicity": "W1"
        }
      """
  }
}
