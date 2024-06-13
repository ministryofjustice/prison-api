package uk.gov.justice.hmpps.prison.service;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication.NewAdjudicationBuilder;
import uk.gov.justice.hmpps.prison.api.model.UpdateAdjudication;
import uk.gov.justice.hmpps.prison.api.model.UpdateAdjudication.UpdateAdjudicationBuilder;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
public class AdjudicationsServiceIntTest {
    @Autowired
    private AdjudicationsService service;

    @Nested
    public class CreateAdjudication {

        @Test
        public void maximumTextSizeExceeded() {
            final var adjudicationWithLargeStatementSize = defaultAdjudicationBuilder()
                .statement(generateMessageWith4001Chars())
                .build();

            assertThatThrownBy(() -> service.createAdjudication( adjudicationWithLargeStatementSize))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Length exceeds the maximum size allowed");
        }

        @Test
        public void maximumTextSizeExceededDueToUtf8() {
            final var adjudicationWithLargeStatementSize = defaultAdjudicationBuilder()
                .statement(generateMessageWith4000CharsAndUtf8Chars())
                .build();

            assertThatThrownBy(() -> service.createAdjudication(adjudicationWithLargeStatementSize))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Length exceeds the maximum size allowed");
        }


        private NewAdjudicationBuilder defaultAdjudicationBuilder() {
            return NewAdjudication.builder()
                .offenderNo("A1234AD")
                .incidentTime(LocalDateTime.now())
                .incidentLocationId(2L)
                .statement("A statement");
        }
    }

    @Nested
    public class ModifyAdjudication {

        private final Long EXAMPLE_ADJUDICATION_NUMBER = 123L;

        @Test
        public void maximumTextSizeExceeded() {
            final var adjudicationWithLargeStatementSize = defaultAdjudicationBuilder()
                .statement(generateMessageWith4001Chars())
                .build();

            assertThatThrownBy(() -> service.updateAdjudication(EXAMPLE_ADJUDICATION_NUMBER, adjudicationWithLargeStatementSize))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Length exceeds the maximum size allowed");
        }

        @Test
        public void maximumTextSizeExceededDueToUtf8() {
            final var adjudicationWithLargeStatementSize = defaultAdjudicationBuilder()
                .statement(generateMessageWith4000CharsAndUtf8Chars())
                .build();

            assertThatThrownBy(() -> service.updateAdjudication(EXAMPLE_ADJUDICATION_NUMBER, adjudicationWithLargeStatementSize))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Length exceeds the maximum size allowed");
        }

        private UpdateAdjudicationBuilder defaultAdjudicationBuilder() {
            return UpdateAdjudication.builder()
                .incidentTime(LocalDateTime.now())
                .incidentLocationId(2L)
                .statement("A statement");
        }
    }

    private String generateMessageWith4001Chars() {
        final String stringWith10Chars = "ABCDE12345";
        final StringBuilder textWith4001Chars = new StringBuilder(4010);
        IntStream.rangeClosed(1,400).forEach((i) -> textWith4001Chars.append(stringWith10Chars));
        textWith4001Chars.append("x"); // Make 4001
        return textWith4001Chars.toString();
    }

    private String generateMessageWith4000CharsAndUtf8Chars() {
        final String stringWith10Chars = "ABCDE12345";
        final StringBuilder textExceeding4000CharsDueToUtf8 = new StringBuilder(4010);
        IntStream.rangeClosed(1,399).forEach((i) -> textExceeding4000CharsDueToUtf8.append(stringWith10Chars));
        textExceeding4000CharsDueToUtf8.append("ABCDE123⌘⌥"); // Add Unicode chars
        return textExceeding4000CharsDueToUtf8.toString();
    }
}
