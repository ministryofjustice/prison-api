package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitorInformation;

import java.util.List;

@Repository
public interface VisitorRepository extends PagingAndSortingRepository<VisitorInformation, String> {
    List<VisitorInformation> findAllByVisitIdAndBookingId(Long visitId, Long bookingId);;
}
