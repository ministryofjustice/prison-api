package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable
import java.math.BigDecimal
import kotlin.jvm.javaClass

@Embeddable
class OffenderTrustAccountId(
  @Column(name = "CASELOAD_ID", nullable = false)
  val prisonId: String,

  @Column(name = "OFFENDER_ID", nullable = false)
  val offenderId: Long,
) : Serializable

@Entity
@EntityOpen
@Table(name = "OFFENDER_TRUST_ACCOUNTS")
data class OffenderTrustAccount(

  @EmbeddedId
  val id: OffenderTrustAccountId,

  @Column(name = "ACCOUNT_CLOSED_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val accountClosed: Boolean,

) {

  @Column(name = "HOLD_BALANCE")
  var holdBalance: BigDecimal? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as OffenderTrustAccount
    return id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()
}
