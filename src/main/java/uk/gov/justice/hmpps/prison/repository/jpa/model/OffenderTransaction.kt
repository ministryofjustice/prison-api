package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Embeddable
data class OffenderTransactionId(
  @Column(name = "TXN_ID", nullable = false)
  val transactionId: Long,

  @Column(name = "TXN_ENTRY_SEQ", nullable = false)
  val transactionEntrySequence: Long,
) : Serializable

@Entity
@EntityOpen
@Table(name = "OFFENDER_TRANSACTIONS")
class OffenderTransaction(

  @EmbeddedId
  val id: OffenderTransactionId,

  @Column(name = "OFFENDER_ID", nullable = false)
  var offenderId: Long,

  @Column(name = "CASELOAD_ID", nullable = false)
  var prisonId: String,

  @Column(name = "HOLD_NUMBER", insertable = false)
  var holdNumber: Long?,

  @Column(name = "HOLD_CLEAR_FLAG", nullable = false)
  var holdClearFlag: String?,

  @Column(name = "SUB_ACCOUNT_TYPE", nullable = false)
  var subAccountType: String,

  @ManyToOne(optional = false)
  @NotFound(action = NotFoundAction.IGNORE)
  @JoinColumn(name = "TXN_TYPE", nullable = false)
  var transactionType: TransactionType,

  @Column(name = "TXN_REFERENCE_NUMBER")
  var transactionReferenceNumber: String?,

  @Column(name = "CLIENT_UNIQUE_REF")
  var clientUniqueRef: String?,

  @Column(name = "TXN_ENTRY_DATE", nullable = false)
  var entryDate: LocalDate,

  @Column(name = "TXN_ENTRY_DESC")
  var entryDescription: String?,

  @Column(name = "TXN_ENTRY_AMOUNT", nullable = false)
  var entryAmount: BigDecimal,

  @Column(name = "TXN_POSTING_TYPE", nullable = false)
  var postingType: String,
)
