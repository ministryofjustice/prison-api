package uk.gov.justice.hmpps.prison.api.model.questionnaire

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.repository.jpa.model.QuestionnaireAnswer
import uk.gov.justice.hmpps.prison.repository.jpa.model.QuestionnaireQuestion
import java.time.LocalDate

data class AnswerRequest(
  @Schema(description = "Code for the answer", example = "20001", required = true)
  val code: Long,

  @Schema(description = "Answer text", example = "YES", required = true)
  val response: String,

  @Schema(description = "Active", example = "true", defaultValue = "true")
  val active: Boolean = true,

  @Schema(description = "Date required", defaultValue = "false")
  val dateRequired: Boolean = false,

  @Schema(description = "Comment required", defaultValue = "false")
  val commentRequired: Boolean = false,

  @Schema(description = "Next question code (optional)", required = false)
  val nextQuestionCode: Long? = null,
) {

  fun toEntity(
    nextQuestion: QuestionnaireQuestion?,
    index: Int,
  ): QuestionnaireAnswer = QuestionnaireAnswer(
    code = code,
    answerText = response,
    nextQuestion = nextQuestion,
    active = active,
    answerSequence = index,
    listSequence = index,
    commentRequired = commentRequired,
    dateRequired = dateRequired,
    expiryDate = if (!active) {
      LocalDate.now()
    } else {
      null
    },
  )
}
