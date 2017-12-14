package net.syscon.elite.executablespecification.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * BDD step implementations for Reference Domains service.
 */
public class AgencySteps extends CommonSteps {
    private static final String API_REF_PREFIX = API_PREFIX + "agencies/";
    private static final String API_AGENCY_URL = API_REF_PREFIX + "{agencyId}";
    private static final String API_LOCATIONS_URL = API_REF_PREFIX + "{agencyId}/locations";
    private List<Agency> agencies;
    private Agency agency;
    private List<Location> locations;

    private <T> List<T> dispatchPagedListRequest(String resourcePath, Long offset, Long limit, Object... params) {
        init();
        HttpEntity<?> httpEntity;
        if (Objects.nonNull(offset) && Objects.nonNull(limit)) {
            applyPagination(offset, limit);
            httpEntity = createEntity(null, addPaginationHeaders());
        } else {
            httpEntity = createEntity();
        }
        String url = resourcePath;
        try {
            ResponseEntity<List<T>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<List<T>>() {},
                    params);
            buildResourceData(response);
            return response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private <T> List<T> dispatchListRequest(String resourcePath, String agencyId, String eventType) {
        init();

        String urlModifier = "?eventType=" + eventType;
        HttpEntity<?> httpEntity = createEntity();
        String url = resourcePath + urlModifier;
        try {
            ResponseEntity<List<T>> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                    new ParameterizedTypeReference<List<T>>() {
                    }, agencyId);
            buildResourceData(response);
            return response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private void dispatchObjectRequest(String resourcePath, String agencyId) {
        init();

        String urlModifier = "";

        String url = resourcePath + urlModifier;

        try {
            ResponseEntity<Agency> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<Agency>() {
                    }, agencyId);

            agency = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();
        agency = null;
        agencies = null;
        locations = null;
    }

    @Step("Submit request for all agencies")
    public void getAllAgencies() {
        agencies = dispatchPagedListRequest(API_REF_PREFIX, 0L, 1000L);
    }

    @Step("Submit request for agency locations")
    public void getLocations(String agencyId, String eventType) {
        locations = dispatchListRequest(API_LOCATIONS_URL, agencyId, eventType);
    }

    public void verifyAgencyList(List<Agency> expected) {

        final Iterator<Agency> expectedIterator = expected.iterator();
        final Iterator<Agency> actualIterator = agencies.iterator();
        while (expectedIterator.hasNext()) {
            final Agency expectedThis = expectedIterator.next();
            final Agency actualThis = actualIterator.next();
            assertEquals(expectedThis.getAgencyId(), actualThis.getAgencyId());
            assertEquals(expectedThis.getAgencyType(), actualThis.getAgencyType());
            assertEquals(expectedThis.getDescription(), actualThis.getDescription());
        }
        assertFalse("Too many actual events", actualIterator.hasNext());
    }

    @Step("Verify agency property")
    public void verifyField(String field, String value) throws ReflectiveOperationException {
        super.verifyField(agency, field, value);
    }

    @Step("Submit request for agency details")
    public void getAgency(String agencyId) {
        dispatchObjectRequest(API_AGENCY_URL, agencyId);
    }

    public void verifyLocationList(List<Location> expected) {
        final Iterator<Location> expectedIterator = expected.iterator();
        final Iterator<Location> actualIterator = locations.iterator();
        while (expectedIterator.hasNext()) {
            final Location expectedThis = expectedIterator.next();
            final Location actualThis = actualIterator.next();
            assertEquals(expectedThis.getLocationId(), actualThis.getLocationId());
            assertEquals(expectedThis.getLocationPrefix(), actualThis.getLocationPrefix());
            assertEquals(expectedThis.getDescription(), actualThis.getDescription());
        }
        assertFalse("Too many actual events", actualIterator.hasNext());
    }

    public void verifySuccess() {
        assertNotNull(agencies);
    }
}
