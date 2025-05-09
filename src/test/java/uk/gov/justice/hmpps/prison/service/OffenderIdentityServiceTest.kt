package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifierCreateRequest
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifierUpdateRequest
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import java.util.Optional

class OffenderIdentityServiceTest {

  private val offenderIdentifierRepository: OffenderIdentifierRepository = mock()
  private val offenderRepository: OffenderRepository = mock()
  private val referenceDomainService: ReferenceDomainService = mock()
  private val offenderIdentifierService: OffenderIdentifierService =
    OffenderIdentifierService(offenderIdentifierRepository, offenderRepository, referenceDomainService)

  private lateinit var testIdentifier: OffenderIdentifier
  private lateinit var testUpdatedIdentifier: OffenderIdentifier
  private lateinit var testIdentifierOnAlias: OffenderIdentifier
  private lateinit var testCroIdentifier: OffenderIdentifier
  private lateinit var testOffender: Offender

  @BeforeEach
  fun setUp() {
    testIdentifier = OffenderIdentifier.builder()
      .identifierType("NINO")
      .identifier("IDENTIFIER")
      .rootOffenderId(123L)
      .offender(Offender().apply { id = 123 })
      .offenderIdentifierPK(OffenderIdentifier.OffenderIdentifierPK(123, 1))
      .build()

    testIdentifierOnAlias = OffenderIdentifier.builder()
      .identifierType("NINO")
      .identifier("IDENTIFIER_ON_ALIAS")
      .rootOffenderId(543L)
      .offender(Offender().apply { id = 543 })
      .offenderIdentifierPK(OffenderIdentifier.OffenderIdentifierPK(543, 2))
      .build()

    testCroIdentifier = OffenderIdentifier.builder()
      .identifierType("CRO")
      .identifier("265416/21G")
      .rootOffenderId(123L)
      .offender(Offender().apply { id = 123 })
      .offenderIdentifierPK(OffenderIdentifier.OffenderIdentifierPK(123, 2))
      .build()

    testUpdatedIdentifier = OffenderIdentifier.builder()
      .identifierType("NINO")
      .identifier("UPDATED_IDENTIFIER")
      .issuedAuthorityText("COMMENT")
      .rootOffenderId(123L)
      .offender(Offender().apply { id = 123 })
      .offenderIdentifierPK(OffenderIdentifier.OffenderIdentifierPK(123, 1))
      .build()

    testOffender = Offender().apply {
      id = 123
      nomsId = "ABC123"
      rootOffenderId = 123
      rootOffender = Offender().apply { id = 123 }
      addBooking(OffenderBooking().apply { bookingId = 1001 })
      identifiers = mutableListOf(testIdentifier)
    }
  }

  @Test
  fun `getOffenderIdentifiers for prisoner number`() {
    whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(any())).thenReturn(
      listOf(testIdentifier),
    )

