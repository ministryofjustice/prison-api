package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable
import java.time.LocalDate

@Embeddable
data class QuestionnaireOffenderRoleId(

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "QUESTIONNAIRE_ID", nullable = false)
  val questionnaire: Questionnaire,

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
  val listSequence: Int? = 99,

  @Column(name = "EXPIRY_DATE")
  val expiryDate: LocalDate? = null,

  @Column
  var auditModuleName: String? = null,
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as QuestionnaireOffenderRole

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  @Override
  override fun toString(): String = this::class.simpleName + "(id = ${id.questionnaire},  ${id.offenderRole} )"
}
