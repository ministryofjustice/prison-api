package uk.gov.justice.hmpps.prison.service.validation;

import com.google.common.base.Utf8;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class MaximumTextSizeValidator implements ConstraintValidator<MaximumTextSize, String> {
    private static final int CHECK_THRESHOLD = 3900;
    private static final int MAX_VARCHAR_BYTES = 4000;

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.length() < CHECK_THRESHOLD) {
            return true;
        }

        return Utf8.encodedLength(value) <= MAX_VARCHAR_BYTES;
    }

    public int getMaximumAnsiEncodingSize() {
        return MAX_VARCHAR_BYTES;
    }
}
