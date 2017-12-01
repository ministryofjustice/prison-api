package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.LocationService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test cases for {@link LocationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationServiceImplTest {

    @Mock private LocationRepository locationRepository;
    @Mock private AgencyRepository agencyRepository;
    @Mock private Environment env;

    private LocationService locationService;

    @Before
    public void init() {
        locationService = new LocationServiceImpl(agencyRepository, locationRepository, null, null, env, "WING", 2);
    }

    @Test
    public void getUserLocations() throws Exception {
        
        List<Agency> agencies =  Collections.singletonList(Agency.builder().agencyId("LEI").build());
 
        Mockito.when(agencyRepository.findAgenciesByUsername("me")).thenReturn(agencies);
        
        List<Location> locations = new ArrayList<>();
        Location location = createTestLocation();
        locations.add(location);
        Mockito.when(locationRepository.findLocationsByAgencyAndType("LEI","WING",2)).thenReturn(locations);

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
    public void testGetGroupHappy() {

        Mockito.when(locationRepository.getCells("LEI")).thenReturn(Arrays.asList(//
                "cell1", "cell2", "cell3", "cell4"));
        Mockito.when(env.getProperty("LEI_mylist")).thenReturn("cell[13]||cell4");

        final List<String> group = locationService.getGroup("LEI", "mylist");
        
        assertThat(group).asList().containsExactly("cell1", "cell3", "cell4");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetGroupNoName() throws Exception {
        Mockito.when(locationRepository.getCells("LEI")).thenReturn(Arrays.asList(//
                "cell1", "cell2", "cell3", "cell4"));
        Mockito.when(env.getProperty("LEI_mylist")).thenReturn("cell[13]||cell4");

        locationService.getGroup("LEI", "does-not-exist");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetGroupNoAgency() throws Exception {
        Mockito.when(locationRepository.getCells("LEI")).thenReturn(Arrays.asList(//
                "cell1", "cell2", "cell3", "cell4"));
        Mockito.when(env.getProperty("LEI_mylist")).thenReturn("cell[13]||cell4");

        locationService.getGroup("does-not-exist", "mylist");
    }

    @Test(expected = PatternSyntaxException.class)
    public void testGetGroupInvalidPattern() throws Exception {
        Mockito.when(locationRepository.getCells("LEI")).thenReturn(Arrays.asList(//
                "cell1", "cell2", "cell3", "cell4"));
        Mockito.when(env.getProperty("LEI_mylist")).thenReturn("cell[13]||[");

        locationService.getGroup("LEI", "mylist");
    }
}
