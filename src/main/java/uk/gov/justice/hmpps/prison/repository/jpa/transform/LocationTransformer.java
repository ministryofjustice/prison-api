package uk.gov.justice.hmpps.prison.repository.jpa.transform;

import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

public class LocationTransformer {

    public static Location fromAgencyInternalLocation(AgencyInternalLocation agencyInternalLocation) {
        return LocationProcessor.processLocation(buildLocation(agencyInternalLocation));
    }

    public static Location fromAgencyInternalLocationPreferUserDesc(AgencyInternalLocation agencyInternalLocation) {
        return LocationProcessor.processLocation(buildLocation(agencyInternalLocation), true);
    }

    private static Location buildLocation(final AgencyInternalLocation agencyInternalLocation) {
        return Location.builder()
            .locationId(agencyInternalLocation.getLocationId())
            .locationType(agencyInternalLocation.getLocationType())
            .description(agencyInternalLocation.getDescription())
            .locationUsage("")
            .agencyId(agencyInternalLocation.getAgencyId())
            .parentLocationId(agencyInternalLocation.getParentLocation() != null ? agencyInternalLocation.getParentLocation().getLocationId() : null)
            .currentOccupancy(agencyInternalLocation.getCurrentOccupancy())
            .locationPrefix("")
            .operationalCapacity(agencyInternalLocation.getOperationalCapacity())
            .userDescription(agencyInternalLocation.getUserDescription())
            .internalLocationCode(agencyInternalLocation.getLocationCode())
            .build();
    }
}

