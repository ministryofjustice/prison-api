package uk.gov.justice.hmpps.prison.api.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateService

class CorePersonPhysicalAttributesResourceTest : ResourceTest() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @MockitoSpyBean
  lateinit var prisonerProfileUpdateService: PrisonerProfileUpdateService

  @Nested
  inner class GetPhysicalAttributes {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AA/core-person-record/physical-attributes")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AA/core-person-record/physical-attributes")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AA/core-person-record/physical-attributes")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.height").isEqualTo(155)
        .jsonPath("$.weight").isEqualTo(77)
        .jsonPath("$.rightEyeColour.code").isEqualTo("BLUE")
        .jsonPath("$.rightEyeColour.description").isEqualTo("Blue")
        .jsonPath("$.face.code").isEqualTo("OVAL")
        .jsonPath("$.face.description").isEqualTo("Oval")
    }
  }

  @Nested
  inner class UpdatePhysicalAttributes {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/physical-attributes")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_PHYSICAL_ATTRIBUTES_UPDATE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/physical-attributes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_PHYSICAL_ATTRIBUTES_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 204 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/physical-attributes")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_PHYSICAL_ATTRIBUTES_UPDATE)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `returns 404 when offender does not exist`() {
      webTestClient.put()
        .uri("/api/offenders/AAA444/core-person-record/physical-attributes")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_PHYSICAL_ATTRIBUTES_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage")
        .isEqualTo("Prisoner with prisonerNumber AAA444 and existing booking not found")
    }

    @Test
    fun `returns status 423 (locked) when database row lock times out`() {
      doThrow(DatabaseRowLockedException("developer message"))
        .whenever(prisonerProfileUpdateService).updatePhysicalAttributes(anyString(), any())

      webTestClient.put()
        .uri("/api/offenders/A1234AA/core-person-record/physical-attributes")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_PHYSICAL_ATTRIBUTES_UPDATE)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.LOCKED)
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource locked, possibly in use in P-Nomis.")
        .jsonPath("developerMessage").isEqualTo("developer message")
    }
  }

  private companion object {
    const val VALID_PHYSICAL_ATTRIBUTES_UPDATE =
      // language=json
      """
        {
          "height": 180,
          "weight": 75,
          "hairCode": "GREY",
          "facialHairCode": "BEARDED",
          "faceCode": "ROUND",
          "buildCode": "MEDIUM",
          "leftEyeColourCode": "BLUE",
          "rightEyeColourCode": "BLUE",
          "shoeSize": "10"
        }
      """
  }
}
