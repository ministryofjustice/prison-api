package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BedAssignmentHistoriesRepository extends
    PagingAndSortingRepository<BedAssignmentHistory, BedAssignmentHistory.BedAssignmentHistoryPK>,
    CrudRepository<BedAssignmentHistory, BedAssignmentHistoryPK> {

    @Query("select coalesce(max(b.bedAssignmentHistoryPK.sequence), 0) from BedAssignmentHistory b where b.bedAssignmentHistoryPK.offenderBookingId = :bookingId")
    Integer getMaxSeqForBookingId(@Param("bookingId") Long bookingId);

    List<BedAssignmentHistory> findAllByBedAssignmentHistoryPKOffenderBookingId(Long offenderBookingId);

    Optional<BedAssignmentHistory> findByBedAssignmentHistoryPKOffenderBookingIdAndBedAssignmentHistoryPKSequence(Long offenderBookingId, Integer sequence);

    @EntityGraph(type = EntityGraphType.FETCH, value = "bed-history-with-location")
    Page<BedAssignmentHistory> findAllByBedAssignmentHistoryPKOffenderBookingId(Long offenderBookingId, Pageable pageable);

    @Query("select ba from BedAssignmentHistory ba where ba.livingUnitId = :livingUnitId and (ba.assignmentEndDateTime is null or :fromDate <= ba.assignmentEndDateTime) and :toDate >= ba.assignmentDateTime")
    List<BedAssignmentHistory> findByLivingUnitIdAndDateTimeRange(@Param("livingUnitId") long livingUnitId, @Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);

    @EntityGraph(type = EntityGraphType.FETCH, value = "bed-history-with-booking")
    List<BedAssignmentHistory> findBedAssignmentHistoriesByAssignmentDateAndLivingUnitIdIn(LocalDate assignmentDate, Set<Long> livingUnitIds);

    void deleteByOffenderBooking_BookingId(Long bookingId);
}
