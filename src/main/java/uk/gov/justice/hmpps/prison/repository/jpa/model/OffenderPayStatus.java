package uk.gov.justice.hmpps.prison.repository.jpa.model;

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
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_PAY_STATUSES")
@IdClass(OffenderPayStatus.PK.class)
@EqualsAndHashCode(callSuper = false)
public class OffenderPayStatus extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        @Column(name = "OFFENDER_BOOK_ID", updatable = false, insertable = false)
        private Long bookingId;

        @Column(name = "START_DATE", updatable = false, insertable = false)
        private LocalDate startDate;
    }

    @Id
    private Long bookingId;

    @Id
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;
}
