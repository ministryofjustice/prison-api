package uk.gov.justice.hmpps.prison.api.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

public class AvailablePrisonIepLevelAndCommentTest {
    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void missingComment() {
        assertThat(validator.validate(
                IepLevelAndComment
                        .builder()
                        .iepLevel("L")
                        .build()))
                .hasSize(1)
                .extracting("message").containsExactly("The IEP comment must not be blank");
    }

    @Test
    public void missingLevel() {
        assertThat(validator.validate(
                IepLevelAndComment
                        .builder()
                        .comment("Comment")
                        .build()))
                .hasSize(1)
                .extracting("message").containsExactly("The IEP level must not be blank");
    }

    @Test
    public void commentTooLong() {
        assertThat(validator.validate(
                IepLevelAndComment
                        .builder()
                        .comment(StringUtils.repeat('A', 241))
                        .iepLevel("L")
                        .build()))
                .hasSize(1)
                .extracting("message").containsExactly("The IEP level must have comment text of between 1 and 240 characters");
    }

    @Test
    public void maximumSizeComment() {
        assertThat(validator.validate(
                IepLevelAndComment
                        .builder()
                        .comment(StringUtils.repeat('A', 240))
                        .iepLevel("L")
                        .build()))
                .hasSize(0);
    }

}
