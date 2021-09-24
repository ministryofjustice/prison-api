package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;

import java.util.Optional;

@Repository
public interface OffenderBookingRepository extends PagingAndSortingRepository<OffenderBooking, Long>, JpaSpecificationExecutor<OffenderBooking> {
    Optional<OffenderBooking> findByOffenderNomsIdAndActive(String nomsId, boolean active);
    Optional<OffenderBooking> findByOffenderNomsIdAndBookingSequence(String nomsId, Integer bookingSequence);
}
