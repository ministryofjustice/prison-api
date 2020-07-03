package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.VisitInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRepository extends PagingAndSortingRepository<VisitInformation, String> {
    Page<VisitInformation> findAllByBookingId(Long bookingId, Pageable pageable);
}
