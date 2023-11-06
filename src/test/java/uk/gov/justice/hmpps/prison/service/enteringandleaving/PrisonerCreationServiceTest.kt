package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.PrisonerRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.NomsIdSequence

class PrisonerCreationServiceTest {
  @Mock
  private val prisonerRepository: PrisonerRepository = mock()

  private lateinit var service: PrisonerCreationService

  companion object {
    val START_SEQUENCE = NomsIdSequence(
      0,
      1,
      27,
      "A",
      "AA",
    )
  }

  @BeforeEach
  internal fun setUp() {
    service = PrisonerCreationService(
      genderRepository = mock(),
      ethnicityRepository = mock(),
      titleRepository = mock(),
      suffixRepository = mock(),
      offenderIdentifierRepository = mock(),
      offenderRepository = mock(),
      offenderTransformer = mock(),
      prisonerRepository = prisonerRepository,
      bookingIntoPrisonService = mock(),
    )
  }

  @Nested
  @DisplayName("getNextPrisonerIdentifier")
  inner class GetNextPrisonerIdentifier {
    @Test
    fun `next sequence is created`() {
      whenever(prisonerRepository.nomsIdSequence).thenReturn(START_SEQUENCE)
      val nextSeq = START_SEQUENCE.next()
      whenever(prisonerRepository.updateNomsIdSequence(eq(nextSeq), eq(START_SEQUENCE))).thenReturn(1)
      val nextPrisonerIdentifier = service.getNextPrisonerIdentifier()
      assertThat(nextPrisonerIdentifier.id).isEqualTo(START_SEQUENCE.prisonerIdentifier)
    }

    @Test
    fun `exception thrown when can never get next sequence`() {
      whenever(prisonerRepository.nomsIdSequence).thenReturn(START_SEQUENCE)
      val nextSeq = START_SEQUENCE.next()
      whenever(prisonerRepository.updateNomsIdSequence(eq(nextSeq), eq(START_SEQUENCE))).thenReturn(0)

      assertThatThrownBy { service.getNextPrisonerIdentifier() }
        .hasMessage("Prisoner Identifier cannot be generated, please try again")
    }

    @Test
    fun `will try again when initially get not get a next sequence`() {
      val nextSeq = START_SEQUENCE.next()
      whenever(prisonerRepository.nomsIdSequence).thenReturn(START_SEQUENCE, nextSeq)
      whenever(prisonerRepository.updateNomsIdSequence(eq(nextSeq), eq(START_SEQUENCE))).thenReturn(0)
      val nextNextSeq = nextSeq.next()

      whenever(prisonerRepository.updateNomsIdSequence(eq(nextNextSeq), eq(nextSeq))).thenReturn(1)
      val nextPrisonerIdentifier = service.getNextPrisonerIdentifier()

      assertThat(nextPrisonerIdentifier.id).isEqualTo(nextSeq.prisonerIdentifier)
    }
  }
}
