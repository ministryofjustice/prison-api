package uk.gov.justice.hmpps.prison.service.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringWithAbbreviationsProcessorTest {

    @Test
    public void formatMultipleUniqueMatches() {
        // Assert that all abbreviations are unchanged when there a multiple unique ones in a string
        final var abbreviationsString = String.join(", ", StringWithAbbreviationsProcessor.ABBREVIATIONS);
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
    public void formatNull() {
        assertThat(StringWithAbbreviationsProcessor.format(null)).isEqualTo(null);

    }
}
