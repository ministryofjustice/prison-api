package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFixedTermRecall;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffenderFixedTermRecallRepository extends CrudRepository<OffenderFixedTermRecall, Long>{

}
