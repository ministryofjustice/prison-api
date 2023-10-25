package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.*
import org.hibernate.Hibernate
import org.hibernate.annotations.JoinColumnOrFormula
import org.hibernate.annotations.JoinColumnsOrFormulas
import org.hibernate.annotations.JoinFormula
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable

@Entity
@Table(name = "OFFENDER_GANG_INVESTS")
@EntityOpen
@IdClass(GangMemberInvestigationId::class)
class GangMemberInvestigation(

  @Id
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "GANG_CODE", nullable = false)
  val gang: Gang,

  @Id
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
  val booking: OffenderBooking,

  @Id
  @Column(name = "INVESTIGATE_SEQ", nullable = false)
  val sequence: Int = 1,

  @ManyToOne(optional = false)
  @JoinColumnsOrFormulas(
    value = [
      JoinColumnOrFormula(
        formula = JoinFormula(
          value = "'" + MembershipStatus.DOMAIN + "'",
          referencedColumnName = "domain",
        ),
      ),
      JoinColumnOrFormula(column = JoinColumn(name = "GANG_MBR_STS", referencedColumnName = "code")),
    ],
  )
  val membershipStatus: MembershipStatus,

  @Column(name = "COMMENT_TEXT")
  val commentText: String? = null,

)

class GangMemberInvestigationId(
  val gang: Gang? = null,
  val booking: OffenderBooking? = null,
  val sequence: Int = 1,

) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as GangMemberInvestigationId

    if (gang != other.gang) return false
    if (booking != other.booking) return false
    if (sequence != other.sequence) return false

    return true
  }

  override fun hashCode(): Int {
    var result = gang?.hashCode() ?: 0
    result = 31 * result + (booking?.hashCode() ?: 0)
    result = 31 * result + sequence
    return result
  }
}