    val identifiers = offenderIdentifierService.getOffenderIdentifiers("ABC123", false)
    assertThat(identifiers).hasSize(1)
    assertThat(identifiers[0].offenderId).isEqualTo(123)
  }

  @Test
  fun `getOffenderIdentifiers with aliases does not filter out aliases`() {
    whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(any())).thenReturn(
      listOf(testIdentifier, testIdentifierOnAlias),
    )

    val identifiers = offenderIdentifierService.getOffenderIdentifiers("ABC123", true)
    assertThat(identifiers).hasSize(2)
    assertThat(identifiers[0].offenderId).isEqualTo(123)
    assertThat(identifiers[0].value).isEqualTo("IDENTIFIER")
    assertThat(identifiers[1].offenderId).isEqualTo(543)
    assertThat(identifiers[1].value).isEqualTo("IDENTIFIER_ON_ALIAS")
  }

  @Test
  fun `getOffenderIdentifier returns a single identifier`() {
    whenever(
      offenderIdentifierRepository.findByPrisonerNumberAndOffenderIdSeq(
        eq("ABC123"),
        eq(1L),
      ),
    ).thenReturn(Optional.of(testIdentifier))

    val identifier = offenderIdentifierService.getOffenderIdentifier("ABC123", 1L)
    assertThat(identifier.type).isEqualTo("NINO")
    assertThat(identifier.value).isEqualTo("IDENTIFIER")
    assertThat(identifier.offenderId).isEqualTo(123)
  }

  @Test
  fun `getOffenderIdentifier throws exception when identifier not found`() {
    whenever(
      offenderIdentifierRepository.findByPrisonerNumberAndOffenderIdSeq(
        any(),
        anyLong(),
      ),
    ).thenReturn(Optional.empty())

    assertThatThrownBy { offenderIdentifierService.getOffenderIdentifier("ABC123", 1L) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Offender identifier for prisoner ABC123 with sequence 1 not found")
  }

  @Test
  fun `addOffenderIdentifiers adds multiple identifiers`() {
    whenever(offenderRepository.findLinkedToLatestBooking("ABC123")).thenReturn(Optional.of(testOffender))
    whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(eq("ABC123")))
      .thenReturn(listOf(testIdentifier))
    whenever(offenderIdentifierRepository.saveAll(any<List<OffenderIdentifier>>()))
      .thenReturn(listOf(testCroIdentifier))
    whenever(referenceDomainService.isReferenceCodeActive(any(), any())).thenReturn(true)

    val requests = listOf(OffenderIdentifierCreateRequest("CRO", "265416/21G", null))
    val identifiers = offenderIdentifierService.addOffenderIdentifiers("ABC123", requests)

    assertThat(identifiers).hasSize(1)
    assertThat(identifiers[0].type).isEqualTo("CRO")
    assertThat(identifiers[0].value).isEqualTo("265416/21G")
  }

  @Test
  fun `addOffenderIdentifiers throws exception when offender not found`() {
    whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(any()))
      .thenReturn(emptyList())

    val requests = listOf(OffenderIdentifierCreateRequest("CRO", "265416/21G", null))
    assertThatThrownBy { offenderIdentifierService.addOffenderIdentifiers("ABC123", requests) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Offender with prisoner number ABC123 not found")
  }

  @Test
  fun `updateOffenderIdentifier updates an existing identifier`() {
    whenever(
      offenderIdentifierRepository.findByPrisonerNumberAndOffenderIdSeq(
        eq("ABC123"),
        eq(1L),
      ),
    ).thenReturn(Optional.of(testIdentifier))
    whenever(offenderIdentifierRepository.save(any<OffenderIdentifier>()))
      .thenReturn(testUpdatedIdentifier)
    whenever(referenceDomainService.isReferenceCodeActive(any(), any())).thenReturn(true)

    val request = OffenderIdentifierUpdateRequest("UPDATED_IDENTIFIER", "COMMENT")
    offenderIdentifierService.updateOffenderIdentifier("ABC123", 1L, request)

    verify(offenderIdentifierRepository).save(any<OffenderIdentifier>())
  }

  @Test
  fun `updateOffenderIdentifier throws exception when identifier not found`() {
    whenever(
      offenderIdentifierRepository.findByPrisonerNumberAndOffenderIdSeq(
        any(),
        anyLong(),
      ),
    ).thenReturn(Optional.empty())

    val request = OffenderIdentifierUpdateRequest("UPDATED_IDENTIFIER", "COMMENT")
    assertThatThrownBy { offenderIdentifierService.updateOffenderIdentifier("ABC123", 1L, request) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Offender identifier for prisoner ABC123 with sequence 1 not found")
  }

  @Nested
  inner class Validation {
    @BeforeEach
    fun setUp() {
      whenever(offenderRepository.findLinkedToLatestBooking("A1234BC")).thenReturn(Optional.of(testOffender))
      whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(eq("A1234BC")))
        .thenReturn(listOf(testIdentifier))
      whenever(offenderIdentifierRepository.saveAll(any<List<OffenderIdentifier>>()))
        .thenReturn(listOf(testCroIdentifier))
      whenever(referenceDomainService.isReferenceCodeActive(any(), any())).thenReturn(true)
    }

    @Nested
    inner class PncValidation {

      @ParameterizedTest
      @ValueSource(strings = ["2000/0160946Q", "1999/0044342D", "2020/0074062F", "20/83779T", "20/0446870G"])
      fun `accepts valid PNC`(pnc: String) {
        val req = OffenderIdentifierCreateRequest("PNC", pnc, null)
        val result = offenderIdentifierService.addOffenderIdentifiers("A1234BC", listOf(req))
        assertThat(result).hasSize(1)
      }

      @Test
      fun `rejects invalid PNC`() {
        val req = OffenderIdentifierCreateRequest("PNC", "12/1234567B", null)
        assertThatThrownBy { offenderIdentifierService.addOffenderIdentifiers("A1234BC", listOf(req)) }
          .hasMessageContaining("is not valid")
      }
    }

    @Nested
    inner class CroValidation {
      @ParameterizedTest
      @ValueSource(
        strings = [
          "265416/21G",
          "288242/01M",
          "41504/71A",
          "016789/71L",
          "SF83/50058Z",
          "SF80/41396X",
          "SF89/25862Y",
        ],
      )
      fun `accepts valid CRO`(cro: String) {
        val req = OffenderIdentifierCreateRequest("CRO", cro, null)
        val result = offenderIdentifierService.addOffenderIdentifiers("A1234BC", listOf(req))
        assertThat(result).hasSize(1)
      }

      @Test
      fun `rejects invalid CRO`() {
        val req = OffenderIdentifierCreateRequest("CRO", "123456/99B", null)
        assertThatThrownBy { offenderIdentifierService.addOffenderIdentifiers("A1234BC", listOf(req)) }
          .hasMessageContaining("is not valid")
      }
    }
  }
}
