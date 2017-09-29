package net.syscon.elite.executableSpecification.steps;

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

/**
 * BDD step implementations for Locations feature.
 */
public class LocationsSteps extends CommonSteps {
    private static final String API_LOCATIONS = API_PREFIX + "locations";

    private Location location;

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
        buildResourceData(response, "locations");
    }

    private void dispatchQueryForObject(String query) {
        init();

        String queryUrl = API_LOCATIONS + StringUtils.trimToEmpty(query);

        ResponseEntity<Location> response;

        try {
            response = restTemplate.exchange(queryUrl, HttpMethod.GET, createEntity(), Location.class);

            HttpStatus httpStatus = response.getStatusCode();

            location = response.getBody();

            List<?> resources = Collections.singletonList(location);

            setResourceMetaData(resources, null);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        location = null;
    }
}
