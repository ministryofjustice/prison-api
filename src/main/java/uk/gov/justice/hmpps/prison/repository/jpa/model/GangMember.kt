package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.*
import org.hibernate.Hibernate
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable

@Entity
@Table(name = "OFFENDER_GANG_AFFILIATIONS")
@EntityOpen
@IdClass(GangMemberId::class)
class GangMember(

  @Id
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "GANG_CODE", nullable = false)
  val gang: Gang,

  @Id
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
  val booking: OffenderBooking,

  @Column(name = "COMMENT_TEXT")
  val commentText: String? = null,

)

class GangMemberId(
  val gang: Gang? = null,
  val booking: OffenderBooking? = null,

) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as GangMemberId

    if (gang != other.gang) return false
    if (booking != other.booking) return false

    return true
  }

  override fun hashCode(): Int {
    var result = gang?.hashCode() ?: 0
    result = 31 * result + (booking?.hashCode() ?: 0)
    return result
  }
}
