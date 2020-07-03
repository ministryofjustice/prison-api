package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.VisitorInformation;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorRepository extends PagingAndSortingRepository<VisitorInformation, String> {
    List<VisitorInformation> findAllByVisitIdAndBookingId(Long visitId, Long bookingId);;
}
