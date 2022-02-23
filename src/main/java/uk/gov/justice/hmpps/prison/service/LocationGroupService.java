package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.LocationGroup;
import uk.gov.justice.hmpps.prison.repository.LocationRepository;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collector;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
public class LocationGroupService {

    private static final Comparator<LocationGroup> LOCATION_GROUP_COMPARATOR = Comparator.comparing(LocationGroup::getName);
    private final LocationRepository locationRepository;

    LocationGroupService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<LocationGroup> getLocationGroupsForAgency(final String agencyId) {
        return getLocationGroups(agencyId);
    }

    public List<LocationGroup> getLocationGroups(String agencyId) {
        val locations = locationRepository.getLocationGroupData(agencyId);
        val locationIds = locations.stream().map(Location::getLocationId).collect(toSet());
        val subLocations = locationRepository.getSubLocationGroupData(locationIds);
        return toLocationGroups(locations, subLocations);
    }

    private static List<LocationGroup> toLocationGroups(List<Location> locations, List<Location> subLocations) {
        val subGroupMap = toSubGroupMap(subLocations);

        return locations
                .stream()
                .map(location -> toLocationGroup(location, subGroupMap))
                .sorted(LOCATION_GROUP_COMPARATOR)
                .collect(toList());
    }

    private static Map<Long, List<LocationGroup>> toSubGroupMap(List<Location> locations) {
        final Collector<Location, ?, List<LocationGroup>> mapping = mapping(LocationGroupService::toLocationGroup, toList());
        return locations.stream().collect(groupingBy(Location::getParentLocationId, mapping));
    }

    private static LocationGroup toLocationGroup(Location location, Map<Long, List<LocationGroup>> subGroupMap) {
        return LocationGroup
                .builder()
                .key(location.getInternalLocationCode())
                .name(getName(location))
                .children(subGroup(location, subGroupMap))
                .build();
    }

    private static List<LocationGroup> subGroup(Location location, Map<Long, List<LocationGroup>> subGroupMap) {
        val group = subGroupMap.getOrDefault(location.getLocationId(), List.of());
        return group.size() == 1 ? List.of() : sort(group);
    }

    private static List<LocationGroup> sort(List<LocationGroup> group) {
        val result = new ArrayList<>(group);
        result.sort(LOCATION_GROUP_COMPARATOR);
        return result;
    }

    private static LocationGroup toLocationGroup(Location location) {
        return LocationGroup
                .builder()
                .key(location.getInternalLocationCode())
                .name(getName(location))
                .build();
    }

    private static String getName(Location location) {
        return LocationProcessor.formatLocation(
                location.getUserDescription() == null ? location.getInternalLocationCode() : location.getUserDescription()
        );
    }

    public Predicate<Location> locationGroupFilter(String agencyId, String groupName) {
        val prefixToMatch = agencyId + '-' + groupName.replace('_', '-') + '-';
        return (Location location) -> location.getLocationPrefix().startsWith(prefixToMatch);
    }
}
