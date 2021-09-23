package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;

import java.util.List;
import java.util.Optional;

public interface AgencyLocationRepository extends CrudRepository<AgencyLocation, String>, JpaSpecificationExecutor<AgencyLocation> {

    Optional<AgencyLocation> findByIdAndDeactivationDateIsNull(String id);

    Optional<AgencyLocation> findByIdAndTypeAndActiveAndDeactivationDateIsNull(String id, AgencyLocationType type, boolean active);

    List<AgencyLocation> findByTypeAndActiveAndDeactivationDateIsNull(AgencyLocationType type, boolean active);

}
