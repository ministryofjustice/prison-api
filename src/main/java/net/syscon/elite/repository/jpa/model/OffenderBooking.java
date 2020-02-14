package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.syscon.elite.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;
import org.hibernate.annotations.ListIndexBase;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_BOOKINGS")
public class OffenderBooking {
    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Column(name = "ACTIVE_FLAG")
    @Enumerated(EnumType.STRING)
    private ActiveFlag activeFlag;

    @OrderColumn(name = "MILITARY_SEQ")
    @ListIndexBase(1)
    @OneToMany(mappedBy = "bookingAndSequence.offenderBooking", cascade = CascadeType.ALL)
    private List<OffenderMilitaryRecord> militaryRecords;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "OFFENDER_BOOK_ID")
    private List<OffenderVisit> visits;

    public void addOffenderMilitaryRecord(final OffenderMilitaryRecord omr) {
        militaryRecords.add(omr);
        omr.setBookingAndSequence(new BookingAndSequence(this, militaryRecords.size()));
    }
}
