package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_BOOKINGS")
public class OffenderBookingPendingDeletion {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_ID", nullable = false)
    private OffenderAliasPendingDeletion offenderAlias;
}
