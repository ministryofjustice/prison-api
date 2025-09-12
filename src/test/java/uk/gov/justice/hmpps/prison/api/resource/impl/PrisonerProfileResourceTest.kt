@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.returnResult
import uk.gov.justice.hmpps.prison.api.model.CorePersonPhysicalAttributes
import uk.gov.justice.hmpps.prison.api.model.CorePersonRecordAlias
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords
import uk.gov.justice.hmpps.prison.api.model.PrisonerProfileSummaryDto
import uk.gov.justice.hmpps.prison.api.model.ReferenceDataValue
import uk.gov.justice.hmpps.prison.service.PrisonerProfilePersonService
import java.time.LocalDate

class PrisonerProfileResourceTest : ResourceTest() {

  @MockitoSpyBean
  lateinit var prisonerProfilePersonService: PrisonerProfilePersonService

  @Nested
  @DisplayName("GET /offenders/{offenderNo}/profile-summary")
  inner class GetFullPerson {

    @Nested
    @DisplayName("Authorisation checks")
    inner class Authorisation {

      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.get()
          .uri("api/offenders/A1072AA/profile-summary")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.get()
          .uri("api/offenders/A1072AA/profile-summary")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.get()
          .uri("api/offenders/A1072AA/profile-summary")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 200 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
        webTestClient.get()
          .uri("api/offenders/A1072AA/profile-summary")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isOk
      }
    }

    @Nested
    @DisplayName("Happy path")
    inner class HappyPath {

      @Test
      fun `should retrieve full person data`() {
        val person = webTestClient.get()
          .uri("api/offenders/A1234AL/profile-summary")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isOk
          .returnResult<PrisonerProfileSummaryDto>()
          .responseBody
          .blockFirst()

        assertThat(person).isEqualTo(expectedPerson)
      }
    }

    @Nested
    @DisplayName("Error conditions")
    inner class ErrorConditions {

      @Test
      fun `returns 404 when prisoner does not exist`() {
        webTestClient.get()
          .uri("api/offenders/XXXX/profile-summary")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage").isEqualTo("Prisoner with prisonerNumber XXXX not found")
      }

      @Test
      fun `returns 500 when an unexpected error occurs`() {
        doThrow(RuntimeException("Unexpected error"))
          .whenever(prisonerProfilePersonService).getPrisonerProfileSummary("A1072AA")

        webTestClient.get()
          .uri("api/offenders/A1072AA/profile-summary")
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
          .exchange()
          .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
          .expectBody()
          .jsonPath("developerMessage").isEqualTo("Unexpected error")
      }
    }
  }

  private val expectedPerson = PrisonerProfileSummaryDto(
    aliases = listOf(
      CorePersonRecordAlias(
        prisonerNumber = "A1234AL",
        offenderId = -1013,
        isWorkingName = false,
        firstName = "DANNY",
        middleName1 = null,
        middleName2 = null,
        lastName = "SMILEY",
        dateOfBirth = LocalDate.parse("1995-08-21"),
        nameType = ReferenceDataValue(
          domain = "NAME_TYPE",
          code = "NICK",
          description = "Nickname",
        ),
        title = null,
        sex = ReferenceDataValue(
          domain = "SEX",
          code = "M",
          description = "Male",
        ),
        ethnicity = ReferenceDataValue(
          domain = "ETHNICITY",
          code = "W2",
          description = "White: Irish",
        ),
      ),
      CorePersonRecordAlias(
        prisonerNumber = "A1234AL",
        offenderId = -1012,
        isWorkingName = true,
        firstName = "DANIEL",
        middleName1 = "JOHN",
        middleName2 = null,
        lastName = "SMELLEY",
        dateOfBirth = LocalDate.parse("1968-01-01"),
        nameType = null,
        title = null,
        sex = ReferenceDataValue(
          domain = "SEX",
          code = "M",
          description = "Male",
        ),
        ethnicity = ReferenceDataValue(
          domain = "ETHNICITY",
          code = "W2",
          description = "White: Irish",
        ),
      ),
    ),
    addresses = emptyList(),
    phones = emptyList(),
    emails = emptyList(),
    militaryRecord = MilitaryRecords(
      militaryRecords = emptyList(),
    ),
    physicalAttributes = CorePersonPhysicalAttributes(
      height = null,
      weight = null,
      hair = null,
      facialHair = null,
      face = null,
      build = null,
      leftEyeColour = null,
      rightEyeColour = null,
      shoeSize = null,
    ),
    distinguishingMarks = emptyList(),
  )
}
