package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.JoinColumnOrFormula
import org.hibernate.annotations.JoinColumnsOrFormulas
import org.hibernate.annotations.JoinFormula
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.time.LocalDateTime

@Entity
@EntityOpen
@Table(name = "OFFENDER_BELIEFS")
data class OffenderBelief(
  @Id
  val beliefId: Long,

  @ManyToOne
  @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
  val booking: OffenderBooking,

  @ManyToOne
  @JoinColumnsOrFormulas(
    value = [
      JoinColumnOrFormula(
        formula = JoinFormula(
          value = "'RELF'",
          referencedColumnName = "PROFILE_TYPE",
        ),
      ),
      JoinColumnOrFormula(column = JoinColumn(name = "BELIEF_CODE", referencedColumnName = "PROFILE_CODE")),
    ],
  )
  val beliefCode: ProfileCode,

  @Column(name = "EFFECTIVE_DATE")
  val startDate: LocalDateTime,

  val endDate: LocalDateTime? = null,

  @Convert(converter = YesNoConverter::class)
  val changeReason: Boolean? = null,

  val comments: String? = null,

  @Convert(converter = YesNoConverter::class)
  @Column(name = "VERIFIED_FLAG")
  val verified: Boolean? = null,

  @Column(nullable = false)
  val createDatetime: LocalDateTime,

  @ManyToOne
  @JoinColumn(name = "CREATE_USER_ID", nullable = false)
  val createdByUser: StaffUserAccount,

  val modifyDatetime: LocalDateTime? = null,

  @ManyToOne(optional = true)
  @JoinColumn(name = "MODIFY_USER_ID")
  val modifiedByUser: StaffUserAccount? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as OffenderBelief
    return beliefId == other.beliefId
  }

  override fun hashCode(): Int {
    return this.javaClass.hashCode()
  }
}
