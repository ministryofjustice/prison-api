package uk.gov.justice.hmpps.prison.service.support;

import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.api.model.Location;

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
        var newDescription = LocationProcessor.stripAgencyId(TEST_LOCATION_DESCRIPTION, TEST_AGENCY_ID);

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
        final var testLocation = buildTestLocation(TEST_LOCATION_USER_DESCRIPTION);

        final var processedLocation = LocationProcessor.processLocation(testLocation);

        assertThat(processedLocation.getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocation.getLocationId()).isEqualTo(TEST_LOCATION_ID);
        assertThat(processedLocation.getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocation.getLocationPrefix()).isEqualTo(TEST_LOCATION_DESCRIPTION);
        assertThat(processedLocation.getDescription()).isEqualTo(TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED);
        assertThat(processedLocation.getUserDescription()).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);
    }

    @Test
    public void processLocationUserDescPreferred() {
        final var testLocation = buildTestLocation(TEST_LOCATION_USER_DESCRIPTION);

        final var processedLocation = LocationProcessor.processLocation(testLocation, true, false);

        assertThat(processedLocation.getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocation.getLocationId()).isEqualTo(TEST_LOCATION_ID);
        assertThat(processedLocation.getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocation.getLocationPrefix()).isEqualTo(TEST_LOCATION_DESCRIPTION);
        assertThat(processedLocation.getDescription()).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);
        assertThat(processedLocation.getUserDescription()).isEqualTo(TEST_LOCATION_USER_DESCRIPTION);
    }

    @Test
    public void processLocationUserDescPreferredButNull() {
        final var testLocation = buildTestLocation(null);

        final var processedLocation = LocationProcessor.processLocation(testLocation, true, false);

        assertThat(processedLocation.getAgencyId()).isEqualTo(TEST_AGENCY_ID);
        assertThat(processedLocation.getLocationId()).isEqualTo(TEST_LOCATION_ID);
        assertThat(processedLocation.getLocationType()).isEqualTo(TEST_LOCATION_TYPE);
        assertThat(processedLocation.getLocationPrefix()).isEqualTo(TEST_LOCATION_DESCRIPTION);
        assertThat(processedLocation.getDescription()).isEqualTo(TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED);
        assertThat(processedLocation.getUserDescription()).isNull();
    }

    @Test
    public void processLocationsUserDescNotPreferred() {
        final var testLocations = buildTestLocationList(TEST_LOCATION_USER_DESCRIPTION, TEST_SECOND_LOCATION_USER_DESCRIPTION);

        final var processedLocations = LocationProcessor.processLocations(testLocations);

        assertThat(processedLocations).hasSize(testLocations.size());

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
        final var testLocations = buildTestLocationList(TEST_LOCATION_USER_DESCRIPTION, TEST_SECOND_LOCATION_USER_DESCRIPTION);

        final var processedLocations = LocationProcessor.processLocations(testLocations, true);

        assertThat(processedLocations).hasSize(testLocations.size());

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
        final var testLocations = buildTestLocationList(null, null);

        final var processedLocations = LocationProcessor.processLocations(testLocations, true);

        assertThat(processedLocations).hasSize(testLocations.size());

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

    private Location buildTestLocation(final String userDescription) {
        return Location.builder()
                .agencyId(TEST_AGENCY_ID)
                .locationId(TEST_LOCATION_ID)
                .locationType(TEST_LOCATION_TYPE)
                .description(TEST_LOCATION_DESCRIPTION)
                .userDescription(userDescription)
                .build();
    }

    private List<Location> buildTestLocationList(final String userDescription1, final String userDescription2) {
        final List<Location> locations = new ArrayList<>();

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
