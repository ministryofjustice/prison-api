package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
    private String payTypeCode;

    @Column(name = "EVENT_ID")
    private Integer eventId;

    @Column(name = "PAY_AMOUNT")
    private BigDecimal payAmount;

    @Column(name = "PIECE_WORK")
    private BigDecimal pieceWork;

    @Column(name = "BONUS_PAY")
    private BigDecimal bonusPay;
}
