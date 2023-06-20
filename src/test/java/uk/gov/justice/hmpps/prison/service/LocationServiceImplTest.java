package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.repository.AgencyRepository;
import uk.gov.justice.hmpps.prison.repository.LocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link LocationService}.
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private AgencyInternalLocationRepository agencyInternalLocationRepository;
    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private CaseLoadService caseLoadService;

    private LocationService locationService;

    @BeforeEach
    void init() {
        locationService = new LocationService(agencyRepository, agencyInternalLocationRepository, locationRepository, null, caseLoadService);
    }

    @Test
    void getUserLocations() {

        final var agencies = Collections.singletonList(Agency.builder().agencyId("LEI").build());

        when(agencyRepository.findAgenciesForCurrentCaseloadByUsername("me")).thenReturn(agencies);

        final var location = createTestLocation();
        when(agencyInternalLocationRepository.findByAgencyIdAndActiveAndParentLocationIsNullAndCapacityGreaterThanAndTypeIsNotNull("LEI", true, 0)).thenReturn(List.of(createTestAgencyInternalLocation()));
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
        location.setDescription("A");

        return location;
    }

    private static AgencyInternalLocation createTestAgencyInternalLocation() {
        return AgencyInternalLocation.builder()
            .locationId(1L)
            .agencyId("LEI")
            .locationType("WING")
            .description("LEI-A")
            .build();
    }
}
