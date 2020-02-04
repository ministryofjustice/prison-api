package net.syscon.elite.repository.jpa.transform;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.repository.jpa.model.AgencyInternalLocation;
import net.syscon.elite.service.support.LocationProcessor;

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

