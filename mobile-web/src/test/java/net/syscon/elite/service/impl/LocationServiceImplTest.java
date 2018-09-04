package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.service.ConfigException;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.LocationGroupService;
import net.syscon.elite.service.LocationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link LocationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationServiceImplTest {

    private static Function<String, Predicate<Location>> filterFactory = (String s) -> (Location l) -> s.equals(l.getLocationPrefix());

    @Mock private LocationRepository locationRepository;
    @Mock private AgencyRepository agencyRepository;
    @Mock private LocationGroupService locationGroupService;

    private LocationService locationService;
    private Location cell1 = Location.builder().locationPrefix("cell1").build();
    private Location cell2 = Location.builder().locationPrefix("cell2").build();
    private Location cell3 = Location.builder().locationPrefix("cell3").build();
    private Location cell4 = Location.builder().locationPrefix("cell4").build();

    @Before
    public void init() throws IOException {
        locationService = new LocationServiceImpl(agencyRepository, locationRepository, null, null, locationGroupService, "WING");
    }

    @Test
    public void getUserLocations() {
        
        List<Agency> agencies =  Collections.singletonList(Agency.builder().agencyId("LEI").build());
 
        when(agencyRepository.findAgenciesForCurrentCaseloadByUsername("me")).thenReturn(agencies);
        
        List<Location> locations = new ArrayList<>();
        Location location = createTestLocation();
        locations.add(location);
        when(locationRepository.findLocationsByAgencyAndType("LEI","WING", true)).thenReturn(locations);

        List<Location> returnedLocations = locationService.getUserLocations("me");

        assertFalse(returnedLocations.isEmpty());
        Location returnedLocation = returnedLocations.get(1);
        assertEquals(location.getLocationId().longValue(), returnedLocation.getLocationId().longValue());
        assertEquals(location.getAgencyId(), returnedLocation.getAgencyId());
        assertEquals(location.getLocationType(), returnedLocation.getLocationType());
        assertEquals(location.getDescription(), returnedLocation.getDescription());
    }

    private static Location createTestLocation() {
        Location location = new Location();

        location.setLocationId(1L);
        location.setAgencyId("LEI");
        location.setLocationType("WING");
        location.setDescription("LEI-A");

        return location;
    }

    @Test
    public void testGetCellLocationsForGroup() {

        when(locationRepository.findLocationsByAgencyAndType("LEI", "CELL", false))
            .thenReturn(Arrays.asList(cell1, cell2, cell3, cell4));

        when(locationGroupService.locationGroupFilters("LEI", "mylist"))
                .thenReturn(Stream.of("cell4", "cell1", "cell3").map(filterFactory).collect(Collectors.toList()));

//                .thenReturn(Arrays.asList(l -> setOf("cell4", "cell1", "cell3").contains(l.getLocationPrefix())));

        final List<Location> group = locationService.getCellLocationsForGroup("LEI", "mylist");

        // Note that the locationGroupFilter ordering imposes an ordering on the results
        assertThat(group).asList().containsExactly(cell4, cell1, cell3);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testLocationGroupFilterThrowsEntityNotFoundException() {

        when(locationGroupService.locationGroupFilters("LEI", "does-not-exist")).thenThrow(EntityNotFoundException.class);

        locationService.getCellLocationsForGroup("LEI", "does-not-exist");
    }


    @Test(expected = PatternSyntaxException.class)
    public void testLocationGroupFilterThrowsPatternSyntaxException() throws Exception {
//        when(locationRepository.findLocationsByAgencyAndType("LEI", "CELL", false))
//                .thenReturn(Arrays.asList(cell1, cell2, cell3, cell4));

        when(locationGroupService.locationGroupFilters("LEI", "mylist")).thenThrow(PatternSyntaxException.class);

        locationService.getCellLocationsForGroup("LEI", "mylist");
    }

    @Test(expected=ConfigException.class)
    public void testGetGroupNoCells() {
        when(locationRepository.findLocationsByAgencyAndType("LEI", "CELL", false))
            .thenReturn(Arrays.asList( cell1, cell2, cell3, cell4));

        when(locationGroupService.locationGroupFilters("LEI", "mylist")).thenReturn(Collections.singletonList(l -> false));
//        groupsProperties.setProperty("LEI_mylist", "");

        locationService.getCellLocationsForGroup("LEI", "mylist");
    }

    private Set<String> setOf(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
