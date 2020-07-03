package uk.gov.justice.hmpps.nomis.prison.datacompliance.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = {"offenderBooking"})
@Table(name = "OFFENDER_CHARGES")
public class OffenderChargePendingDeletion {

    @Id
    @Column(name = "OFFENDER_CHARGE_ID")
    private Long offenderChargeId;

    @NotNull
    @Column(name = "OFFENCE_CODE", nullable = false)
    private String offenceCode;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBookingPendingDeletion offenderBooking;
}
