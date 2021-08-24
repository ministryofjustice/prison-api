package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.AgencyRepository;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.LocationRepository;
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class LocationService {
    private final AgencyRepository agencyRepository;
    private final LocationRepository locationRepository;
    private final InmateRepository inmateRepository;
    private final CaseLoadService caseLoadService;
    private final String locationTypeGranularity;

    public LocationService(
            final AgencyRepository agencyRepository,
            final LocationRepository locationRepository,
            final InmateRepository inmateRepository,
            final CaseLoadService caseLoadService,
            @Value("${api.users.me.locations.locationType:WING}") final String locationTypeGranularity) {
        this.locationRepository = locationRepository;
        this.inmateRepository = inmateRepository;
        this.caseLoadService = caseLoadService;
        this.agencyRepository = agencyRepository;
        this.locationTypeGranularity = locationTypeGranularity;
    }

    @Transactional
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

    public Page<OffenderBooking> getInmatesFromLocation(final long locationId, final String username, final String orderByField, final Order order, final long offset, final long limit) {
        // validation check?
        locationRepository.findLocation(locationId, username);

        final var colSort = StringUtils.isNotBlank(orderByField) ? orderByField : GlobalSearchService.DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT;

        return inmateRepository.findInmatesByLocation(
                locationId,
                locationTypeGranularity,
                getWorkingCaseLoad(username),
                colSort,
                order,
                offset,
                limit);
    }

    public Location getLocation(final long locationId) {
        return getLocation(locationId, false);
    }

    public Location getLocation(final long locationId, boolean includeInactive) {
        return locationRepository
            .findLocation(locationId, includeInactive ? StatusFilter.ALL : StatusFilter.ACTIVE_ONLY)
            .orElseThrow(EntityNotFoundException.withId(locationId));
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
