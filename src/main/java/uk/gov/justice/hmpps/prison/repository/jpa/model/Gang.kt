package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.time.LocalDate

@Entity
@Table(name = "GANGS")
@EntityOpen
class Gang(

  @Id
  @Column(name = "GANG_CODE", nullable = false)
  val code: String,

  @Column(name = "GANG_NAME")
  val name: String,

  @Column(name = "LIST_SEQ")
  val sequence: Int = 99,

  @Column(name = "ACTIVE_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  var active: Boolean = true,

  @Column(name = "EXPIRY_DATE")
  var expiryDate: LocalDate? = null,

  @JoinColumn(name = "PARENT_GANG_CODE")
  @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  val parent: Gang? = null,

  @OneToMany(mappedBy = "gang", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val members: MutableList<GangMember> = mutableListOf(),

  @OneToMany(mappedBy = "primaryGang", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val nonAssociationsPrimary: MutableList<GangNonAssociation> = mutableListOf(),

  @OneToMany(mappedBy = "secondaryGang", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val nonAssociationsSecondary: MutableList<GangNonAssociation> = mutableListOf(),

) {

  fun addMember(booking: OffenderBooking, commentText: String? = null) {
    members.add(GangMember(this, booking, commentText))
  }

  fun addNonAssociation(gang: Gang, reason: NonAssociationReason) {
    nonAssociationsPrimary.add(GangNonAssociation(this, gang, reason))
  }

  fun getNonAssociations(): List<Pair<Gang, NonAssociationReason>> =
    nonAssociationsPrimary.plus(nonAssociationsSecondary).map { naGang ->
      Pair(naGang.primaryGang.takeIf { it != this } ?: naGang.secondaryGang, naGang.nonAssociationReason)
    }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as Gang

    return code == other.code
  }

  override fun hashCode(): Int {
    return code.hashCode()
  }
}
