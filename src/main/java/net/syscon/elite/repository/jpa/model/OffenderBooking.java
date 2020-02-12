package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ListIndexBase;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_BOOKINGS")
public class OffenderBooking {
    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @OrderColumn(name = "MILITARY_SEQ")
    @ListIndexBase(1)
    @ElementCollection
    @CollectionTable(name = "OFFENDER_MILITARY_RECORDS", joinColumns = @JoinColumn(name = "OFFENDER_BOOK_ID"))
    private List<OffenderMilitaryRecord> militaryRecords;
}
