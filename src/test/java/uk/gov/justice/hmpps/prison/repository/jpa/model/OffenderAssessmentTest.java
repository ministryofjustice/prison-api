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
    void classificationSummaryCalculation(String calculatedClassificationCode, String overrideClassificationCode, String reviewedClassificationCode,
                                          String reviewCommitteeComment, String overrideReason, String expectedFinalOutcome,
                                          String expectedOriginalOutcome, String expectedApprovalReason)
    {
        final var offenderAssessment = OffenderAssessment.builder()
            .calculatedClassification(generateClassification(calculatedClassificationCode))
            .overridingClassification(generateClassification(overrideClassificationCode))
            .reviewedClassification(generateClassification(reviewedClassificationCode))
            .reviewCommitteeComment(reviewCommitteeComment)
            .overrideReason(overrideReason == null ? null: new AssessmentOverrideReason("OVERRIDE_DUMMY_VALUE", overrideReason))
            .build();
        final var classificationSummary = offenderAssessment.getClassificationSummary();

        assertClassificationCodeEquals(classificationSummary.getFinalClassification(), expectedFinalOutcome);
        assertClassificationCodeEquals(classificationSummary.getOriginalClassification(), expectedOriginalOutcome);
        assertThat(classificationSummary.getClassificationApprovalReason()).isEqualTo(expectedApprovalReason);
    }

    private void assertClassificationCodeEquals(AssessmentClassification actualClassification, String expectedCode) {
        if (expectedCode == null) {
            assertThat(actualClassification).isNull();
        } else {
            assertThat(actualClassification.getCode()).isEqualTo(expectedCode);
        }
    }

    private AssessmentClassification generateClassification(String classificationCode) {
        if (classificationCode == null) {
            return null;
        }
        final var generatedClassification = new AssessmentClassification();
        generatedClassification.setCode(classificationCode);
        generatedClassification.setDescription("Description of " + classificationCode);
        return generatedClassification;
    }

    private static Stream<Arguments> classificationsWithExpectedResults() {
        return Stream.of(
            arguments("STANDARD", "HI", "HI", "Approval Comment", "Override Comment", "HI", "STANDARD", "Override Comment"),
            arguments("STANDARD", "HI", "HI", "Approval Comment", null, "HI", "STANDARD", "Approval Comment"),
            arguments("STANDARD", "HI", "HI", null, null, "HI", "STANDARD", null),
            arguments("STANDARD", "HI", "STANDARD", "Approval Comment", "Override Comment", "STANDARD", null, "Approval Comment"),
            arguments("STANDARD", null, "STANDARD", null, "Override Comment", "STANDARD", null, null),
            arguments(null, "HI", "STANDARD", "Approval Comment", "Override Comment", "STANDARD", null, "Override Comment"),
            arguments(null, null, "STANDARD", "Approval Comment", "Override Comment","STANDARD", null, "Override Comment"),
            arguments("PEND", "STANDARD", "HI", "Approval Comment", "Override Comment", "HI", null, "Override Comment"),
            arguments("PEND", null, "HI", "Approval Comment", null, "HI", null, "Approval Comment"),
            arguments("PEND", null, null, null, null, null, null, null),
            arguments(null, null, null, null, null, null, null, null)
        );
    }
}
