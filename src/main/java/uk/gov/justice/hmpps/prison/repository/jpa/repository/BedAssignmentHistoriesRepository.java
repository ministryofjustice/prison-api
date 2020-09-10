package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;

import java.time.LocalDate;
import java.util.List;

public interface BedAssignmentHistoriesRepository extends PagingAndSortingRepository<BedAssignmentHistory, BedAssignmentHistory.BedAssignmentHistoryPK> {

    @Query("select coalesce(max(b.bedAssignmentHistoryPK.sequence), 0) from BedAssignmentHistory b where b.bedAssignmentHistoryPK.offenderBookingId = :bookingId")
    Integer getMaxSeqForBookingId(@Param("bookingId") Long bookingId);

    List<BedAssignmentHistory> findAllByBedAssignmentHistoryPKOffenderBookingId(Long offenderBookingId);

    Page<BedAssignmentHistory> findAllByBedAssignmentHistoryPKOffenderBookingId(Long offenderBookingId, Pageable pageable);

    @Query("select ba from BedAssignmentHistory ba where ba.livingUnitId = :livingUnitId and (ba.assignmentEndDate is null or :fromDate <= trunc(ba.assignmentEndDate)) and :toDate >= trunc(ba.assignmentDate)")
    List<BedAssignmentHistory> findByLivingUnitIdAndDateRange(@Param("livingUnitId") long livingUnitId, @Param("fromDate") LocalDate from, @Param("toDate") LocalDate to);
}