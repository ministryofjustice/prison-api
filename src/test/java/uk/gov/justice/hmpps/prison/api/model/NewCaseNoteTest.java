package uk.gov.justice.hmpps.prison.api.model;


import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class NewCaseNoteTest {

    private static final String CHAR_TEXT_OVER_4000_BYTES;

    static {
        String stringWith10Chars = "ABCDE12345";
        StringBuilder string = new StringBuilder(4010);
        IntStream.rangeClosed(1,399).forEach((i) -> string.append(stringWith10Chars));
        string.append("ABCDE123⌘⌥"); // Add Unicode chars
        CHAR_TEXT_OVER_4000_BYTES = string.toString();
    }

    @Test
    public void textCannotExceed4000UTF8Bytes() {
        var caseNote = new NewCaseNote();
        caseNote.setText(CHAR_TEXT_OVER_4000_BYTES);
        assertThat(caseNote.getText().getBytes(StandardCharsets.UTF_8).length).isEqualTo(4000);
    }

}
