@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.PrisonerRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProfileDetailRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository
import uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateService
import java.util.stream.Stream

class OffenderResourceImplIntTest_updateNationality : ResourceTest() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @Autowired
  lateinit var offenderProfileDetailRepository: OffenderProfileDetailRepository

  @Autowired
  lateinit var profileTypeRepository: ProfileTypeRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @MockitoSpyBean
  lateinit var prisonerProfileUpdateService: PrisonerProfileUpdateService

  @Nested
  inner class Authorisation {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/nationality")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_NATIONALITY_UPDATE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/nationality")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_NATIONALITY_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/nationality")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_NATIONALITY_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 204 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/nationality")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_NATIONALITY_UPDATE)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  open inner class HappyPath {
    @DisplayName("should update the nationality")
    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceImplIntTest_updateNationality#updateNationalityIds")
    @Transactional(readOnly = true)
    open fun `should update the nationality`(prisonerId: String, id: Long) {
      webTestClient.put()
        .uri("api/offenders/$prisonerId/nationality")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_NATIONALITY_UPDATE)
        .exchange()
        .expectStatus().isNoContent

      val booking = offenderRepository.findById(id).get().allBookings.first { it.bookingSequence == 1 }
      assertThat(
        offenderProfileDetailRepository.findById(OffenderProfileDetail.PK(booking, nationalityProfileType(), 1)).get().code.id.code,
      ).isEqualTo("BRIT")
    }

    @Test
    @Transactional(readOnly = true)
    open fun `should allow nationality to be removed`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/nationality")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(NULL_NATIONALITY_UPDATE)
        .exchange()
        .expectStatus().isNoContent

      val booking = offenderRepository.findById(-1001L).get().allBookings.first { it.bookingSequence == 1 }
      assertThat(
        offenderProfileDetailRepository.findById(OffenderProfileDetail.PK(booking, nationalityProfileType(), 1)),
      ).isEmpty
    }

    private fun nationalityProfileType(): ProfileType = profileTypeRepository.findByTypeAndCategoryAndActive("NAT", "PI", true).get()
  }

  @Nested
  inner class ErrorConditions {
    @Test
    fun shouldReturn404WhenOffenderDoesNotExist() {
      webTestClient.put()
        .uri("api/offenders/AAA444/nationality")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_NATIONALITY_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage")
        .isEqualTo("Prisoner with prisonerNumber AAA444 and existing booking not found")
    }

    @Test
    fun shouldReturn404WhenOffenderHasNoBooking() {
      webTestClient.put()
        .uri("api/offenders/A9880GH/nationality")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_NATIONALITY_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage")
        .isEqualTo("Prisoner with prisonerNumber A9880GH and existing booking not found")
    }

    @Test
    fun `returns status 423 (locked) when database row lock times out`() {
      doThrow(DatabaseRowLockedException("developer message"))
        .whenever(prisonerProfileUpdateService).updateNationalityOfLatestBooking(anyString(), anyString())

      webTestClient.put()
        .uri("api/offenders/A1234AA/nationality")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_NATIONALITY_UPDATE)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.LOCKED)
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource locked, possibly in use in P-Nomis.")
        .jsonPath("developerMessage").isEqualTo("developer message")
    }
  }

  private companion object {
    const val VALID_NATIONALITY_UPDATE =
      // language=json
      """
        {
          "nationality": "BRIT"
        }
      """

    const val NULL_NATIONALITY_UPDATE =
      // language=json
      """
        {
          "nationality": null 
        }
      """

    @JvmStatic
    fun updateNationalityIds(): Stream<Arguments> = Stream.of(
      arguments(named("Prisoner with existing nationality", "A1234AA"), -1001L),
      arguments(named("Prisoner without existing nationality", "A1068AA"), -1068L),
    )
  }
}
