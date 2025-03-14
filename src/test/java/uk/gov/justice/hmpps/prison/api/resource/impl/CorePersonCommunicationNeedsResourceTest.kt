package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository

class CorePersonCommunicationNeedsResourceTest : ResourceTest() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @Nested
  inner class GetCommunicationNeeds {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AA/core-person-record/communication-needs")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AA/core-person-record/communication-needs")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AA/core-person-record/communication-needs")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.languagePreferences.preferredSpokenLanguage.code").isEqualTo("POL")
        .jsonPath("$.languagePreferences.interpreterRequired").isEqualTo(false)
    }
  }

  @Nested
  inner class UpdateLanguagePreferences {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/language-preferences")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_LANGUAGE_PREFERENCES_UPDATE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/language-preferences")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_LANGUAGE_PREFERENCES_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 204 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/language-preferences")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_LANGUAGE_PREFERENCES_UPDATE)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  inner class AddSecondaryLanguage {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/secondary-language")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_SECONDARY_LANGUAGE_UPDATE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/secondary-language")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_SECONDARY_LANGUAGE_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 204 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/secondary-language")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_SECONDARY_LANGUAGE_UPDATE)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  inner class DeleteSecondaryLanguage {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.delete()
        .uri("/api/offenders/A1234AA/core-person-record/secondary-language/SPA")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.delete()
        .uri("/api/offenders/A1234AA/core-person-record/secondary-language/SPA")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 204 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.delete()
        .uri("/api/offenders/A1234AA/core-person-record/secondary-language/SPA")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .exchange()
        .expectStatus().isNoContent
    }
  }

  private companion object {
    const val VALID_LANGUAGE_PREFERENCES_UPDATE =
      // language=json
      """
        {
            "preferredSpokenLanguageCode": "ENG",
            "preferredWrittenLanguageCode": "ENG",
            "interpreterRequired": true
        }
      """

    const val VALID_SECONDARY_LANGUAGE_UPDATE =
      // language=json
      """
        {
            "language": "SPA",
            "canRead": true,
            "canSpeak": false,
            "canWrite": true
        }
      """
  }
}
