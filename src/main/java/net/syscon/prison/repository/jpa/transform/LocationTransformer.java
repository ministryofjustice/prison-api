package net.syscon.prison.repository.jpa.transform;

import net.syscon.prison.api.model.Location;
import net.syscon.prison.repository.jpa.model.AgencyInternalLocation;
import net.syscon.prison.service.support.LocationProcessor;

public class LocationTransformer {

    public static Location fromAgencyInternalLocation(AgencyInternalLocation agencyInternalLocation) {
        final var location = Location.builder()
                .locationId(agencyInternalLocation.getLocationId())
                .locationType(agencyInternalLocation.getLocationType())
                .description(agencyInternalLocation.getDescription())
                .locationUsage("")
                .agencyId(agencyInternalLocation.getAgencyId())
                .parentLocationId(agencyInternalLocation.getParentLocationId())
                .currentOccupancy(agencyInternalLocation.getCurrentOccupancy())
                .locationPrefix("")
                .operationalCapacity(agencyInternalLocation.getOperationalCapacity())
                .userDescription(agencyInternalLocation.getUserDescription())
                .internalLocationCode(agencyInternalLocation.getLocationCode())
                .build();
        return LocationProcessor.processLocation(location);
    }
}

