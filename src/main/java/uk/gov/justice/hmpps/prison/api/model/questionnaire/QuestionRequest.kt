package uk.gov.justice.hmpps.prison.api.model.questionnaire

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.repository.jpa.model.Questionnaire
import uk.gov.justice.hmpps.prison.repository.jpa.model.QuestionnaireQuestion
import java.time.LocalDate

data class QuestionRequest(

  @Schema(description = "Code for the question", example = "10001", required = true)
  val code: Long,

  @Schema(description = "Question text", example = "Was a drone seen?", required = true)
  val question: String,

  @Schema(description = "Question active flag", example = "true", defaultValue = "true")
  val active: Boolean = true,

  @Schema(description = "Multiple answers allowed", example = "false", defaultValue = "false")
  val multipleAnswers: Boolean = false,

  @Schema(description = "Answers for this question")
  val answers: List<AnswerRequest> = emptyList(),
) {
  fun toEntity(questionnaire: Questionnaire, index: Int) = QuestionnaireQuestion(
    code = code,
    questionnaire = questionnaire,
    active = active,
    questionSequence = index,
    questionText = question,
    multipleAnswers = multipleAnswers,
    listSequence = index,
    expiryDate = if (!active) {
      LocalDate.now()
    } else {
      null
    },
  )
}
