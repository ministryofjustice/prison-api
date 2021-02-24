package uk.gov.justice.hmpps.prison.validation;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSizeValidator;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class MaximumTextSizeValidatorTest {

    private static final String CHAR_TEXT_3990_BYTES;
    private final MaximumTextSizeValidator validator = new MaximumTextSizeValidator();

    static {
        String stringWith10Chars = "ABCDE12345";
        StringBuilder string = new StringBuilder(4010);
        IntStream.rangeClosed(1,399).forEach((i) -> string.append(stringWith10Chars));
        CHAR_TEXT_3990_BYTES = string.toString();
    }

    @Test
    public void testValidOnNullValue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    public void testValidWhen4000AnsiCharacters() {
        final String testString = CHAR_TEXT_3990_BYTES + "ABCDE12345";
        assertThat(validator.isValid(testString, null)).isTrue();
    }

    @Test
    public void testInvalidWhen4001AnsiCharacters() {
        final String testString = CHAR_TEXT_3990_BYTES + "ABCDE123450";
        assertThat(validator.isValid(testString, null)).isFalse();
    }

    @Test
    public void testInvalidWhenMoreThan4000BytesDueToUtf8() {
        final String testString = CHAR_TEXT_3990_BYTES + "ABCDE12⌥⌘";
        assertThat(validator.isValid(testString, null)).isFalse();
    }
}
