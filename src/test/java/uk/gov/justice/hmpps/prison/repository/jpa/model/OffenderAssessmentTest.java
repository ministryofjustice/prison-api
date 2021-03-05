package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class OffenderAssessmentTest {

    @ParameterizedTest
    @MethodSource("classificationsWithExpectedResults")
    void classificationSummaryCalculation(String calculatedClassification, String overrideClassification,
                                          String reviewedClassification, String expectedFinalOutcome, String expectedOriginalOutcome)
    {
        final var offenderAssessment = OffenderAssessment.builder()
            .calculatedClassification(calculatedClassification)
            .overridingClassification(overrideClassification)
            .reviewedClassification(reviewedClassification)
            .build();
        final var classificationSummary = offenderAssessment.getClassificationSummary();

        assertThat(classificationSummary.getFinalClassification()).isEqualTo(expectedFinalOutcome);
        assertThat(classificationSummary.getOriginalClassification()).isEqualTo(expectedOriginalOutcome);
    }

    private static Stream<Arguments> classificationsWithExpectedResults() {
        return Stream.of(
            arguments("STANDARD", "HI", "HI", "HI", "STANDARD"),
            arguments("STANDARD", "HI", "STANDARD", "STANDARD", null),
            arguments("STANDARD", null, "STANDARD", "STANDARD", null),
            arguments(null, "HI", "STANDARD", "STANDARD", null),
            arguments(null, null, "STANDARD", "STANDARD", null),
            arguments("PEND", "STANDARD", "HI", "HI", null),
            arguments("PEND", null, "HI", "HI", null),
            arguments("PEND", null, null, null, null),
            arguments(null, null, null, null, null)
        );
    }
}
