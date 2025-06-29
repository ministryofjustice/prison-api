package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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
  private lateinit var testPncIdentifier: OffenderIdentifier
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

    testPncIdentifier = OffenderIdentifier.builder()
      .identifierType("PNC")
      .identifier("96/346527V")
      .rootOffenderId(123L)
      .offender(Offender().apply { id = 123 })
      .offenderIdentifierPK(OffenderIdentifier.OffenderIdentifierPK(123, 2))
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

  @Nested
  @DisplayName("getOffenderIdentifiers")
  inner class GetIdentifiers {
    @Test
    fun `get identifiers for prisoner number`() {
      whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(any())).thenReturn(
        listOf(testIdentifier),
      )

      val identifiers = offenderIdentifierService.getOffenderIdentifiers("ABC123", false)
      assertThat(identifiers).hasSize(1)
      assertThat(identifiers[0].offenderId).isEqualTo(123)
    }

    @Test
    fun `get with aliases does not filter out aliases`() {
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
  }

  @Nested
  @DisplayName("getOffenderIdentifierForAlias")
  inner class GetIdentifier {
    @Test
    fun `returns a single identifier`() {
      whenever(
        offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
          eq(1L),
          eq(1L),
        ),
      ).thenReturn(Optional.of(testIdentifier))

      val identifier = offenderIdentifierService.getOffenderIdentifierForAlias(1L, 1L)
      assertThat(identifier.type).isEqualTo("NINO")
      assertThat(identifier.value).isEqualTo("IDENTIFIER")
      assertThat(identifier.offenderId).isEqualTo(123)
    }

    @Test
    fun `throws exception when identifier not found`() {
      whenever(
        offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
          anyLong(),
          anyLong(),
        ),
      ).thenReturn(Optional.empty())

      assertThatThrownBy { offenderIdentifierService.getOffenderIdentifierForAlias(1L, 1L) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessageContaining("Offender identifier for alias (offenderId) 1 with sequence 1 not found")
    }
  }

  @Nested
  @DisplayName("addOffenderIdentifiers")
  inner class AddIdentifiers {
    @Test
    fun `adds multiple identifiers`() {
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
    fun `does not allow adding a duplicate identifier`() {
      whenever(offenderRepository.findLinkedToLatestBooking("ABC123")).thenReturn(Optional.of(testOffender))
      whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(eq("ABC123")))
        .thenReturn(listOf(testIdentifier))
      whenever(offenderIdentifierRepository.saveAll(any<List<OffenderIdentifier>>()))
        .thenReturn(listOf(testCroIdentifier))
      whenever(referenceDomainService.isReferenceCodeActive(any(), any())).thenReturn(true)

      val requests = listOf(OffenderIdentifierCreateRequest(testIdentifier.identifierType, testIdentifier.identifier, null))
      assertThatThrownBy { offenderIdentifierService.addOffenderIdentifiers("ABC123", requests) }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("Identifier IDENTIFIER already exists for prisoner ABC123")
    }

    @Test
    fun `throws exception when offender not found`() {
      whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(any()))
        .thenReturn(emptyList())

      val requests = listOf(OffenderIdentifierCreateRequest("CRO", "265416/21G", null))
      assertThatThrownBy { offenderIdentifierService.addOffenderIdentifiers("ABC123", requests) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessageContaining("Offender with prisoner number ABC123 not found")
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

  @Nested
  @DisplayName("updateOffenderIdentifierForAlias")
  inner class UpdateIdentifier {
    @Test
    fun `updates an existing identifier`() {
      val identifier: OffenderIdentifier = mock()
      whenever(identifier.identifierType).thenReturn("NINO")
      whenever(identifier.offender).thenReturn(testOffender)
      whenever(identifier.offenderIdentifierPK).thenReturn(testIdentifier.offenderIdentifierPK)
      whenever(
        offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
          eq(testOffender.id),
          eq(1L),
        ),
      ).thenReturn(Optional.of(identifier))
      whenever(offenderRepository.findById(testOffender.id)).thenReturn(Optional.of(testOffender))
      whenever(referenceDomainService.isReferenceCodeActive(any(), any())).thenReturn(true)

      val request = OffenderIdentifierUpdateRequest("UPDATED_IDENTIFIER", "COMMENT")
      offenderIdentifierService.updateOffenderIdentifierForAlias(testOffender.id, 1L, request)

      verify(identifier).identifier = "UPDATED_IDENTIFIER"
      verify(identifier).issuedAuthorityText = "COMMENT"
    }

    @Test
    fun `allows update of issuedAuthorityText without changing identifier value`() {
      val identifier: OffenderIdentifier = mock()
      whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(any())).thenReturn(
        listOf(testIdentifier),
      )
      whenever(identifier.identifierType).thenReturn("NINO")
      whenever(identifier.offender).thenReturn(testOffender)
      whenever(identifier.offenderIdentifierPK).thenReturn(testIdentifier.offenderIdentifierPK)
      whenever(
        offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
          eq(testOffender.id),
          eq(1L),
        ),
      ).thenReturn(Optional.of(identifier))
      whenever(offenderRepository.findById(testOffender.id)).thenReturn(Optional.of(testOffender))
      whenever(referenceDomainService.isReferenceCodeActive(any(), any())).thenReturn(true)

      val request = OffenderIdentifierUpdateRequest("IDENTIFIER", "COMMENT")
      offenderIdentifierService.updateOffenderIdentifierForAlias(testOffender.id, 1L, request)

      verify(identifier).identifier = "IDENTIFIER"
      verify(identifier).issuedAuthorityText = "COMMENT"
    }

    @Test
    fun `does not allow updating to a duplicate value`() {
      whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(any())).thenReturn(
        listOf(testIdentifier, testIdentifierOnAlias),
      )
      whenever(
        offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
          eq(testOffender.id),
          eq(1L),
        ),
      ).thenReturn(Optional.of(testIdentifier))
      whenever(offenderRepository.findById(testOffender.id)).thenReturn(Optional.of(testOffender))
      whenever(referenceDomainService.isReferenceCodeActive(any(), any())).thenReturn(true)

      val request = OffenderIdentifierUpdateRequest(testIdentifierOnAlias.identifier, "COMMENT")
      assertThatThrownBy {
        offenderIdentifierService.updateOffenderIdentifierForAlias(testOffender.id, 1L, request)
      }.isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("Identifier IDENTIFIER_ON_ALIAS already exists for prisoner ABC123")
    }

    @Test
    fun `throws exception when identifier not found`() {
      whenever(
        offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
          anyLong(),
          anyLong(),
        ),
      ).thenReturn(Optional.empty())

      val request = OffenderIdentifierUpdateRequest("UPDATED_IDENTIFIER", "COMMENT")
      assertThatThrownBy { offenderIdentifierService.updateOffenderIdentifierForAlias(1L, 1L, request) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessageContaining("Offender identifier for alias (offenderId) 1 with sequence 1 not found")
    }

    @Nested
    inner class Validation {
      @BeforeEach
      fun setUp() {
        whenever(offenderRepository.findById(1)).thenReturn(Optional.of(testOffender))
        whenever(offenderRepository.findLinkedToLatestBooking("A1234BC")).thenReturn(Optional.of(testOffender))
        whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(eq("A1234BC")))
          .thenReturn(listOf(testIdentifier, testCroIdentifier, testPncIdentifier))
      }

      @Nested
      inner class PncValidation {

        @ParameterizedTest
        @ValueSource(strings = ["2000/0160946Q", "1999/0044342D", "2020/0074062F", "20/83779T", "20/0446870G"])
        fun `accepts valid PNC`(pnc: String) {
          whenever(
            offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
              eq(1),
              eq(1),
            ),
          ).thenReturn(Optional.of(testPncIdentifier))

          val req = OffenderIdentifierUpdateRequest(pnc, null)
          val result = offenderIdentifierService.updateOffenderIdentifierForAlias(1, 1, req)
          assertThat(result.value).isEqualTo(pnc)
        }

        @Test
        fun `rejects invalid PNC`() {
          whenever(
            offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
              eq(1),
              eq(1),
            ),
          ).thenReturn(Optional.of(testPncIdentifier))

          val req = OffenderIdentifierUpdateRequest("12/1234567B", null)
          assertThatThrownBy { offenderIdentifierService.updateOffenderIdentifierForAlias(1, 1, req) }
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
          whenever(
            offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
              eq(1),
              eq(1),
            ),
          ).thenReturn(Optional.of(testCroIdentifier))

          val req = OffenderIdentifierUpdateRequest(cro, null)
          val result = offenderIdentifierService.updateOffenderIdentifierForAlias(1, 1, req)
          assertThat(result.value).isEqualTo(cro)
        }

        @Test
        fun `rejects invalid CRO`() {
          whenever(
            offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(
              eq(1),
              eq(1),
            ),
          ).thenReturn(Optional.of(testPncIdentifier))

          val req = OffenderIdentifierUpdateRequest("123456/99B", null)
          assertThatThrownBy { offenderIdentifierService.updateOffenderIdentifierForAlias(1, 1, req) }
            .hasMessageContaining("is not valid")
        }
      }
    }
  }
}
