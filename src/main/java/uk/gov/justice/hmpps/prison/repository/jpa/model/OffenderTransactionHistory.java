package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@EntityListeners(AuditingEntityListener.class)
public class OffenderTransactionHistory {

    @Id
    @Column(name = "TXN_ID", nullable = false, insertable = false, updatable = false)
    private Long transactionId;

    @Id
    @Column(name = "TXN_ENTRY_SEQ", nullable = false, insertable = false, updatable = false)
    private Long transactionEntrySequence;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
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
    @BatchSize(size = 1000)
    private List<OffenderTransactionDetails> relatedTransactionDetails = new ArrayList<>();

    @Builder.Default
    @Transient
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "HOLD_CLEAR_FLAG")
    private Boolean holdingCleared;

    @Column(name = "CREATE_DATETIME")
    @CreatedDate
    private LocalDateTime createDatetime;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "CREATE_USER_ID")
    @CreatedBy
    private String createUserId;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "MODIFY_USER_ID")
    @LastModifiedBy
    private String modifyUserId;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "MODIFY_DATETIME")
    @LastModifiedDate
    private LocalDateTime modifyDatetime;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private Long transactionId;
        private Long transactionEntrySequence;
    }
}
