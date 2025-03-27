package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderTransaction.Pk.class)
@Table(name = "OFFENDER_TRANSACTIONS")
public class OffenderTransaction {

    @Id
    @Column(name = "TXN_ID", nullable = false, insertable = false, updatable = false)
    private Long transactionId;

    @Id
    @Column(name = "TXN_ENTRY_SEQ", nullable = false, insertable = false, updatable = false)
    private Long transactionEntrySequence;

    @Column(name = "OFFENDER_ID", nullable = false)
    private Long offenderId;

    @Column(name = "CASELOAD_ID", nullable = false)
    private String prisonId;

    @Column(name = "SUB_ACCOUNT_TYPE")
    private String subAccountType;

    @ManyToOne(optional = false)
    @NotFound(action = IGNORE)
    @JoinColumn(name = "TXN_TYPE", nullable = false)
    private TransactionType transactionType;

    @Column(name = "TXN_REFERENCE_NUMBER")
    private String transactionReferenceNumber;

    @Column(name = "CLIENT_UNIQUE_REF")
    private String clientUniqueRef;

    @Column(name = "TXN_ENTRY_DATE")
    private LocalDate entryDate;

    @Column(name = "TXN_ENTRY_DESC")
    private String entryDescription;

    @Column(name = "TXN_ENTRY_AMOUNT")
    private BigDecimal entryAmount;

    @Column(name = "TXN_POSTING_TYPE")
    private String postingType;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private Long transactionId;
        private Long transactionEntrySequence;
    }
}
