package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.time.LocalDate

@Entity
@Table(name = "QUESTIONNAIRE_QUESTIONS")
@EntityOpen
data class QuestionnaireQuestion(
  @Id
  @Column(name = "QUESTIONNAIRE_QUE_ID")
  @SequenceGenerator(name = "QUESTIONNAIRE_QUE_ID", sequenceName = "QUESTIONNAIRE_QUE_ID", allocationSize = 1)
  @GeneratedValue(generator = "QUESTIONNAIRE_QUE_ID")
  val id: Long = 0,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "QUESTIONNAIRE_QUE_ID", nullable = false)
  val answers: MutableList<QuestionnaireAnswer> = mutableListOf(),

  @Column(name = "ACTIVE_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val active: Boolean = true,

  @Column(name = "QUE_SEQ")
  val questionSequence: Int,

  @Column(name = "DESCRIPTION")
  val questionText: String,

  @Column(name = "MULTIPLE_ANSWER_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val multipleAnswers: Boolean = true,

  @Column(name = "LIST_SEQ")
  val listSequence: Int,

  @Column(name = "EXPIRY_DATE")
  val expiryDate: LocalDate,

  @Column
  var auditModuleName: String? = null,
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as QuestionnaireQuestion

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(id = $id ), question = $questionText)"
  }
}
