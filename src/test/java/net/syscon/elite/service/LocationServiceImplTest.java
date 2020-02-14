package net.syscon.elite.service;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link LocationService}.
 */
@ExtendWith(MockitoExtension.class)
public class LocationServiceImplTest {

    private static final Function<String, Predicate<Location>> filterFactory = (String s) -> (Location l) -> s.equals(l.getLocationPrefix());

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private LocationGroupService locationGroupService;
    @Mock
    private CaseLoadService caseLoadService;

    private LocationService locationService;
    private final Location cell1 = Location.builder().locationPrefix("cell1").build();
    private final Location cell2 = Location.builder().locationPrefix("cell2").build();
    private final Location cell3 = Location.builder().locationPrefix("cell3").build();
    private final Location cell4 = Location.builder().locationPrefix("cell4").build();

    @BeforeEach
    public void init() {
        locationService = new LocationService(agencyRepository, locationRepository, null, caseLoadService, locationGroupService, "WING");
    }

    @Test
    public void getUserLocations() {

        final var agencies = Collections.singletonList(Agency.builder().agencyId("LEI").build());

        when(agencyRepository.findAgenciesForCurrentCaseloadByUsername("me")).thenReturn(agencies);

        final List<Location> locations = new ArrayList<>();
        final var location = createTestLocation();
        locations.add(location);
        when(locationRepository.findLocationsByAgencyAndType("LEI", "WING", true)).thenReturn(locations);
        when(caseLoadService.getWorkingCaseLoadForUser("me")).thenReturn(Optional.of(CaseLoad.builder().caseLoadId("LEI").type("INST").build()));
        final var returnedLocations = locationService.getUserLocations("me");

        assertFalse(returnedLocations.isEmpty());
        assertThat(returnedLocations).hasSize(2);

        final var returnedLocation = returnedLocations.get(1);
        assertEquals(location.getLocationId().longValue(), returnedLocation.getLocationId().longValue());
        assertEquals(location.getAgencyId(), returnedLocation.getAgencyId());
        assertEquals(location.getLocationType(), returnedLocation.getLocationType());
        assertEquals(location.getDescription(), returnedLocation.getDescription());
    }

    @Test
    public void getUserLocationsWithCentralOnly() {

        when(caseLoadService.getWorkingCaseLoadForUser("admin")).thenReturn(Optional.of(CaseLoad.builder().caseLoadId("CADM_I").type("ADMIN").build()));
        final var returnedLocations = locationService.getUserLocations("admin");

        assertThat(returnedLocations).isEmpty();
    }

    @Test
    public void getUserLocationsWithNoCaseload() {

        when(caseLoadService.getWorkingCaseLoadForUser("noone")).thenReturn(Optional.empty());
        final var returnedLocations = locationService.getUserLocations("noone");

        assertThat(returnedLocations).isEmpty();
    }

    private static Location createTestLocation() {
        final var location = new Location();

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

        when(locationGroupService.locationGroupFilter("LEI", "mylist"))
                .thenReturn(Stream.of("cell4", "cell1", "cell3").map(filterFactory).reduce(Predicate::or).get());

        final var group = locationService.getCellLocationsForGroup("LEI", "mylist");

        // Note that the result order no longer matters.
        assertThat(group).asList().containsExactlyInAnyOrder(cell4, cell1, cell3);
    }

    @Test
    public void testLocationGroupFilterThrowsEntityNotFoundException() {

        when(locationGroupService.locationGroupFilter("LEI", "does-not-exist")).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> locationService.getCellLocationsForGroup("LEI", "does-not-exist")).isInstanceOf(EntityNotFoundException.class);
    }


    @Test
    public void testLocationGroupFilterThrowsPatternSyntaxException() {

        when(locationGroupService.locationGroupFilter("LEI", "mylist")).thenThrow(PatternSyntaxException.class);

        assertThatThrownBy(() -> locationService.getCellLocationsForGroup("LEI", "mylist")).isInstanceOf(PatternSyntaxException.class);
    }

    @Test
    public void testGetGroupNoCells() {
        when(locationRepository.findLocationsByAgencyAndType("LEI", "CELL", false))
                .thenReturn(Arrays.asList(cell1, cell2, cell3, cell4));

        when(locationGroupService.locationGroupFilter("LEI", "mylist")).thenReturn(l -> false);

        assertThatThrownBy(() -> locationService.getCellLocationsForGroup("LEI", "mylist")).isInstanceOf(ConfigException.class);
    }
}
