package net.syscon.elite.service.support;

import net.syscon.elite.api.model.Location;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationProcessorTest {
    private static final String TEST_AGENCY_ID = "LEI";
    private static final Long TEST_LOCATION_ID = 1L;
    private static final String TEST_LOCATION_TYPE = "INST";
    private static final String TEST_LOCATION_DESCRIPTION = "LEI-A";
    private static final String TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED = "A";
    private static final String TEST_LOCATION_USER_DESCRIPTION = "Block A";

    private static final Long TEST_SECOND_LOCATION_ID = 2L;
    private static final String TEST_SECOND_LOCATION_DESCRIPTION = "LEI-B";
    private static final String TEST_SECOND_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED = "B";
    private static final String TEST_SECOND_LOCATION_USER_DESCRIPTION = "Block B";

    @Test
    public void stripAgencyId() {
        String newDescription = LocationProcessor.stripAgencyId(TEST_LOCATION_DESCRIPTION, TEST_AGENCY_ID);

        assertThat(newDescription).isEqualTo(TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED);

        newDescription = LocationProcessor.stripAgencyId(TEST_LOCATION_USER_DESCRIPTION, TEST_AGENCY_ID);

        assertThat(newDescription).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);

        newDescription = LocationProcessor.stripAgencyId(null, TEST_AGENCY_ID);

        assertThat(newDescription).isNull();

        newDescription = LocationProcessor.stripAgencyId(TEST_LOCATION_DESCRIPTION, null);

        assertThat(newDescription).isEqualTo(TEST_LOCATION_DESCRIPTION);
    }

    @Test
    public void processLocationUserDescNotPreferred() {
        Location testLocation = buildTestLocation(TEST_LOCATION_USER_DESCRIPTION);

        Location processedLocation = LocationProcessor.processLocation(testLocation);

        assertThat(processedLocation.getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocation.getLocationId()).isEqualTo(TEST_LOCATION_ID);
        assertThat(processedLocation.getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocation.getLocationPrefix()).isEqualTo(TEST_LOCATION_DESCRIPTION);
        assertThat(processedLocation.getDescription()).isEqualTo(TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED);
        assertThat(processedLocation.getUserDescription()).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);
    }

    @Test
    public void processLocationUserDescPreferred() {
        Location testLocation = buildTestLocation(TEST_LOCATION_USER_DESCRIPTION);

        Location processedLocation = LocationProcessor.processLocation(testLocation, true);

        assertThat(processedLocation.getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocation.getLocationId()).isEqualTo(TEST_LOCATION_ID);
        assertThat(processedLocation.getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocation.getLocationPrefix()).isEqualTo(TEST_LOCATION_DESCRIPTION);
        assertThat(processedLocation.getDescription()).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);
        assertThat(processedLocation.getUserDescription()).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);
    }

    @Test
    public void processLocationUserDescPreferredButNull() {
        Location testLocation = buildTestLocation(null);

        Location processedLocation = LocationProcessor.processLocation(testLocation, true);

        assertThat(processedLocation.getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocation.getLocationId()).isEqualTo(TEST_LOCATION_ID);
        assertThat(processedLocation.getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocation.getLocationPrefix()).isEqualTo(TEST_LOCATION_DESCRIPTION);
        assertThat(processedLocation.getDescription()).isEqualTo(TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED);
        assertThat(processedLocation.getUserDescription()).isNull();
    }

    @Test
    public void processLocationsUserDescNotPreferred() {
        List<Location> testLocations = buildTestLocationList(TEST_LOCATION_USER_DESCRIPTION, TEST_SECOND_LOCATION_USER_DESCRIPTION);

        List<Location> processedLocations = LocationProcessor.processLocations(testLocations);

        assertThat(processedLocations.size()).isEqualTo(testLocations.size());

        assertThat(processedLocations.get(0).getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocations.get(0).getLocationId()).isEqualTo(TEST_LOCATION_ID);
        assertThat(processedLocations.get(0).getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocations.get(0).getLocationPrefix()).isEqualTo(TEST_LOCATION_DESCRIPTION);
        assertThat(processedLocations.get(0).getDescription()).isEqualTo(TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED);
        assertThat(processedLocations.get(0).getUserDescription()).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);

        assertThat(processedLocations.get(1).getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocations.get(1).getLocationId()).isEqualTo(TEST_SECOND_LOCATION_ID);
        assertThat(processedLocations.get(1).getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocations.get(1).getLocationPrefix()).isEqualTo(TEST_SECOND_LOCATION_DESCRIPTION);
        assertThat(processedLocations.get(1).getDescription()).isEqualTo(TEST_SECOND_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED);
        assertThat(processedLocations.get(1).getUserDescription()).isEqualTo(TEST_SECOND_LOCATION_USER_DESCRIPTION);
    }

    @Test
    public void processLocationsUserDescPreferred() {
        List<Location> testLocations = buildTestLocationList(TEST_LOCATION_USER_DESCRIPTION, TEST_SECOND_LOCATION_USER_DESCRIPTION);

        List<Location> processedLocations = LocationProcessor.processLocations(testLocations, true);

        assertThat(processedLocations.size()).isEqualTo(testLocations.size());

        assertThat(processedLocations.get(0).getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocations.get(0).getLocationId()).isEqualTo(TEST_LOCATION_ID);
        assertThat(processedLocations.get(0).getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocations.get(0).getLocationPrefix()).isEqualTo(TEST_LOCATION_DESCRIPTION);
        assertThat(processedLocations.get(0).getDescription()).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);
        assertThat(processedLocations.get(0).getUserDescription()).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);

        assertThat(processedLocations.get(1).getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocations.get(1).getLocationId()).isEqualTo(TEST_SECOND_LOCATION_ID);
        assertThat(processedLocations.get(1).getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocations.get(1).getLocationPrefix()).isEqualTo(TEST_SECOND_LOCATION_DESCRIPTION);
        assertThat(processedLocations.get(1).getDescription()).isEqualTo(TEST_SECOND_LOCATION_USER_DESCRIPTION);
        assertThat(processedLocations.get(1).getUserDescription()).isEqualTo(TEST_SECOND_LOCATION_USER_DESCRIPTION);
    }

    @Test
    public void processLocationsUserDescPreferredButNull() {
        List<Location> testLocations = buildTestLocationList(null, null);

        List<Location> processedLocations = LocationProcessor.processLocations(testLocations, true);

        assertThat(processedLocations.size()).isEqualTo(testLocations.size());

        assertThat(processedLocations.get(0).getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocations.get(0).getLocationId()).isEqualTo(TEST_LOCATION_ID);
        assertThat(processedLocations.get(0).getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocations.get(0).getLocationPrefix()).isEqualTo(TEST_LOCATION_DESCRIPTION);
        assertThat(processedLocations.get(0).getDescription()).isEqualTo(TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED);
        assertThat(processedLocations.get(0).getUserDescription()).isNull();

        assertThat(processedLocations.get(1).getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocations.get(1).getLocationId()).isEqualTo(TEST_SECOND_LOCATION_ID);
        assertThat(processedLocations.get(1).getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocations.get(1).getLocationPrefix()).isEqualTo(TEST_SECOND_LOCATION_DESCRIPTION);
        assertThat(processedLocations.get(1).getDescription()).isEqualTo(TEST_SECOND_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED);
        assertThat(processedLocations.get(1).getUserDescription()).isNull();
    }

    private Location buildTestLocation(String userDescription) {
        return Location.builder()
                .agencyId(TEST_AGENCY_ID)
                .locationId(TEST_LOCATION_ID)
                .locationType(TEST_LOCATION_TYPE)
                .description(TEST_LOCATION_DESCRIPTION)
                .userDescription(userDescription)
                .build();
    }

    private List<Location> buildTestLocationList(String userDescription1, String userDescription2) {
        List<Location> locations = new ArrayList<>();

        locations.add(Location.builder()
                .agencyId(TEST_AGENCY_ID)
                .locationId(TEST_LOCATION_ID)
                .locationType(TEST_LOCATION_TYPE)
                .description(TEST_LOCATION_DESCRIPTION)
                .userDescription(userDescription1)
                .build());

        locations.add(Location.builder()
                .agencyId(TEST_AGENCY_ID)
                .locationId(TEST_SECOND_LOCATION_ID)
                .locationType(TEST_LOCATION_TYPE)
                .description(TEST_SECOND_LOCATION_DESCRIPTION)
                .userDescription(userDescription2)
                .build());

        return locations;
    }
}
