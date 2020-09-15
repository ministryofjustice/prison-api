package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NomsIdSequenceTest {

    @Test
    void isNextSequenceValidSimple() {
        var nomsIdSequence = NomsIdSequence.builder()
                .suffixAlphaSeq(27)
                .currentSuffix("AA")
                .currentPrefix("A")
                .prefixAlphaSeq(1)
                .nomsId(0)
                .build();
        assertThat(nomsIdSequence.getPrisonerIdentifier()).isEqualTo("A0001AA");

        assertThat(nomsIdSequence.next().getPrisonerIdentifier()).isEqualTo("A0002AA");

    }

    @Test
    void isNextSequenceValidNextSuffix() {
        final var nomsIdSequence = NomsIdSequence.builder()
                .suffixAlphaSeq(27)
                .currentSuffix("AA")
                .currentPrefix("A")
                .prefixAlphaSeq(1)
                .nomsId(9998)
                .build();
        assertThat(nomsIdSequence.getPrisonerIdentifier()).isEqualTo("A9999AA");

        assertThat(nomsIdSequence.next().getPrisonerIdentifier()).isEqualTo("A0001AC");

    }

    @Test
    void isNextSequenceValidNextPrefix() {
        final var nomsIdSequence = NomsIdSequence.builder()
                .suffixAlphaSeq(702)
                .currentSuffix("ZZ")
                .currentPrefix("A")
                .prefixAlphaSeq(1)
                .nomsId(9998)
                .build();
        assertThat(nomsIdSequence.getPrisonerIdentifier()).isEqualTo("A9999ZZ");

        assertThat(nomsIdSequence.next().getPrisonerIdentifier()).isEqualTo("C0001AA");

    }

    @Test
    void isNextSequenceValidExcludedSuffix() {
        final var nomsIdSequence = NomsIdSequence.builder()
                .suffixAlphaSeq(234)
                .currentSuffix("HZ")
                .currentPrefix("A")
                .prefixAlphaSeq(1)
                .nomsId(9998)
                .build();
        assertThat(nomsIdSequence.getPrisonerIdentifier()).isEqualTo("A9999HZ");

        assertThat(nomsIdSequence.next().getPrisonerIdentifier()).isEqualTo("A0001JA");

    }

}
