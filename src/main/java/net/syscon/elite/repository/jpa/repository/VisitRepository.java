package net.syscon.elite.repository.jpa.repository;

import org.springframework.data.domain.Page;
import net.syscon.elite.repository.jpa.model.VisitInformation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRepository extends PagingAndSortingRepository<VisitInformation, String> {
    Page<VisitInformation> findAllByBookingId(Long bookingId, Pageable pageable);
}
