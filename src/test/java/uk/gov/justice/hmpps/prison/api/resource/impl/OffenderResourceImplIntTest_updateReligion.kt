@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
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
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
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

class OffenderResourceImplIntTest_updateReligion : ResourceTest() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @Autowired
  lateinit var offenderProfileDetailRepository: OffenderProfileDetailRepository

  @Autowired
  lateinit var profileTypeRepository: ProfileTypeRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @SpyBean
  lateinit var prisonerProfileUpdateService: PrisonerProfileUpdateService

  @Nested
  inner class Authorisation {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 204 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  open inner class HappyPath {
    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.OffenderResourceImplIntTest_updateReligion#updateReligionIds")
    @Transactional(readOnly = true)
    open fun `should update the religion`(prisonerId: String, id: Long) {
      webTestClient.put()
        .uri("api/offenders/$prisonerId/religion")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNoContent

      val booking = offenderRepository.findById(id).get().allBookings.first { it.bookingSequence == 1 }
      assertThat(
        offenderProfileDetailRepository.findById(OffenderProfileDetail.PK(booking, religionProfileType(), 1)).get().code.id.code,
      ).isEqualTo("DRU")
    }

    @Test
    @Transactional(readOnly = true)
    open fun `should allow religion to be removed`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(NULL_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNoContent

      val booking = offenderRepository.findById(-1001L).get().allBookings.first { it.bookingSequence == 1 }
      assertThat(
        offenderProfileDetailRepository.findById(OffenderProfileDetail.PK(booking, religionProfileType(), 1)),
      ).isEmpty
    }

    private fun religionProfileType(): ProfileType =
      profileTypeRepository.findByTypeAndCategoryAndActive("RELF", "PI", true).get()
  }

  @Nested
  inner class ErrorConditions {
    @Test
    fun shouldReturn404WhenOffenderDoesNotExist() {
      webTestClient.put()
        .uri("api/offenders/AAA444/religion")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage")
        .isEqualTo("Prisoner with prisonerNumber AAA444 and existing booking not found")
    }

    @Test
    fun shouldReturn404WhenOffenderHasNoBooking() {
      webTestClient.put()
        .uri("api/offenders/A9880GH/religion")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage")
        .isEqualTo("Prisoner with prisonerNumber A9880GH and existing booking not found")
    }

    @Test
    fun `returns status 423 (locked) when database row lock times out`() {
      doThrow(DatabaseRowLockedException("developer message"))
        .whenever(prisonerProfileUpdateService).updateReligionOfLatestBooking(anyString(), anyString())

      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.LOCKED)
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource locked, possibly in use in P-Nomis.")
        .jsonPath("developerMessage").isEqualTo("developer message")
    }
  }

  private companion object {
    const val VALID_RELIGION_UPDATE =
      // language=json
      """
        {
          "religion": "DRU"
        }
      """

    const val NULL_RELIGION_UPDATE =
      // language=json
      """
        {
          "religion": null 
        }
      """

    @JvmStatic
    fun updateReligionIds(): Stream<Arguments> {
      return Stream.of(
        arguments(named("Prisoner with existing religion", "A1234AA"), -1001L),
        arguments(named("Prisoner without existing religion", "A1068AA"), -1068L),
      )
    }
  }
}
