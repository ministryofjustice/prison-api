package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.VisitorInformation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorRepository extends PagingAndSortingRepository<VisitorInformation, String> {
    List<VisitorInformation> findAllByVisitIdAndBookingId(Long visitId, Long bookingId);;
}
