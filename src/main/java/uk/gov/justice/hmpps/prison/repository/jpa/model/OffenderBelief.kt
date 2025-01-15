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
import org.hibernate.annotations.JoinColumnOrFormula
import org.hibernate.annotations.JoinColumnsOrFormulas
import org.hibernate.annotations.JoinFormula
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@EntityOpen
@Table(name = "OFFENDER_BELIEFS")
data class OffenderBelief(

  @Id
  @Column(name = "BELIEF_ID", nullable = false)
  @SequenceGenerator(name = "BELIEF_ID", sequenceName = "BELIEF_ID", allocationSize = 1)
  @GeneratedValue(generator = "BELIEF_ID")
  val beliefId: Long = 0,

  @ManyToOne
  @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
  val booking: OffenderBooking,

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "ROOT_OFFENDER_ID", nullable = false)
  var rootOffender: Offender,

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
  val startDate: LocalDate,

  @Column(name = "END_DATE")
  var endDate: LocalDate? = null,

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

  @Column(name = "MODIFY_DATETIME")
  var modifyDatetime: LocalDateTime? = null,

  @ManyToOne(optional = true)
  @JoinColumn(name = "MODIFY_USER_ID")
  var modifiedByUser: StaffUserAccount? = null,
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
