package uk.gov.justice.hmpps.prison.service.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringWithAbbreviationsProcessorTest {

    @Test
    public void formatMultipleUniqueMatches() {
        // Assert that all abbreviations are unchanged when there are multiple unique ones in a string
        final var abbreviationsString = "AAA, ADTP, AIC, AM, ATB, BBV, BHU, BICS, CAD, CASU, CES, CGL, CIT, CSC, CSCP";
        assertThat(StringWithAbbreviationsProcessor.format(abbreviationsString)).isEqualTo(abbreviationsString);

    }

    @Test
    public void formatMultipleDuplicateMatches() {
        // Assert that all abbreviations are unchanged when there are multiple duplicate matches
        final var abbreviationsString = "Test HMP HMP";
        assertThat(StringWithAbbreviationsProcessor.format(abbreviationsString)).isEqualTo(abbreviationsString);

    }

    @Test
    public void formatWithinWord() {
        // Assert that it does not change characters if they're in the middle of words
        final var abbreviationsString = "testHMPtest";
        assertThat(StringWithAbbreviationsProcessor.format(abbreviationsString)).isEqualTo("Testhmptest");

    }

    @Test
    public void formatAbbreviationsWithFollowingNumbers() {
        // Assert that abbreviations that can have numbers after are uppercased
        final var abbreviationsString = "Test hb3, hb11 and lb123 are converted but not lb or lrc1";
        assertThat(StringWithAbbreviationsProcessor.format(abbreviationsString)).isEqualTo("Test HB3, HB11 And LB123 Are Converted But Not Lb Or Lrc1");

    }

    @Test
    public void formatNull() {
        assertThat(StringWithAbbreviationsProcessor.format(null)).isNull();
    }
}
