package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;

public interface AgencyLocationRepository extends CrudRepository<AgencyLocation, String>, JpaSpecificationExecutor<AgencyLocation> {
}
