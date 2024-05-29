package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository

class OffenderIdentityServiceTest {
  private val offenderIdentifierRepository: OffenderIdentifierRepository = mock()
  private val offenderIdentifierService: OffenderIdentifierService =
    OffenderIdentifierService(offenderIdentifierRepository)

  @Test
  internal fun `getOffenderIdentifiers for prisoner number`() {
    whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(any())).thenReturn(
      listOf(
        OffenderIdentifier().apply {
          identifierType = "TYPE"
          identifier = "IDENTIFIER"
          rootOffenderId = 123
          offender = Offender().apply {
            id = 123
            rootOffender = Offender().apply { id = 123 }
            addBooking(OffenderBooking().apply { bookingId = 1001 })
          }
        },
      ),
    )

    val identifiers = offenderIdentifierService.getOffenderIdentifiers("ABC123", false)
    assertThat(identifiers).hasSize(1)
    assertThat(identifiers[0].offenderId).isEqualTo(123)
  }

  @Test
  internal fun `getOffenderIdentifiers with aliases does not filter out aliases`() {
    whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(any())).thenReturn(
      listOf(
        OffenderIdentifier().apply {
          identifierType = "TYPE"
          identifier = "IDENTIFIER"
          rootOffenderId = 123
          offender = Offender().apply {
            id = 123
            rootOffender = Offender().apply { id = 123 }
            addBooking(OffenderBooking().apply { bookingId = 1001 })
          }
        },
        OffenderIdentifier().apply {
          identifierType = "TYPE"
          identifier = "IDENTIFIER_ALIAS"
          rootOffenderId = 123
          offender = Offender().apply {
            id = 543
            rootOffender = Offender().apply { id = 123 }
            addBooking(OffenderBooking().apply { bookingId = 1001 })
          }
        },
      ),
    )

    val identifiers = offenderIdentifierService.getOffenderIdentifiers("ABC123", true)
    assertThat(identifiers).hasSize(2)
    assertThat(identifiers[0].offenderId).isEqualTo(123)
    assertThat(identifiers[0].value).isEqualTo("IDENTIFIER")
    assertThat(identifiers[1].offenderId).isEqualTo(543)
    assertThat(identifiers[1].value).isEqualTo("IDENTIFIER_ALIAS")
  }
}
