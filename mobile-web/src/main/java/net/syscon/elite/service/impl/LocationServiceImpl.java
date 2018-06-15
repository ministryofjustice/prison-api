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
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.ConfigException;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.LocationService;
import net.syscon.elite.service.support.LocationProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.syscon.elite.service.SearchOffenderService.DEFAULT_OFFENDER_SORT;
import static org.springframework.util.StringUtils.commaDelimitedListToSet;

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
    private final String locationTypeGranularity;
    private final Properties groupsProperties;

    public LocationServiceImpl(AgencyRepository agencyRepository, LocationRepository locationRepository,
            InmateRepository inmateRepository, CaseLoadService caseLoadService,
            @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity,
            @Value("groups.properties") ClassPathResource groupPropertiesFile) throws IOException {
        this.locationRepository = locationRepository;
        this.inmateRepository = inmateRepository;
        this.caseLoadService = caseLoadService;
        this.agencyRepository = agencyRepository;
        this.groupsProperties = groupPropertiesFile == null ? null : PropertiesLoaderUtils.loadProperties(groupPropertiesFile);
        this.locationTypeGranularity = locationTypeGranularity;
    }

    @Override
    public List<Location> getUserLocations(final String username) {
        final List<Location> locations = new ArrayList<>();

        // Step 1 - Get all agencies associated with user
        agencyRepository.findAgenciesForCurrentCaseloadByUsername(username).forEach(
                agency -> {
                    // Start with agency converted to location
                    locations.add(convertToLocation(agency));

                    // Then retrieve all associated internal locations at configured level of granularity.
                    final List<Location> agencyLocations = locationRepository.findLocationsByAgencyAndType(
                            agency.getAgencyId(), locationTypeGranularity, true);

                    agencyLocations.forEach(a -> a.setDescription(LocationProcessor.formatLocation(a.getDescription())));
                    locations.addAll(agencyLocations);
                }
        );

        return locations;
    }

    @Override
    public Page<Location> getLocations(String username, String query, String orderBy, Order order, long offset, long limit) {
        return locationRepository.findLocations(username, query, orderBy, order, offset, limit);
    }

    @Override
    public Page<OffenderBooking> getInmatesFromLocation(long locationId, String username, String query, String orderByField, Order order, long offset, long limit) {
        // validation check?
        locationRepository.findLocation(locationId, username);

        String colSort = StringUtils.isNotBlank(orderByField) ? orderByField : DEFAULT_OFFENDER_SORT;

        Page<OffenderBooking> inmates = inmateRepository.findInmatesByLocation(
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
    public Location getLocation(long locationId) {
        return locationRepository.getLocation(locationId).orElseThrow(EntityNotFoundException.withId(locationId));
    }

    /**
     * Get all cells for the prison/agency then filter them using the named pattern
     * defined in the groups.properties file.
     */
    @Override
    @VerifyAgencyAccess
    public List<Location> getGroup(String agencyId, String name) {

        final String patterns = groupsProperties.getProperty(agencyId + '_' + name);
        if (patterns == null) {
            throw new EntityNotFoundException(
                    "Group '" + name + "' does not exist for agencyId '" + agencyId + "'.");
        }
        final List<Location> cells = locationRepository.findLocationsByAgencyAndType(agencyId, "CELL", false);
        cells.forEach(c -> c.setDescription(LocationProcessor.formatLocation(c.getDescription())));
        final Set<String> patternSet = commaDelimitedListToSet(patterns);
        final List<Location> results = new ArrayList<>();
        for (String patternString : patternSet) {
            final Pattern pattern = Pattern.compile(patternString);
            for (Location cell : cells) {
                if (pattern.matcher(cell.getLocationPrefix()).matches() && !results.contains(cell)) {
                    results.add(cell);
                }
            }
        }
        if (results.isEmpty()) {
            throw ConfigException.withMessage("Group '%s' defines no locations for agencyId '%s'", name, agencyId);
        }
        return results; 
    }

    /**
     * Get all available groups for the prison/agency defined in the groups.properties file.
     */
    @Override
    @VerifyAgencyAccess
    public List<String> getAvailableGroups(String agencyId) {

        final Set<String> keySet = groupsProperties.stringPropertyNames();

        final List<String> results = keySet.stream()//
                .filter(t -> t.startsWith(agencyId))//
                .map(k -> k.substring(agencyId.length() + 1))//
                .sorted()//
                .collect(Collectors.toList());
        return results;
    }

    private String getWorkingCaseLoad(String username) {
        Optional<CaseLoad> workingCaseLoad = caseLoadService.getWorkingCaseLoadForUser(username);

        return workingCaseLoad.isPresent() ? workingCaseLoad.get().getCaseLoadId() : null;
    }

    private Location convertToLocation(Agency agency) {
        return Location.builder()
                .locationId(-1L)
                .agencyId(agency.getAgencyId())
                .locationType(agency.getAgencyType())
                .description(LocationProcessor.formatLocation(agency.getDescription()))
                .locationPrefix(agency.getAgencyId())
                .build();
    }
}
