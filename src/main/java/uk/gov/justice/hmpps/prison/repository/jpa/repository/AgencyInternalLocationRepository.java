package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;

import java.util.List;
import java.util.Optional;

public interface AgencyInternalLocationRepository extends CrudRepository<AgencyInternalLocation, Long> {

    List<AgencyInternalLocation> findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag(final String agencyId, final String locationType, final ActiveFlag activeFlag);

    Optional<AgencyInternalLocation> findOneByDescription(final String description);

    Optional<AgencyInternalLocation> findByLocationCodeAndAgencyId(final String locationCode, final String agencyId);
}
