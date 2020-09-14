package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NomsIdSequence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrisonerCreationServiceTest {
    @Mock
    private OffenderRepository offenderRepository;

    private PrisonerCreationService service;
    public static final NomsIdSequence START_SEQUENCE = NomsIdSequence.builder()
            .suffixAlphaSeq(27)
            .currentSuffix("AA")
            .currentPrefix("A")
            .prefixAlphaSeq(1)
            .nomsId(0)
            .build();

    @BeforeEach
    void setUp() {
        service = new PrisonerCreationService(offenderRepository);
    }

    @Test
    void testNextSeqenceCreated() {
        when(offenderRepository.getNomsIdSequence()).thenReturn(START_SEQUENCE);
        final var nextSeq = START_SEQUENCE.next();
        when(offenderRepository.updateNomsIdSequence(Mockito.eq(nextSeq), Mockito.eq(START_SEQUENCE))).thenReturn(1);

        final var nextPrisonerIdentifier = service.getNextPrisonerIdentifier();

        assertThat(nextPrisonerIdentifier.getId()).isEqualTo(START_SEQUENCE.getPrisonerIdentifier());
    }

    @Test
    void testCannotUpdate() {
        when(offenderRepository.getNomsIdSequence()).thenReturn(START_SEQUENCE);
        final var nextSeq = START_SEQUENCE.next();
        when(offenderRepository.updateNomsIdSequence(Mockito.eq(nextSeq), Mockito.eq(START_SEQUENCE))).thenReturn(0);

        assertThatThrownBy(() -> service.getNextPrisonerIdentifier())
                .hasMessage("Prisoner Identifier cannot be generated, please try again");
    }

    @Test
    void testCanUpdateAfterRetry() {
        final var nextSeq = START_SEQUENCE.next();
        when(offenderRepository.getNomsIdSequence()).thenReturn(START_SEQUENCE, nextSeq);
        when(offenderRepository.updateNomsIdSequence(Mockito.eq(nextSeq), Mockito.eq(START_SEQUENCE))).thenReturn(0);
        final var nextNextSeq = nextSeq.next();
        when(offenderRepository.updateNomsIdSequence(Mockito.eq(nextNextSeq), Mockito.eq(nextSeq))).thenReturn(1);

        final var nextPrisonerIdentifier = service.getNextPrisonerIdentifier();

        assertThat(nextPrisonerIdentifier.getId()).isEqualTo(nextSeq.getPrisonerIdentifier());
    }
}
