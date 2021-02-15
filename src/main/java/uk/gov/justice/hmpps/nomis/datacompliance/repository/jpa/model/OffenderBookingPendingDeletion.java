package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = {"offenderAlias"})
@Table(name = "OFFENDER_BOOKINGS")
public class OffenderBookingPendingDeletion {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Column(name = "AGY_LOC_ID")
    private String agencyLocationId;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_ID", nullable = false)
    private OffenderAliasPendingDeletion offenderAlias;

    @Singular
    @OneToMany(mappedBy = "offenderBooking")
    private List<OffenderChargePendingDeletion> offenderCharges;

    @Singular
    @OneToMany(mappedBy = "offenderBooking")
    private List<OffenderAlertPendingDeletion> offenderAlerts;
}
