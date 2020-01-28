package net.syscon.elite.service.whereabouts;

import net.syscon.elite.api.model.LocationGroup;
import net.syscon.elite.service.LocationGroupService;
import net.syscon.elite.service.whereabouts.LocationGroupServiceSelector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class LocationGroupServiceSelectorTest {

    private static final LocationGroup LG1 = LocationGroup.builder().key("A").name("A").build();

    @Mock
    private LocationGroupService defaultService;

    @Mock
    private LocationGroupService overrideService;

    private LocationGroupService service;

    @Before
    public void setUp() {
        service = new LocationGroupServiceSelector(defaultService, overrideService);
    }

    @Test
    public void getLocationGroupsCallsDefaultWhenNoOverride() {
        when(defaultService.getLocationGroups("LEI")).thenReturn(List.of(LG1));
        assertThat(service.getLocationGroups("LEI")).contains(LG1);
        verify(overrideService).getLocationGroups("LEI");
    }

    @Test
    public void getLocationGroupsDoesNotCallDefaultWhenOverridden() {
        when(overrideService.getLocationGroups("LEI")).thenReturn(List.of(LG1));
        assertThat(service.getLocationGroups("LEI")).contains(LG1);
        verify(overrideService).getLocationGroups("LEI");
        verifyNoMoreInteractions(defaultService);
    }

    @Test
    public void getLocationGroupsForAgencyDelegatesToGetLocationGroups() {
        when(defaultService.getLocationGroups("LEI")).thenReturn(List.of(LG1));
        assertThat(service.getLocationGroupsForAgency("LEI")).contains(LG1);
        verify(overrideService).getLocationGroups("LEI");
    }

    @Test
    public void locationGroupsFiltersCallsDefaultWhenNoOverride() {
        service.locationGroupFilter("LEI", "Z");
        verify(defaultService).locationGroupFilter("LEI", "Z");
    }

    @Test
    public void locationGroupsFiltersCallsOverrideOnlyIfOverridden() {
        when(overrideService.getLocationGroups("LEI")).thenReturn(List.of(LG1));
        service.locationGroupFilter("LEI", "Z");
        verify(overrideService).locationGroupFilter("LEI", "Z");
        verifyNoMoreInteractions(defaultService);
    }
}
