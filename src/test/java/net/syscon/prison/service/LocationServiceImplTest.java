package net.syscon.prison.service;

import net.syscon.prison.api.model.Agency;
import net.syscon.prison.api.model.CaseLoad;
import net.syscon.prison.api.model.Location;
import net.syscon.prison.repository.AgencyRepository;
import net.syscon.prison.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link LocationService}.
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

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
    void init() {
        locationService = new LocationService(agencyRepository, locationRepository, null, caseLoadService, "WING");
    }

    @Test
    void getUserLocations() {

        final var agencies = Collections.singletonList(Agency.builder().agencyId("LEI").build());

        when(agencyRepository.findAgenciesForCurrentCaseloadByUsername("me")).thenReturn(agencies);

        final List<Location> locations = new ArrayList<>();
        final var location = createTestLocation();
        locations.add(location);
        when(locationRepository.findLocationsByAgencyAndType("LEI", "WING", true)).thenReturn(locations);
        when(caseLoadService.getWorkingCaseLoadForUser("me")).thenReturn(Optional.of(CaseLoad.builder().caseLoadId("LEI").type("INST").build()));
        final var returnedLocations = locationService.getUserLocations("me");

        assertThat(returnedLocations.isEmpty()).isFalse();
        assertThat(returnedLocations).hasSize(2);

        final var returnedLocation = returnedLocations.get(1);
        assertThat(returnedLocation.getLocationId().longValue()).isEqualTo(location.getLocationId().longValue());
        assertThat(returnedLocation.getAgencyId()).isEqualTo(location.getAgencyId());
        assertThat(returnedLocation.getLocationType()).isEqualTo(location.getLocationType());
        assertThat(returnedLocation.getDescription()).isEqualTo(location.getDescription());
    }

    @Test
    void getUserLocationsWithCentralOnly() {

        when(caseLoadService.getWorkingCaseLoadForUser("admin")).thenReturn(Optional.of(CaseLoad.builder().caseLoadId("CADM_I").type("ADMIN").build()));
        final var returnedLocations = locationService.getUserLocations("admin");

        assertThat(returnedLocations).isEmpty();
    }

    @Test
    void getUserLocationsWithNoCaseload() {

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
}
