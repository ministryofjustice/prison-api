package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffenderBookingRepository extends
    PagingAndSortingRepository<OffenderBooking, Long>,
    JpaSpecificationExecutor<OffenderBooking>,
    CrudRepository<OffenderBooking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ob from OffenderBooking ob where ob.offender.nomsId = :nomsId")
    List<OffenderBooking> findAllByOffenderNomsIdForUpdate(String nomsId);

    Optional<OffenderBooking> findByOffenderNomsIdAndActive(String nomsId, boolean active);
    Optional<OffenderBooking> findByOffenderNomsIdAndBookingSequence(String nomsId, Integer bookingSequence);
    Optional<OffenderBooking> findByBookingId(Long bookingId);
}
