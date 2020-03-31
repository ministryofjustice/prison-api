package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.BedAssignmentHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BedAssignmentHistoriesRepository extends CrudRepository<BedAssignmentHistory, BedAssignmentHistory.BookingAndSequence> {

    @Query("select coalesce(max(b.bookingAndSequence.sequence), 0) from BedAssignmentHistory b where b.bookingAndSequence.offenderBooking.bookingId = :bookingId")
    Integer getMaxSeqForBookingId(@Param("bookingId") Long bookingId);
}
