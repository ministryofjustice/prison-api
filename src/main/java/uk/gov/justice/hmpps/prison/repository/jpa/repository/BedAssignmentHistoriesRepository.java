package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;

import java.util.List;

public interface BedAssignmentHistoriesRepository extends CrudRepository<BedAssignmentHistory, BedAssignmentHistory.BedAssignmentHistoryPK> {

    @Query("select coalesce(max(b.bedAssignmentHistoryPK.sequence), 0) from BedAssignmentHistory b where b.bedAssignmentHistoryPK.offenderBookingId = :bookingId")
    Integer getMaxSeqForBookingId(@Param("bookingId") Long bookingId);

    List<BedAssignmentHistory> findAllByBedAssignmentHistoryPKOffenderBookingId(Long offenderBookingId);
}
