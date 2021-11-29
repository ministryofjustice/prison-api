package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyInternalLocationRepository extends JpaRepository<AgencyInternalLocation, Long> {

    List<AgencyInternalLocation> findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive(final String agencyId, final String locationType, final boolean active);

    List<AgencyInternalLocation> findAgencyInternalLocationsByAgencyIdAndLocationType(final String agencyId, final String locationType);

    Optional<AgencyInternalLocation> findOneByDescription(final String description);

    Optional<AgencyInternalLocation> findOneByDescriptionAndAgencyId(final String description, final String agencyId);

    Optional<AgencyInternalLocation> findOneByLocationId(final Long locationId);

    List<AgencyInternalLocation> findByLocationCodeAndAgencyId(final String locationCode, final String agencyId);

    List<AgencyInternalLocation> findByAgencyIdAndLocationTypeAndActiveAndParentLocationIsNull(final String agencyId, final String locationType, boolean active);
}
