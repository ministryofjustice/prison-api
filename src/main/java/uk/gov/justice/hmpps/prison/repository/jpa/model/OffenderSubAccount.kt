package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.Hibernate
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable
import java.math.BigDecimal

@Embeddable
data class OffenderSubAccountId(
  @Column(name = "CASELOAD_ID", insertable = false)
  val prisonId: String,

  @Column(name = "OFFENDER_ID", nullable = false)
  val offenderId: Long,

  @Column(name = "TRUST_ACCOUNT_CODE", nullable = false)
  val accountCode: Long,
) : Serializable

@Entity
@Table(name = "OFFENDER_SUB_ACCOUNTS")
@EntityOpen
class OffenderSubAccount(

  @EmbeddedId
  val id: OffenderSubAccountId,

  @Column(name = "BALANCE", nullable = false)
  var balance: BigDecimal,

  @Column(name = "HOLD_BALANCE")
  var holdBalance: BigDecimal? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as OffenderSubAccount
    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}
