package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.Generated
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "QUESTIONNAIRE_ANSWERS")
@EntityOpen
data class QuestionnaireAnswer(
  @Id
  @Column(name = "QUESTIONNAIRE_ANS_ID")
  @SequenceGenerator(name = "QUESTIONNAIRE_ANS_ID", sequenceName = "QUESTIONNAIRE_ANS_ID", allocationSize = 1)
  @GeneratedValue(generator = "QUESTIONNAIRE_ANS_ID")
  val id: Long = 0,

  @Column(name = "DESCRIPTION")
  val answerText: String,

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "NEXT_QUESTIONNAIRE_QUE_ID")
  var nextQuestion: QuestionnaireQuestion? = null,

  @Column(name = "ACTIVE_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val active: Boolean = true,

  @Column(name = "ANS_SEQ")
  val answerSequence: Int,

  @Column(name = "LIST_SEQ")
  val listSequence: Int,

  @Column(name = "COMMENT_REQUIRED_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val commentRequired: Boolean = false,

  @Column(name = "DATE_REQUIRED_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val dateRequired: Boolean = false,

  @Column
  var auditModuleName: String? = null,
) {
  @Column(name = "CREATE_USER_ID", insertable = false, updatable = false)
  @Generated
  lateinit var createUsername: String

  @Column(name = "CREATE_DATETIME", insertable = false, updatable = false)
  @Generated
  lateinit var createDatetime: LocalDateTime

  @Column(name = "EXPIRY_DATE")
  @Generated
  lateinit var expiryDate: LocalDate

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as QuestionnaireAnswer

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(id = $id ), answer = $answerText)"
  }
}
