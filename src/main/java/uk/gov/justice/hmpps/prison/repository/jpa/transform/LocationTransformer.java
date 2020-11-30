package uk.gov.justice.hmpps.prison.repository.jpa.transform;

import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

public class LocationTransformer {

    public static Location fromAgencyInternalLocation(AgencyInternalLocation agencyInternalLocation) {
        final var location = Location.builder()
                .locationId(agencyInternalLocation.getLocationId())
                .locationType(agencyInternalLocation.getLocationType())
                .description(agencyInternalLocation.getDescription())
                .locationUsage("")
                .agencyId(agencyInternalLocation.getAgencyId())
                .parentLocationId(agencyInternalLocation.getParentLocation().getLocationId())
                .currentOccupancy(agencyInternalLocation.getCurrentOccupancy())
                .locationPrefix("")
                .operationalCapacity(agencyInternalLocation.getOperationalCapacity())
                .userDescription(agencyInternalLocation.getUserDescription())
                .internalLocationCode(agencyInternalLocation.getLocationCode())
                .build();
        return LocationProcessor.processLocation(location);
    }
}

