package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@EqualsAndHashCode(callSuper = false)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderTransaction.Pk.class)
@Table(name = "OFFENDER_TRANSACTIONS")
public class OffenderTransactionHistory extends AuditableEntity {

    @Id
    @Column(name = "TXN_ID", nullable = false, insertable = false, updatable = false)
    private Long transactionId;

    @Id
    @Column(name = "TXN_ENTRY_SEQ", nullable = false, insertable = false, updatable = false)
    private Long transactionEntrySequence;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OFFENDER_ID", nullable = false)
    private Offender offender;

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

    @Column(name = "TXN_POSTING_TYPE", nullable = false, length = 12)
    private String postingType;

    @Column(name = "CASELOAD_ID", nullable = false, length = 6)
    private String agencyId;

    @JoinColumns({
        @JoinColumn(name = "TXN_ID", referencedColumnName = "TXN_ID"),
        @JoinColumn(name = "TXN_ENTRY_SEQ", referencedColumnName = "TXN_ENTRY_SEQ")
    })
    @OneToMany
    @Default
    private List<OffenderTransactionDetails> relatedTransactionDetails = new ArrayList<>();

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private Long transactionId;
        private Long transactionEntrySequence;
    }
}
