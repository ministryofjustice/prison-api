package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.service.LocationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Location API service implementation.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class LocationServiceImpl implements LocationService {
    private final AgencyRepository agencyRepository;
    private final LocationRepository locationRepository;

    @Value("${api.users.me.locations.locationType:WING}")
    private String locationTypeGranularity;

    @Value("${api.users.me.locations.depth:1}")
    private Integer locationDepth;

    public LocationServiceImpl(AgencyRepository agencyRepository, LocationRepository locationRepository) {
        this.agencyRepository = agencyRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public List<Location> getUserLocations(String username) {
        final List<Location> locations = new ArrayList<>();

        // Step 1 - Get all agencies associated with user
        List<Agency> agencies = agencyRepository.findAgenciesByUsername(username);

        // Step 2 - Evaluate number of agencies to determine next step
        int agencyCount = agencies.size();

        // TODO: Implement support for hierarchical location retrieval (placeholder - may not be needed).
        if (agencyCount == 1) {
            // User has one agency so the agency will be used as a main location together with associated internal
            // locations of a granularity (e.g. 'WING') as determined by configuration setting.
            log.debug("User [{}] is associated with one agency.", username);
            Agency agency = agencies.get(0);

            // Start with agency converted to location
            locations.add(convertToLocation(agency));

            // Then retrieve all associated internal locations at configured level of granularity.
            final List<Location> agencyLocations = locationRepository.findLocationsByAgencyAndType(
                    agency.getAgencyId(), locationTypeGranularity, locationDepth
            );

            locations.addAll(agencyLocations);
        } else if (agencyCount > 1) {
            // User has multiple agencies so these will be used directly as locations.
            log.debug("User [{}] is associated with {} agencies.", username, agencyCount);

            // Add retrieved agencies converted to locations
            locations.addAll(agencies.stream().map(this::convertToLocation).collect(Collectors.toList()));
        } else {
            // TODO: Decide what to do if no agencies associated with current user - is this even possible?
            log.debug("User [{}] is not associated with any agencies.", username);
        }

        return locations;
    }

    private Location convertToLocation(Agency agency) {
        return Location.builder()
                .locationId(-1L)
                .agencyId(agency.getAgencyId())
                .locationType(agency.getAgencyType())
                .description(agency.getDescription())
                .locationPrefix(agency.getAgencyId())
                .build();
    }

}
