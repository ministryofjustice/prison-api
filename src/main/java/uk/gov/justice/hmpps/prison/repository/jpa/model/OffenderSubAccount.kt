package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor
import lombok.ToString
import java.io.Serializable
import java.math.BigDecimal

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderSubAccount.Pk::class)
@Table(name = "OFFENDER_SUB_ACCOUNTS")
class OffenderSubAccount {
  @Id
  @Column(name = "CASELOAD_ID", nullable = false, insertable = false, updatable = false)
  private var prisonId: String? = null

  @Id
  @Column(name = "OFFENDER_ID", nullable = false, insertable = false, updatable = false)
  private var offenderId: Long? = null

  @Id
  @Column(name = "TRUST_ACCOUNT_CODE", nullable = false, insertable = false, updatable = false)
  private var accountCode: Long? = null

  @Column(name = "BALANCE", nullable = false)
  private var balance: BigDecimal? = null

  @EqualsAndHashCode
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  class Pk : Serializable {
    private var prisonId: String? = null
    private var offenderId: Long? = null
    private var accountCode: Long? = null
  }
}
