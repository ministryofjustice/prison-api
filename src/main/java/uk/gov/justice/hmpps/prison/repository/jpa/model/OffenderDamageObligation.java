package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "OFFENDER_DAMAGE_OBLIGATIONS")
public class OffenderDamageObligation extends AuditableEntity  {

    @Id
    @Column(name = "OFFENDER_DMG_OBLIGATION_ID", nullable = false, insertable = false, updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OFFENDER_ID", nullable = false)
    private Offender offender;

    @Column(name = "REFERENCE_NO", nullable = false, length = 20)
    private String referenceNumber;

    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime endDateTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation prison;

    @Column(name = "AMOUNT_TO_PAY",nullable = false)
    private BigDecimal amountToPay;

    @Column(name = "PAID_AMOUNT")
    private BigDecimal amountPaid;

    @Column(name = "OBLIGATION_STATUS", nullable = false, length = 12)
    private String status;

    @Column(name = "COMMENT_TEXT", length = 4000)
    private String comment;

    public enum Status {
        ACTIVE("ACTIVE"), PAID("PAID"), INACT("INACT"),
        ONH("ONH"), APPEAL("APPEAL"), ALL("");
        private String code;
        Status(String code) {
            this.code = code;
        }
        public String code() {
            return code;
        }
        public static Status forCode(String code) {
            return Arrays.stream(Status.values())
                .filter(c -> c.equals(code.toLowerCase()))
                .findFirst()
                .orElse(ALL);
        }
    }
}


