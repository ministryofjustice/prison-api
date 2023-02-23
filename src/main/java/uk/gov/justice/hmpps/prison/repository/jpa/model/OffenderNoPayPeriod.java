package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_NO_PAY_PERIODS")
@EqualsAndHashCode(callSuper = false)
public class OffenderNoPayPeriod extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_NO_PAY_PERIOD_ID")
    private Long id;

    @Column(name = "OFFENDER_BOOK_ID", nullable = false)
    private Long bookingId;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;
}
