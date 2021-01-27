package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;

import java.util.Optional;

public interface AgencyLocationRepository extends CrudRepository<AgencyLocation, String>, JpaSpecificationExecutor<AgencyLocation> {

    Optional<AgencyLocation> findByIdAndDeactivationDateIsNull(String id);

    Optional<AgencyLocation> findByIdAndTypeAndActiveFlagAndDeactivationDateIsNull(String id, AgencyLocationType type, ActiveFlag activeFlag);
}
