package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;

/**
 * BDD step implementations for Locations feature.
 */
public class LocationsSteps extends CommonSteps {
    private static final String API_LOCATIONS = API_PREFIX + "locations";

    private static final String GROUPS_API_URL = API_LOCATIONS + "/groups/{agencyId}";
    private static final String GROUP_API_URL = API_LOCATIONS + "/groups/{agencyId}/{name}";
    
    private Location location;
    private List<Location> locationList;

    private List<String> groupList;

    @Step("Perform locations search without any criteria")
    public void findAll() {
        dispatchQuery(null);
    }

    @Step("Perform location search by location id")
    public void findByLocationId(Long locationId) {
        dispatchQueryForObject("/" + locationId.toString());
    }

    @Step("Verify location type")
    public void verifyLocationType(String type) {
        String locationType = (location == null) ? StringUtils.EMPTY : location.getLocationType();

        assertThat(locationType).isEqualTo(type);
    }

    @Step("Verify location description")
    public void verifyLocationDescription(String description) {
        String locationDesc = (location == null) ? StringUtils.EMPTY : location.getDescription();

        assertThat(locationDesc).isEqualTo(description);
    }

    private void dispatchQuery(String query) {
        init();

        String queryUrl = API_LOCATIONS + StringUtils.trimToEmpty(query);

        ResponseEntity<List<Location>> response = restTemplate.exchange(queryUrl,
                HttpMethod.GET, createEntity(null, addPaginationHeaders()), new ParameterizedTypeReference<List<Location>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        buildResourceData(response);
    }

    private void dispatchQueryForObject(String query) {
        init();

        String queryUrl = API_LOCATIONS + StringUtils.trimToEmpty(query);

        ResponseEntity<Location> response;

        try {
            response = restTemplate.exchange(queryUrl, HttpMethod.GET, createEntity(), Location.class);

            location = response.getBody();

            List<?> resources = Collections.singletonList(location);

            setResourceMetaData(resources);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchGroupCall(String url, String agencyId, String name) {
        init();
        try {
            ResponseEntity<List<Location>> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null, null),
                    new ParameterizedTypeReference<List<Location>>() {}, agencyId, name);
            locationList = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchGroupsCall(String url, String agencyId) {
        init();
        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null, null),
                    new ParameterizedTypeReference<List<String>>() {}, agencyId);
            groupList = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();

        location = null;
        locationList = null;
        groupList = null;
    }

    public void findList(String agencyId, String name) {
        dispatchGroupCall(GROUP_API_URL, agencyId, name);
    }

    public void verifyLocationList(String expectedList) {
        assertThat(locationList).asList().extracting("locationPrefix")
                .containsExactly((Object[]) commaDelimitedListToStringArray(expectedList));
    }

    public void verifyLocationIdList(String expectedList) {
        // Careful here - this does not check order, we are relying on verifyLocationList() for that
        verifyLongValues(locationList, Location::getLocationId, expectedList);
    }

    public void aRequestIsMadeToRetrieveAllGroups(String agencyId) {
        dispatchGroupsCall(GROUPS_API_URL, agencyId);
    }

    public void groupsAre(String expectedList) {
        assertThat(groupList).asList().containsExactly((Object[]) commaDelimitedListToStringArray(expectedList));
    }
}
