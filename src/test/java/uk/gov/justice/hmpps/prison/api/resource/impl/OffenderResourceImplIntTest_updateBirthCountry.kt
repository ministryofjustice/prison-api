@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateService

class OffenderResourceImplIntTest_updateBirthCountry : ResourceTest() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @SpyBean
  lateinit var prisonerProfileUpdateService: PrisonerProfileUpdateService

  @Nested
  inner class Authorisation {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.put()
        .uri("api/offenders/A1234AL/birth-country")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_BIRTH_COUNTRY_UPDATE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.put()
        .uri("api/offenders/A1234AL/birth-country")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_BIRTH_COUNTRY_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("api/offenders/A1234AL/birth-country")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_BIRTH_COUNTRY_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 204 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("api/offenders/A1234AL/birth-country")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_BIRTH_COUNTRY_UPDATE)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  open inner class HappyPath {
    @Test
    @Transactional(readOnly = true)
    open fun `should update the birth country`() {
      webTestClient.put()
        .uri("api/offenders/A1234AL/birth-country")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_BIRTH_COUNTRY_UPDATE)
        .exchange()
        .expectStatus().isNoContent

      assertThat(offenderRepository.findById(-1012L).get().birthCountry.code).isEqualTo("FRA")
    }

    @Test
    @Transactional(readOnly = true)
    open fun `should allow the birth country to be updated to null`() {
      webTestClient.put()
        .uri("api/offenders/A1234AL/birth-country")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(NULL_BIRTH_COUNTRY_UPDATE)
        .exchange()
        .expectStatus().isNoContent

      assertThat(offenderRepository.findById(-1012L).get().birthCountry).isNull()
    }
  }

  @Nested
  inner class ErrorConditions {
    @Test
    fun shouldReturn404WhenOffenderDoesNotExist() {
      webTestClient.put()
        .uri("api/offenders/AAA444/birth-country")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_BIRTH_COUNTRY_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Prisoner with prisonerNumber AAA444 and existing booking not found")
    }

    @Test
    fun shouldReturn404WhenOffenderHasNoBooking() {
      webTestClient.put()
        .uri("api/offenders/A9880GH/birth-country")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_BIRTH_COUNTRY_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Prisoner with prisonerNumber A9880GH and existing booking not found")
    }

    @Test
    fun `returns status 423 (locked) when database row lock times out`() {
      doThrow(DatabaseRowLockedException("developer message"))
        .whenever(prisonerProfileUpdateService).updateBirthCountryOfCurrentAlias(anyString(), anyString())

      webTestClient.put()
        .uri("api/offenders/A1234AL/birth-country")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_BIRTH_COUNTRY_UPDATE)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.LOCKED)
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource locked, possibly in use in P-Nomis.")
        .jsonPath("developerMessage").isEqualTo("developer message")
    }
  }

  private companion object {
    const val VALID_BIRTH_COUNTRY_UPDATE =
      // language=json
      """
        {
          "countryCode": "FRA"
        }
      """

    const val NULL_BIRTH_COUNTRY_UPDATE =
      // language=json
      """
        {
          "countryCode": null
        }
      """
  }
}
