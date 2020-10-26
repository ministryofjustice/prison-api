package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderTransaction.Pk.class)
@Table(name = "OFFENDER_TRANSACTIONS")
public class OffenderTransactionHistory {

    @Id
    @Column(name = "TXN_ID", nullable = false, insertable = false, updatable = false)
    private Long transactionId;

    @Id
    @Column(name = "TXN_ENTRY_SEQ", nullable = false, insertable = false, updatable = false)
    private Long transactionEntrySequence;

    @Column(name = "OFFENDER_ID", nullable = false)
    private Long offenderId;

    @Column(name = "TXN_ENTRY_DATE", nullable = false)
    private LocalDate entryDate;

    @Column(name = "TXN_TYPE", nullable = false, length = 6)
    private String transactionType;

    @Column(name = "TXN_ENTRY_DESC", length = 240)
    private String entryDescription;

    @Column(name = "TXN_REFERENCE_NUMBER", length = 12)
    private String referenceNumber;

    @Column(name = "TXN_ENTRY_AMOUNT", nullable = false)
    private BigDecimal entryAmount;

    @Column(name = "SUB_ACCOUNT_TYPE", nullable = false, length = 12)
    private String accountType;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private Long transactionId;
        private Long transactionEntrySequence;
    }
}
