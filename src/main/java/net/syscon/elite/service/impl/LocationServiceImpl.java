package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.LocationProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.syscon.elite.service.SearchOffenderService.DEFAULT_OFFENDER_SORT;

/**
 * Location API service implementation.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class LocationServiceImpl implements LocationService {
    private final AgencyRepository agencyRepository;
    private final LocationRepository locationRepository;
    private final InmateRepository inmateRepository;
    private final CaseLoadService caseLoadService;
    private final LocationGroupService locationGroupService;
    private final String locationTypeGranularity;

    public LocationServiceImpl(
            final AgencyRepository agencyRepository,
            final LocationRepository locationRepository,
            final InmateRepository inmateRepository,
            final CaseLoadService caseLoadService,
            final LocationGroupService locationGroupService,
            @Value("${api.users.me.locations.locationType:WING}") final String locationTypeGranularity) throws IOException {
        this.locationRepository = locationRepository;
        this.inmateRepository = inmateRepository;
        this.caseLoadService = caseLoadService;
        this.locationGroupService = locationGroupService;
        this.agencyRepository = agencyRepository;
        this.locationTypeGranularity = locationTypeGranularity;
    }

    @Override
    public List<Location> getUserLocations(final String username) {
        final var caseLoad = caseLoadService.getWorkingCaseLoadForUser(username);
        if (caseLoad.isEmpty() || caseLoad.get().isAdminType()) {
            return Collections.emptyList();
        }
        return findLocationsFromAgencies(username);
    }

    private List<Location> findLocationsFromAgencies(final String username) {
        return agencyRepository.findAgenciesForCurrentCaseloadByUsername(username).stream().flatMap(agency -> {

            final var locations = new ArrayList<Location>();
            locations.add(convertToLocation(agency));

            // Then retrieve all associated internal locations at configured level of granularity.
            final var agencyLocations = locationRepository.findLocationsByAgencyAndType(
                    agency.getAgencyId(), locationTypeGranularity, true);

            agencyLocations.forEach(a -> a.setDescription(LocationProcessor.formatLocation(a.getDescription())));
            locations.addAll(agencyLocations);
            return locations.stream();

        }).collect(Collectors.toList());
    }

    @Override
    public Page<OffenderBooking> getInmatesFromLocation(final long locationId, final String username, final String query, final String orderByField, final Order order, final long offset, final long limit) {
        // validation check?
        locationRepository.findLocation(locationId, username);

        final var colSort = StringUtils.isNotBlank(orderByField) ? orderByField : DEFAULT_OFFENDER_SORT;

        final var inmates = inmateRepository.findInmatesByLocation(
                locationId,
                locationTypeGranularity,
                getWorkingCaseLoad(username),
                query,
                colSort,
                order,
                offset,
                limit);

        return inmates;
    }

    @Override
    public Location getLocation(final long locationId) {
        return locationRepository.getLocation(locationId).orElseThrow(EntityNotFoundException.withId(locationId));
    }

    /**
     * Get all cells for the prison/agency then filter them using the named pattern
     * defined in the groups.properties file.
     */
    @Override
    @VerifyAgencyAccess
    public List<Location> getCellLocationsForGroup(final String agencyId, final String groupName) {

        final var groupFilters = locationGroupService.locationGroupFilters(agencyId, groupName);

        final var cells = locationRepository.findLocationsByAgencyAndType(agencyId, "CELL", false);

        cells.forEach(c -> c.setDescription(LocationProcessor.formatLocation(c.getDescription())));

        final var cellLocations = groupFilters.stream()
                .flatMap(groupFilter -> cells.stream().filter(groupFilter))
                .collect(Collectors.toList());

        if (cellLocations.isEmpty()) {
            throw ConfigException.withMessage("There are no cells set up for location '%s'", groupName);
        }
        return cellLocations;
    }

    private String getWorkingCaseLoad(final String username) {
        final var workingCaseLoad = caseLoadService.getWorkingCaseLoadForUser(username);

        return workingCaseLoad.map(CaseLoad::getCaseLoadId).orElse(null);
    }

    private Location convertToLocation(final Agency agency) {
        return Location.builder()
                .locationId(-1L)
                .agencyId(agency.getAgencyId())
                .locationType(agency.getAgencyType())
                .description(LocationProcessor.formatLocation(agency.getDescription()))
                .locationPrefix(agency.getAgencyId())
                .build();
    }
}
