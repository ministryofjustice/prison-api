package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;
import uk.gov.justice.hmpps.prison.api.model.OffenderFinePaymentDto;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"offenderBooking", "sequence"}, callSuper = false)
@Table(name = "OFFENDER_FINE_PAYMENTS")
@IdClass(OffenderFinePayment.PK.class)
public class OffenderFinePayment extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private Integer sequence;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "PAYMENT_SEQ")
    private Integer sequence;

    @Column(name = "PAYMENT_DATE")
    private LocalDate paymentDate;

    @Column(name = "PAYMENT_AMOUNT")
    private BigDecimal paymentAmount;

    public OffenderFinePaymentDto getOffenderFinePaymentDto() {
        return OffenderFinePaymentDto.builder()
            .bookingId(offenderBooking.getBookingId())
            .sequence(sequence)
            .paymentAmount(paymentAmount)
            .paymentDate(paymentDate)
            .build();
    }
}
