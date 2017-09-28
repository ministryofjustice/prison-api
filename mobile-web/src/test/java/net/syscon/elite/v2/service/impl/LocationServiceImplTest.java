package net.syscon.elite.v2.service.impl;

import net.syscon.elite.persistence.LocationRepository;
import net.syscon.elite.v2.api.model.Location;
import net.syscon.elite.v2.api.support.Order;
import net.syscon.elite.v2.service.LocationService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;

/**
 * Test cases for {@link LocationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationServiceImplTest {
    @Mock
    private LocationRepository locationRepository;

    private LocationService locationService;

    @Before
    public void setUp() throws Exception {
//        locationService = new LocationServiceImpl(locationRepository);
    }

    @Test
    @Ignore
    public void getUserLocations() throws Exception {
        List<Location> locations = new ArrayList<>();
        Location location = createTestLocation();

        locations.add(location);

        Mockito.when(locationRepository.findLocations(eq(null), eq("locationId"), eq(Order.ASC), eq(0), eq(10))).thenReturn(locations);

        List<net.syscon.elite.v2.api.model.Location> returnedLocations = locationService.getUserLocations("");

        assertFalse(returnedLocations.isEmpty());

        net.syscon.elite.v2.api.model.Location returnedLocation = returnedLocations.get(0);

        assertEquals(location.getLocationId().longValue(), returnedLocation.getLocationId().longValue());
        assertEquals(location.getAgencyId(), returnedLocation.getAgencyId());
        assertEquals(location.getLocationType(), returnedLocation.getLocationType());
        assertEquals(location.getDescription(), returnedLocation.getDescription());
    }

    private Location createTestLocation() {
        Location location = new Location();

        location.setLocationId(1L);
        location.setAgencyId("LEI");
        location.setLocationType("WING");
        location.setDescription("LEI-A");

        return location;
    }
}
