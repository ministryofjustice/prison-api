package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_NO_PAY_PERIODS")
@EqualsAndHashCode(callSuper = false)
public class OffenderNoPayPeriod extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_NO_PAY_PERIOD_ID", nullable = false)
    @SequenceGenerator(name = "OFFENDER_NO_PAY_PERIOD_ID", sequenceName = "OFFENDER_NO_PAY_PERIOD_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_NO_PAY_PERIOD_ID")
    private Long id;

    @Column(name = "OFFENDER_BOOK_ID", nullable = false)
    private Long bookingId;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    // From reference domain NO_PAY_RSN. Not modelled as a reference code (yet) as it's not being used in production code.
    @Column(name = "REASON_CODE")
    private String reasonCode;
}
