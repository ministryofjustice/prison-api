package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.Generated
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Embeddable
data class QuestionnaireOffenderRoleId(

  @Column(name = "QUESTIONNAIRE_ID", nullable = false)
  val questionnaireId: Long,

  // Offender Role = IR_OFF_PART in Reference_Codes table
  @Column(name = "PARTICIPATION_ROLE", nullable = false)
  val offenderRole: String,
) : Serializable

@Entity
@Table(name = "QUESTIONNAIRE_ROLES")
@EntityOpen
data class QuestionnaireOffenderRole(

  @EmbeddedId
  val id: QuestionnaireOffenderRoleId,

  @Column(name = "SINGLE_ROLE_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val singleRole: Boolean = false,

  @Column(name = "ACTIVE_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val active: Boolean = true,

  @Column(name = "LIST_SEQ")
  val listSequence: Int,

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
    other as QuestionnaireOffenderRole

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(id = ${id.questionnaireId},  ${id.offenderRole} )"
  }
}
