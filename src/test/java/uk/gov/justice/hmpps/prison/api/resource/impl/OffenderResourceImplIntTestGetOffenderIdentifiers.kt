package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
import java.time.LocalDate
import java.time.LocalDateTime

class OffenderResourceImplIntTestGetOffenderIdentifiers : ResourceTest() {
  private val staffUserAccount = StaffUserAccount.builder().username("johnsmith").staff(Staff.builder().firstName("John").lastName("Smith").build())

  private fun stubRepositoryCall() {
    whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(ArgumentMatchers.anyString())).thenReturn(
      listOf(
        OffenderIdentifier().apply {
          identifierType = "TYPE"
          identifier = "IDENTIFIER"
          issuedAuthorityText = "Comment"
          issuedDate = LocalDate.parse("2024-01-01")
          caseloadType = "GENERAL"
          createDateTime = LocalDateTime.parse("2023-01-01T00:00:00")
          rootOffenderId = 123
          offender = Offender().apply {
            id = 123
            rootOffender = Offender().apply { id = 123 }
            addBooking(OffenderBooking().apply { bookingId = 1001 })
            nomsId = "A1234BC"
          }
        },

        OffenderIdentifier().apply {
          identifierType = "TYPE_ALIAS"
          identifier = "IDENTIFIER_ALIAS"
          issuedAuthorityText = "Comment"
          issuedDate = LocalDate.parse("2024-01-01")
          caseloadType = "GENERAL"
          createDateTime = LocalDateTime.parse("2023-01-01T00:00:00")
          rootOffenderId = 123
          offender = Offender().apply {
            id = 321
            rootOffender = Offender().apply { id = 123 }
            addBooking(OffenderBooking().apply { bookingId = 1001 })
            nomsId = "A1234BC"
          }
        },
      ),
    )
  }

  @MockBean
  private lateinit var offenderIdentifierRepository: OffenderIdentifierRepository

  @Test
  fun `should return 401 when user does not have token`() {
    webTestClient.get().uri("/api/offenders/ABC123AB/offender-identifiers").exchange().expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if no override role`() {
    webTestClient.get().uri("/api/offenders/ABC123AB/offender-identifiers")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
    stubRepositoryCall()
    webTestClient.get().uri("/api/offenders/ABC123AB/offender-identifiers")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `should return offender identifiers without aliases`() {
    stubRepositoryCall()
    webTestClient.get().uri("/api/offenders/ABC123AB/offender-identifiers")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectBody()
      .jsonPath("$.length()").isEqualTo(1)
      .jsonPath("[0].type").isEqualTo("TYPE")
      .jsonPath("[0].value").isEqualTo("IDENTIFIER")
      .jsonPath("[0].offenderNo").isEqualTo("A1234BC")
      .jsonPath("[0].issuedAuthorityText").isEqualTo("Comment")
      .jsonPath("[0].issuedDate").isEqualTo("2024-01-01")
      .jsonPath("[0].caseloadType").isEqualTo("GENERAL")
      .jsonPath("[0].whenCreated").isEqualTo("2023-01-01T00:00:00")
      .jsonPath("[0].offenderId").isEqualTo("123")
      .jsonPath("[0].rootOffenderId").isEqualTo("123")
  }

  @Test
  fun `should return offender identifiers with aliases`() {
    stubRepositoryCall()
    webTestClient.get().uri("/api/offenders/ABC123AB/offender-identifiers?includeAliases=true")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectBody()
      .jsonPath("$.length()").isEqualTo(2)
      .jsonPath("[1].type").isEqualTo("TYPE_ALIAS")
      .jsonPath("[1].value").isEqualTo("IDENTIFIER_ALIAS")
  }
}
