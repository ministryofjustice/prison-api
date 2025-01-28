@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
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
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.PrisonerRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBeliefRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProfileDetailRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateService
import java.time.LocalDate

class OffenderResourceImplIntTest_updateReligion : ResourceTest() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @Autowired
  lateinit var offenderProfileDetailRepository: OffenderProfileDetailRepository

  @Autowired
  lateinit var profileTypeRepository: ProfileTypeRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var offenderBeliefRepository: OffenderBeliefRepository

  @Autowired
  lateinit var staffUserAccountRepository: StaffUserAccountRepository

  @MockitoSpyBean
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
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when supplied roles do not include PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setAuthorisation(listOf("ROLE_BANANAS")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 204 when supplied role includes PRISON_API__PRISONER_PROFILE__RW`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  open inner class HappyPath {
    @Test
    @Transactional(readOnly = true)
    open fun `should update the religion`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNoContent

      val booking = offenderRepository.findById(-1001L).get().allBookings.first { it.bookingSequence == 1 }
      val history = offenderBeliefRepository.getOffenderBeliefHistory("A1234AA", booking.bookingId.toString())

      val historyEntry = history[0]
      assertThat(historyEntry.beliefCode.id.code).isEqualTo("DRU")
      assertThat(historyEntry.changeReason).isTrue()
      assertThat(historyEntry.comments).isEqualTo("Some comment")
      assertThat(historyEntry.verified).isTrue()
      assertThat(historyEntry.startDate).isEqualTo(LocalDate.parse("2025-01-01"))
    }

    @Test
    @Transactional(readOnly = true)
    open fun `should update the religion with a minimal update request`() {
      webTestClient.put()
        .uri("api/offenders/A1068AA/religion")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_MINIMAL_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNoContent

      val booking = offenderRepository.findById(-1068L).get().allBookings.first { it.bookingSequence == 1 }
      val history = offenderBeliefRepository.getOffenderBeliefHistory("A1068AA", booking.bookingId.toString())
      val historyEntry = history[0]
      assertThat(historyEntry.beliefCode.id.code).isEqualTo("DRU")
      assertThat(historyEntry.changeReason).isFalse()
      assertThat(historyEntry.comments).isNull()
      assertThat(historyEntry.verified).isFalse()
      assertThat(historyEntry.startDate).isEqualTo(LocalDate.now())
    }

    @Test
    @Transactional(readOnly = true)
    open fun `should not allow religion to be removed`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(NULL_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Malformed request")
    }

    @Test
    @Transactional(readOnly = true)
    open fun `should prevent invalid date format`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(INVALID_DATE_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Malformed request")
    }

    private fun religionProfileType(): ProfileType = profileTypeRepository.findByTypeAndCategoryAndActive("RELF", "PI", true).get()
  }

  @Nested
  inner class ErrorConditions {
    @Test
    fun `returns 404 when offender does not exist`() {
      webTestClient.put()
        .uri("api/offenders/AAA444/religion")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage")
        .isEqualTo("Prisoner with prisonerNumber AAA444 and existing booking not found")
    }

    @Test
    fun `returns 404 when offender has no booking`() {
      webTestClient.put()
        .uri("api/offenders/A9880GH/religion")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage")
        .isEqualTo("Prisoner with prisonerNumber A9880GH and existing booking not found")
    }

    @Test
    fun `returns 404 when a staff user account is not found for the provided username`() {
      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(VALID_RELIGION_UPDATE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage")
        .isEqualTo("Staff user account with provided username not found")
    }

    @Test
    fun `returns status 423 (locked) when database row lock times out`() {
      doThrow(DatabaseRowLockedException("developer message"))
        .whenever(prisonerProfileUpdateService).updateReligionOfLatestBooking(anyString(), any(), anyString())

      webTestClient.put()
        .uri("api/offenders/A1234AA/religion")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
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
          "religion": "DRU",
          "comment": "Some comment",
          "effectiveFromDate": "2025-01-01",
          "verified": true
        }
      """

    const val VALID_MINIMAL_RELIGION_UPDATE =
      // language=json
      """
        {
          "religion": "DRU"
        }
      """

    const val INVALID_DATE_RELIGION_UPDATE =
      // language=json
      """
        {
          "religion": "DRU",
          "effectiveFromDate": "1st January 2025"
        }
      """

    const val NULL_RELIGION_UPDATE =
      // language=json
      """
        {
          "religion": null 
        }
      """
  }
}
