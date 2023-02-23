package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_TRANSACTION_DETAILS")
public class OffenderTransactionDetails extends AuditableEntity {

    public final static String ACTIVITY_PAY_TYPE_CODE = "SESSION";

    @Id
    @Column(name = "TXN_DETAIL_ID")
    private Long id;

    @Column(name = "TXN_ID")
    private Long transactionId;

    @Column(name = "TXN_ENTRY_SEQ")
    private Long transactionEntrySequence;

    @Column(name = "CALENDAR_DATE")
    private LocalDate calendarDate;

    @Column(name = "PAY_TYPE_CODE")
    private String paymentTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + PaymentType.PAY_TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "PAY_TYPE_CODE", referencedColumnName = "code", insertable = false, updatable = false))
    })
    private PaymentType noneActivityPaymentType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "EVENT_ID", referencedColumnName = "EVENT_ID", insertable = false, updatable = false)
    private OffenderCourseAttendance event;

    @Column(name = "PAY_AMOUNT")
    private BigDecimal payAmount;

    @Column(name = "PIECE_WORK")
    private BigDecimal pieceWork;

    @Column(name = "BONUS_PAY")
    private BigDecimal bonusPay;

    @Builder.Default
    @Transient
    private BigDecimal currentBalance = BigDecimal.ZERO;

    public Boolean isAttachedToPaidActivity() {
        return paymentTypeCode != null && paymentTypeCode.equals(ACTIVITY_PAY_TYPE_CODE);
    }
}
