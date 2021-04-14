package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BedAssignmentHistoriesRepository extends PagingAndSortingRepository<BedAssignmentHistory, BedAssignmentHistory.BedAssignmentHistoryPK> {

    @Query("select coalesce(max(b.bedAssignmentHistoryPK.sequence), 0) from BedAssignmentHistory b where b.bedAssignmentHistoryPK.offenderBookingId = :bookingId")
    Integer getMaxSeqForBookingId(@Param("bookingId") Long bookingId);

    List<BedAssignmentHistory> findAllByBedAssignmentHistoryPKOffenderBookingId(Long offenderBookingId);

    Optional<BedAssignmentHistory> findByBedAssignmentHistoryPKOffenderBookingIdAndBedAssignmentHistoryPKSequence(Long offenderBookingId, Integer sequence);

    Page<BedAssignmentHistory> findAllByBedAssignmentHistoryPKOffenderBookingId(Long offenderBookingId, Pageable pageable);

    @Query("select ba from BedAssignmentHistory ba where ba.livingUnitId = :livingUnitId and (ba.assignmentEndDateTime is null or :fromDate <= ba.assignmentEndDateTime) and :toDate >= ba.assignmentDateTime")
    List<BedAssignmentHistory> findByLivingUnitIdAndDateTimeRange(@Param("livingUnitId") long livingUnitId, @Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);

    @Query("select ba from BedAssignmentHistory ba where (ba.assignmentEndDateTime is null or :fromDate <= ba.assignmentEndDateTime) and :toDate >= ba.assignmentDateTime")
    List<BedAssignmentHistory> findByDateTimeRange(@Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);
}