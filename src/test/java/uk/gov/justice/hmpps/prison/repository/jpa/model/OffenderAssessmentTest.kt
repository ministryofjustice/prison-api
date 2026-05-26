package uk.gov.justice.hmpps.prison.repository.jpa.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class OffenderAssessmentTest {
  @ParameterizedTest
  @MethodSource("classificationsWithExpectedResults")
  fun classificationSummaryCalculation(
    calculatedClassificationCode: String?,
    overrideClassificationCode: String?,
    reviewedClassificationCode: String?,
    reviewCommitteeComment: String?,
    overrideReason: String?,
    expectedFinalOutcome: String?,
    expectedOriginalOutcome: String?,
    expectedApprovalReason: String?,
  ) {
    val offenderAssessment = OffenderAssessment.builder()
      .calculatedClassification(generateClassification(calculatedClassificationCode))
      .overridingClassification(generateClassification(overrideClassificationCode))
      .reviewedClassification(generateClassification(reviewedClassificationCode))
      .reviewCommitteeComment(reviewCommitteeComment)
      .overrideReason(
        if (overrideReason == null) {
          null
        } else {
          AssessmentOverrideReason(
            "OVERRIDE_DUMMY_VALUE",
            overrideReason,
          )
        },
      )
      .build()
    val classificationSummary = offenderAssessment.getClassificationSummary()

    assertClassificationCodeEquals(classificationSummary.finalClassification, expectedFinalOutcome)
    assertClassificationCodeEquals(classificationSummary.originalClassification, expectedOriginalOutcome)
    assertThat(classificationSummary.classificationApprovalReason).isEqualTo(expectedApprovalReason)
  }

  @ParameterizedTest
  @MethodSource("classificationPriorityWithExpectedResults")
  fun classificationSummaryPriority(
    calculatedClassificationCode: String?,
    overrideClassificationCode: String?,
    expectedClassificationCode: String?,
  ) {
    val offenderAssessment = OffenderAssessment.builder()
      .calculatedClassification(generateClassification(calculatedClassificationCode))
      .overridingClassification(generateClassification(overrideClassificationCode))
      .build()
    val classificationSummary = offenderAssessment.getClassificationSummary()

    assertThat(classificationSummary.finalClassification.code)
      .isEqualTo(expectedClassificationCode)
  }

  @ParameterizedTest
  @MethodSource("classificationsWithExpectedIsSetResults")
  fun classificationSummaryIsSet(
    calculatedClassificationCode: String?,
    overrideClassificationCode: String?,
    reviewedClassificationCode: String?,
    expectedIsSetResult: Boolean,
  ) {
    val offenderAssessment = OffenderAssessment.builder()
      .calculatedClassification(generateClassification(calculatedClassificationCode))
      .overridingClassification(generateClassification(overrideClassificationCode))
      .reviewedClassification(generateClassification(reviewedClassificationCode))
      .build()
    val classificationSummary = offenderAssessment.getClassificationSummary()

    assertThat(classificationSummary.isSet).isEqualTo(expectedIsSetResult)
  }

  private fun assertClassificationCodeEquals(actualClassification: AssessmentClassification?, expectedCode: String?) {
    if (expectedCode == null) {
      assertThat(actualClassification).isNull()
    } else {
      assertThat(actualClassification?.code).isEqualTo(expectedCode)
    }
  }

  private fun generateClassification(classificationCode: String?): AssessmentClassification? {
    if (classificationCode == null) {
      return null
    }
    val generatedClassification = AssessmentClassification()
    generatedClassification.code = classificationCode
    generatedClassification.description = "Description of $classificationCode"
    return generatedClassification
  }

  companion object {
    @JvmStatic
    private fun classificationsWithExpectedResults(): Stream<Arguments> = Stream.of(
      Arguments.arguments(
        "STANDARD",
        "HI",
        "HI",
        "Approval Comment",
        "Override Comment",
        "HI",
        "STANDARD",
        "Override Comment",
      ),
      Arguments.arguments("STANDARD", "HI", "HI", "Approval Comment", null, "HI", "STANDARD", "Approval Comment"),
      Arguments.arguments("STANDARD", "HI", "HI", null, null, "HI", "STANDARD", null),
      Arguments.arguments("HI", "STANDARD", null, null, "Override Comment", "HI", null, null),
      Arguments.arguments(
        "STANDARD",
        "HI",
        "STANDARD",
        "Approval Comment",
        "Override Comment",
        "STANDARD",
        null,
        "Approval Comment",
      ),
      Arguments.arguments("STANDARD", null, "STANDARD", null, "Override Comment", "STANDARD", null, null),
      Arguments.arguments(
        null,
        "HI",
        "STANDARD",
        "Approval Comment",
        "Override Comment",
        "STANDARD",
        null,
        "Override Comment",
      ),
      Arguments.arguments(
        null,
        null,
        "STANDARD",
        "Approval Comment",
        "Override Comment",
        "STANDARD",
        null,
        "Override Comment",
      ),
      Arguments.arguments(
        "PEND",
        "STANDARD",
        "HI",
        "Approval Comment",
        "Override Comment",
        "HI",
        null,
        "Override Comment",
      ),
      Arguments.arguments("PEND", null, "HI", "Approval Comment", null, "HI", null, "Approval Comment"),
      Arguments.arguments("PEND", null, null, null, null, null, null, null),
      Arguments.arguments(null, null, null, null, null, null, null, null),
    )

    @JvmStatic
    private fun classificationPriorityWithExpectedResults(): Stream<Arguments> = Stream.of(
      Arguments.arguments("HI", "HI", "HI"),
      Arguments.arguments("HI", "STANDARD", "HI"),
      Arguments.arguments("HI", "MED", "HI"),
      Arguments.arguments("HI", "LOW", "HI"),
      Arguments.arguments("STANDARD", "HI", "HI"),
      Arguments.arguments("STANDARD", "STANDARD", "STANDARD"),
      Arguments.arguments("STANDARD", "MED", "STANDARD"),
      Arguments.arguments("STANDARD", "LOW", "STANDARD"),
      Arguments.arguments("MED", "HI", "HI"),
      Arguments.arguments("MED", "STANDARD", "STANDARD"),
      Arguments.arguments("MED", "MED", "MED"),
      Arguments.arguments("MED", "LOW", "MED"),
      Arguments.arguments("LOW", "HI", "HI"),
      Arguments.arguments("LOW", "STANDARD", "STANDARD"),
      Arguments.arguments("LOW", "MED", "MED"),
      Arguments.arguments("LOW", "LOW", "LOW"),
    )

    @JvmStatic
    private fun classificationsWithExpectedIsSetResults(): Stream<Arguments> = Stream.of(
      Arguments.arguments("STANDARD", "HI", "HI", true),
      Arguments.arguments("STANDARD", "HI", null, true),
      Arguments.arguments("STANDARD", null, "STANDARD", true),
      Arguments.arguments("STANDARD", null, null, true),
      Arguments.arguments(null, "HI", "STANDARD", true),
      Arguments.arguments(null, null, "STANDARD", true),
      Arguments.arguments("PEND", null, null, false),
      Arguments.arguments(null, null, null, false),
    )
  }
}
