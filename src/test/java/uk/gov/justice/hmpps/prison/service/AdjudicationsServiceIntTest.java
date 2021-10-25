package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication.NewAdjudicationBuilder;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
public class AdjudicationsServiceIntTest {
    @Autowired
    private AdjudicationsService service;

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"SYSTEM_USER"})
    public void createAdjudication_maximumTextSizeExceeded() {
        final var adjudicationWithLargeStatementSize = defaultAdjudicationBuilder()
            .statement(generateMessageWith4001Chars())
            .build();

        assertThatThrownBy(() -> service.createAdjudication(adjudicationWithLargeStatementSize.getBookingId(), adjudicationWithLargeStatementSize))
            .isInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("Length exceeds the maximum size allowed");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"SYSTEM_USER"})
    public void createAdjudication_maximumTextSizeExceededDueToUtf8() {
        final var adjudicationWithLargeStatementSize = defaultAdjudicationBuilder()
            .statement(generateMessageWith4000CharsAndUtf8Chars())
            .build();

        assertThatThrownBy(() -> service.createAdjudication(adjudicationWithLargeStatementSize.getBookingId(), adjudicationWithLargeStatementSize))
            .isInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("Length exceeds the maximum size allowed");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"SYSTEM_USER"})
    public void createAdjudication_invalidBooking() {
        final var adjudicationWithLargeStatementSize = defaultAdjudicationBuilder()
            .statement(generateMessageWith4000CharsAndUtf8Chars())
            .build();

        assertThatThrownBy(() -> service.createAdjudication(500L, adjudicationWithLargeStatementSize))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Offender booking with id 500 not found.");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"SOME_ROLE"})
    public void createAdjudication_wrongRole() {
        assertThatThrownBy(() -> service.createAdjudication(1L, defaultAdjudicationBuilder().build()))
                .hasMessage("Access is denied");
    }

    private NewAdjudicationBuilder defaultAdjudicationBuilder() {
        return NewAdjudication.builder()
            .bookingId(-4L)
            .incidentTime(LocalDateTime.now())
            .incidentLocationId(2L)
            .statement("A statement");
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
