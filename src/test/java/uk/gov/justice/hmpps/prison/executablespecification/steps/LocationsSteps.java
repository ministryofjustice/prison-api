package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.LocationGroup;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;

/**
 * BDD step implementations for Locations feature.
 */
public class LocationsSteps extends CommonSteps {
    private static final String API_LOCATIONS = API_PREFIX + "locations";
    private static final String GROUPS_API_URL = AgencySteps.API_AGENCY_URL + "/locations/groups";
    private static final String GROUP_API_URL = API_LOCATIONS + "/groups/{agencyId}/{name}";

    private Location location;
    private List<Location> locationList;
    private List<LocationGroup> groupList;
    private List<OffenderSearchSteps.OffenderBookingResponse> bookingList;

    @Step("Perform location search by location id")
    public void findByLocationId(final Long locationId) {
        dispatchQueryForObject("/" + locationId.toString());
    }

    @Step("Verify location type")
    public void verifyLocationType(final String type) {
        final var locationType = (location == null) ? StringUtils.EMPTY : location.getLocationType();

        assertThat(locationType).isEqualTo(type);
    }

    @Step("Verify location description")
    public void verifyLocationDescription(final String description) {
        final var locationDesc = (location == null) ? StringUtils.EMPTY : location.getDescription();

        assertThat(locationDesc).isEqualTo(description);
    }

    private void dispatchQueryForObject(final String query) {
        init();

        final var queryUrl = API_LOCATIONS + StringUtils.trimToEmpty(query);

        final ResponseEntity<Location> response;

        try {
            response = restTemplate.exchange(queryUrl, HttpMethod.GET, createEntity(), Location.class);
            location = response.getBody();
            final List<?> resources = Collections.singletonList(location);
            setResourceMetaData(resources);

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchGroupsCall(final String agencyId) {
        init();
        try {
            final var response = restTemplate.exchange(LocationsSteps.GROUPS_API_URL, HttpMethod.GET, createEntity(null, null),
                    new ParameterizedTypeReference<List<LocationGroup>>() {
                    }, agencyId);
            groupList = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();

        location = null;
        locationList = null;
        groupList = null;
        bookingList = null;
    }

    public void verifyLocationList(final String expectedList) {
        assertThat(locationList).asList().extracting("locationPrefix")
                .containsExactly(commaDelimitedListToStringArray(expectedList));
    }

    public void verifyLocationIdList(final String expectedList) {
        // Careful here - this does not check order, we are relying on verifyLocationList() for that
        verifyLongValues(locationList, Location::getLocationId, expectedList);
    }

    public void retrieveListOfInmates(final String agency) {
        retrieveInmates(API_LOCATIONS + "/description/" + agency + "/inmates");
    }

    public void retrieveListOfInmates(final String agency, final String convictedStatus) {
        retrieveInmates(API_LOCATIONS + "/description/" + agency + "/inmates?convictedStatus=" + convictedStatus);
    }

    private void retrieveInmates(final String queryUrl) {

        init();

        applyPagination(0L, 100L);

        try {
            final var response = restTemplate.exchange(
                    queryUrl,
                    HttpMethod.GET,
                    createEntity(null, addPaginationHeaders()),
                    new ParameterizedTypeReference<List<OffenderSearchSteps.OffenderBookingResponse>>() {
                    });

            assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
            bookingList = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void checkOffenderCount(int offenderCount) {
        assertThat(bookingList).hasSize(offenderCount);
    }

    public void checkOffenderCountByConvictedStatus(int offenderCount, final String convictedStatus) {
        final var filteredList = bookingList
                .stream()
                .filter(offender -> offender.getConvictedStatus() != null)
                .filter(offender -> offender.getConvictedStatus().contentEquals(convictedStatus))
                .collect(Collectors.toList());

        assertThat(filteredList).hasSize(offenderCount);
    }
}
