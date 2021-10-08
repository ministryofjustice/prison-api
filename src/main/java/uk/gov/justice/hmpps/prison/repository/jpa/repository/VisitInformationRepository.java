package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation;

@Repository
public interface VisitInformationRepository extends PagingAndSortingRepository<VisitInformation, String>, JpaSpecificationExecutor<VisitInformation> {
}
