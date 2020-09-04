package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocationProfile;

import java.util.List;

public interface AgencyInternalLocationProfileRepository extends CrudRepository<AgencyInternalLocationProfile, Long> {
    List<AgencyInternalLocationProfile> findAllByLocationId(Long locationId);
}
