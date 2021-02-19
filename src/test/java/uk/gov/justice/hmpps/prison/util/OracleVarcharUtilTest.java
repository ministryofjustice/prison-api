package uk.gov.justice.hmpps.prison.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class OracleVarcharUtilTest {

    private static final String CHAR_TEXT_3990_BYTES;

    static {
        String stringWith10Chars = "ABCDE12345";
        StringBuilder string = new StringBuilder(4010);
        IntStream.rangeClosed(1,399).forEach((i) -> string.append(stringWith10Chars));
        CHAR_TEXT_3990_BYTES = string.toString();
    }

    @Test
    public void When_0Bytes_Then_ReturnsUnchanged() {
        final String testString = "";
        String treatedString = OracleVarcharUtil.enforceMaximumTextSize(testString);
        assertThat(treatedString).isEqualTo(testString);
    }

    @Test
    public void When_4000BytesIncludingUTF_Then_ReturnsUnchangedWith4000Bytes() {
        final String testString = CHAR_TEXT_3990_BYTES + "ABCDE12⌥";
        String treatedString = OracleVarcharUtil.enforceMaximumTextSize(testString);
        assertThat(treatedString).isEqualTo(testString);
        assertThat(treatedString.getBytes(StandardCharsets.UTF_8).length).isEqualTo(4000);
    }

    @Test
    public void When_Over4000BytesIncludingUTF_Then_ReturnsWithASCIIInPlaceOfUTF() {
        final String testString = CHAR_TEXT_3990_BYTES + "ABCDE12⌥⌘";
        final String expectedTreatedString = CHAR_TEXT_3990_BYTES + "ABCDE12??";
        String treatedString = OracleVarcharUtil.enforceMaximumTextSize(testString);
        assertThat(treatedString).isEqualTo(expectedTreatedString);
        assertThat(treatedString.getBytes(StandardCharsets.UTF_8).length).isEqualTo(testString.length());
    }

}