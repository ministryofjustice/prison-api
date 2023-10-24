package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.JoinColumnOrFormula
import org.hibernate.annotations.JoinColumnsOrFormulas
import org.hibernate.annotations.JoinFormula
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable

@Entity
@Table(name = "GANG_NON_ASSOCIATIONS")
@EntityOpen
@IdClass(GangNonAssociationId::class)
class GangNonAssociation(

  @Id
  @ManyToOne(optional = false)
  @JoinColumn(name = "GANG_CODE", nullable = false)
  val primaryGang: Gang,

  @Id
  @ManyToOne(optional = false)
  @JoinColumn(name = "NS_GANG_CODE", nullable = false)
  val secondaryGang: Gang,

  @ManyToOne(optional = false)
  @JoinColumnsOrFormulas(
    value = [
      JoinColumnOrFormula(
        formula = JoinFormula(
          value = "'" + NonAssociationReason.DOMAIN + "'",
          referencedColumnName = "domain",
        ),
      ),
      JoinColumnOrFormula(column = JoinColumn(name = "NS_REASON_CODE", referencedColumnName = "code")),
    ],
  )
  val nonAssociationReason: NonAssociationReason,

  @Column(name = "INTERNAL_LOCATION_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  var withinPrisonOnly: Boolean = true,

  @Column(name = "TRANSPORT_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  var transportPurposes: Boolean = false,

)

class GangNonAssociationId(
  val primaryGang: Gang? = null,
  val secondaryGang: Gang? = null,

) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as GangNonAssociationId

    if (primaryGang != other.primaryGang) return false
    if (secondaryGang != other.secondaryGang) return false

    return true
  }

  override fun hashCode(): Int {
    var result = primaryGang?.hashCode() ?: 0
    result = 31 * result + (secondaryGang?.hashCode() ?: 0)
    return result
  }
}
