package net.syscon.elite.service.support;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class StringWithAbbreviationsProcessorTest {
    @Test
    public void format() {
        // Assert that all abbreviations are unchanged, even when there a multiple ones in a string
        final var abbreviationsString = String.join(", ", StringWithAbbreviationsProcessor.ABBREVIATIONS);
        assertThat(StringWithAbbreviationsProcessor.format(abbreviationsString)).isEqualTo(abbreviationsString);

    }
}
