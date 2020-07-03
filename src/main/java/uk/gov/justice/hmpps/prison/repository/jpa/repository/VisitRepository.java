package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation;

@Repository
public interface VisitRepository extends PagingAndSortingRepository<VisitInformation, String> {
    Page<VisitInformation> findAllByBookingId(Long bookingId, Pageable pageable);
}
