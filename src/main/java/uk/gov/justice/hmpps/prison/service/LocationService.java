package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.transform.LocationTransformer;
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class LocationService {
    private final AgencyRepository agencyRepository;
    private final AgencyInternalLocationRepository agencyInternalLocationRepository;
    private final LocationRepository locationRepository;
    private final InmateRepository inmateRepository;
    private final CaseLoadService caseLoadService;

    public LocationService(
            final AgencyRepository agencyRepository,
            final AgencyInternalLocationRepository agencyInternalLocationRepository,
            final LocationRepository locationRepository,
            final InmateRepository inmateRepository,
            final CaseLoadService caseLoadService) {
        this.locationRepository = locationRepository;
        this.agencyInternalLocationRepository = agencyInternalLocationRepository;
        this.inmateRepository = inmateRepository;
        this.caseLoadService = caseLoadService;
        this.agencyRepository = agencyRepository;
    }

    @Transactional
    public List<Location> getUserLocations(final String username, final boolean includeNonRes) {
        final var caseLoad = caseLoadService.getWorkingCaseLoadForUser(username);
        if (caseLoad.isEmpty() || caseLoad.get().isAdminType()) {
            return Collections.emptyList();
        }
        return findLocationsFromAgencies(username, includeNonRes);
    }

    private List<Location> findLocationsFromAgencies(final String username, final boolean includeNonRes) {
        return agencyRepository.findAgenciesForCurrentCaseloadByUsername(username).stream()
            .flatMap(agency -> {
                final var locations = new ArrayList<Location>();
                locations.add(convertToLocation(agency));

                // Then retrieve all associated internal locations at configured level of granularity.
                locations.addAll(agencyInternalLocationRepository.findByAgencyIdAndActiveAndParentLocationIsNullAndCapacityGreaterThanAndTypeIsNotNull(agency.getAgencyId(), true, 0)
                        .stream()
                        .filter(l -> includeNonRes || l.isCertifiedFlag())
                    .map(LocationTransformer::fromAgencyInternalLocationPreferUserDesc).sorted(Comparator.comparing(Location::getDescription)).toList());
                return locations.stream();

        }).toList();
    }

    public Page<OffenderBooking> getInmatesFromLocation(final long locationId, final String username, final String orderByField, final Order order, final long offset, final long limit) {
        // validation check?
        locationRepository.findLocation(locationId, username);

        final var colSort = StringUtils.isNotBlank(orderByField) ? orderByField : GlobalSearchService.DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT;

        return inmateRepository.findInmatesByLocation(
                locationId,
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

    public Optional<Location> getLocationByCode(final String code) {
        return agencyInternalLocationRepository.findOneByDescription(code)
            .map(LocationTransformer::fromAgencyInternalLocationPreferUserDesc);
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
