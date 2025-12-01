package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.api.model.IncidentTypeAnswer
import uk.gov.justice.hmpps.prison.api.model.IncidentTypeConfiguration
import uk.gov.justice.hmpps.prison.api.model.IncidentTypePrisonerRole
import uk.gov.justice.hmpps.prison.api.model.IncidentTypeQuestion
import uk.gov.justice.hmpps.prison.api.model.questionnaire.QuestionRequest
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import java.time.LocalDate
import kotlin.collections.forEach

@Entity
@EntityOpen
@Table(name = "QUESTIONNAIRES")
data class Questionnaire(
  @Id
  @Column(name = "QUESTIONNAIRE_ID")
  @SequenceGenerator(name = "QUESTIONNAIRE_ID", sequenceName = "QUESTIONNAIRE_ID", allocationSize = 1)
  @GeneratedValue(generator = "QUESTIONNAIRE_ID")
  val id: Long = 0,

  @Column(name = "DESCRIPTION")
  var description: String? = null,

  @Column
  val code: String,

  @Column(name = "ACTIVE_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  var active: Boolean = true,

  @Column(name = "QUESTIONNAIRE_CATEGORY", nullable = false)
  val category: String = "IR_TYPE",

  @Column(name = "LIST_SEQ", nullable = false)
  var listSequence: Int,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "questionnaire")
  val questions: MutableList<QuestionnaireQuestion> = mutableListOf(),

  @OneToMany(mappedBy = "id.questionnaire", cascade = [CascadeType.ALL], orphanRemoval = true)
  val offenderRoles: MutableList<QuestionnaireOffenderRole> = mutableListOf(),

  @Column(name = "EXPIRY_DATE")
  var expiryDate: LocalDate? = null,

  @Column
  var auditModuleName: String? = null,

) {

  fun findQuestionByCode(code: Long): QuestionnaireQuestion? = questions.find { it.code == code }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Questionnaire

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  @Override
  override fun toString(): String = this::class.simpleName + "(id = $id, code = $code, description = $description)"

  fun toIncidentTypeConfiguration(): IncidentTypeConfiguration = IncidentTypeConfiguration(
    questionnaireId = id,
    incidentType = code,
    incidentTypeDescription = description,
    active = active,
    expiryDate = expiryDate,
    prisonerRoles = offenderRoles.sortedBy { it.listSequence }.map { role ->
      IncidentTypePrisonerRole(
        prisonerRole = role.id.offenderRole,
        singleRole = role.singleRole,
        active = role.active,
        expiryDate = role.expiryDate,
      )
    },
    questions = questions.sortedBy { it.listSequence }
      .map { question ->
        IncidentTypeQuestion(
          questionnaireQueId = question.code,
          questionSeq = question.questionSequence,
          questionDesc = question.questionText,
          questionListSeq = question.listSequence,
          multipleAnswerFlag = question.multipleAnswers,
          questionActiveFlag = question.active,
          questionExpiryDate = question.expiryDate,
          answers = question.answers.sortedBy { it.listSequence }
            .map { answer ->
              IncidentTypeAnswer(
                questionnaireAnsId = answer.code,
                answerSeq = answer.answerSequence,
                answerDesc = answer.answerText,
                answerListSeq = answer.listSequence,
                dateRequiredFlag = answer.dateRequired,
                commentRequiredFlag = answer.commentRequired,
                nextQuestionnaireQueId = answer.nextQuestion?.code,
                answerActiveFlag = answer.active,
                answerExpiryDate = answer.expiryDate,
              )
            },
        )
      },
  )

  fun mapAnswers(questions: List<QuestionRequest>) {
    questions.forEach { q ->
      // find the question
      findQuestionByCode(q.code)?.let {
        it.answers.clear()
        it.answers.addAll(
          q.answers.mapIndexed { index, answer ->
            val nextQuestion = answer.nextQuestionCode?.let {
              findQuestionByCode(answer.nextQuestionCode)
                ?: throw EntityNotFoundException("Next question code : ${answer.nextQuestionCode}")
            }
            answer.toEntity(nextQuestion, index + 1)
          },
        )
      }
    }
  }
}
