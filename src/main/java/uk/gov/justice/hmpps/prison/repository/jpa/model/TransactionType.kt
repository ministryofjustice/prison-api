package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "TRANSACTION_TYPES")
class TransactionType(
  @Id
  @Column(name = "TXN_TYPE", nullable = false)
  val type: String,

  @Column(name = "DESCRIPTION", nullable = false)
  val description: String,
)
