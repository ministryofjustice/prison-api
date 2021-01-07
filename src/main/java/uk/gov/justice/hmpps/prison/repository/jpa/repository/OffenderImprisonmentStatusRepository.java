package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus;

import java.util.List;

@Repository
public interface OffenderImprisonmentStatusRepository extends CrudRepository<OffenderImprisonmentStatus, OffenderImprisonmentStatus.PK> {
    List<OffenderImprisonmentStatus> findByOffenderBookId(final long offenderBookId);

    List<OffenderImprisonmentStatus> findByOffenderBookIdAndLatestStatus(final long offenderBookId, final String latestStatus);

    @Query("select coalesce(max(s.imprisonStatusSeq), 0) from OffenderImprisonmentStatus s where s.offenderBookId = :bookingId")
    Integer getMaxSeqForBookingId(@Param("bookingId") Long bookingId);
}
