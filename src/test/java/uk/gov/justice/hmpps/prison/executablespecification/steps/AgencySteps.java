package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Agencies service.
 */
public class AgencySteps extends CommonSteps {
    private static final String API_REF_PREFIX = API_PREFIX + "agencies/";
    public static final String API_AGENCY_URL = API_REF_PREFIX + "{agencyId}";
    private static final String API_EVENT_LOCATIONS_URL = API_REF_PREFIX + "{agencyId}/eventLocations";
    private static final String API_CASELOAD_URL = API_REF_PREFIX + "caseload/{caseload}";
    private List<Agency> agencies;
    private Agency agency;
    private List<Location> locations;

    private void dispatchPagedListRequest(final String resourcePath, final Long offset, final Long limit, final Object... params) {
        init();

        final HttpEntity<?> httpEntity;

        if (Objects.nonNull(offset) && Objects.nonNull(limit)) {
            applyPagination(offset, limit);
            httpEntity = createEntity(null, addPaginationHeaders());
        } else {
            httpEntity = createEntity();
        }

        try {
            final var response = restTemplate.exchange(
                resourcePath,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<List<Agency>>() {
                    },
                    params);

            agencies = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchListRequest(final String resourcePath, final String agencyId, final String eventType, final Map<String, String> headers) {
        init();

        final var urlModifier = StringUtils.isBlank(eventType) ? "" : "?eventType=" + eventType;
        final var url = resourcePath + urlModifier;

        try {
            final var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(null, headers),
                    new ParameterizedTypeReference<List<Location>>() {
                    },
                    agencyId);

            locations = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchObjectRequest(final String agencyId, final Boolean activeOnly) {
        init();


        final var uriBuilder = UriComponentsBuilder.fromPath(API_AGENCY_URL);

        if (activeOnly != null) {
            uriBuilder.queryParam("activeOnly", activeOnly);
        }

        final var uri = uriBuilder.build(agencyId);

        try {
            final var response = restTemplate.exchange(uri, HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<Agency>() {
                    });

            agency = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchObjectRequestForCaseload(final String caseload) {
        init();
        try {
            final var response = restTemplate.exchange(AgencySteps.API_CASELOAD_URL, HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<Agency>>() {
                    }, caseload);

            agencies = response.getBody();
            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
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
        dispatchPagedListRequest(API_REF_PREFIX, 0L, 1000L);
    }

    @Step("Submit request for any event locations")
    public void getLocationsForAnyEvents(final String agencyId) {
        dispatchListRequest(API_EVENT_LOCATIONS_URL, agencyId, null, null);
    }

    public void verifyAgencyList(final List<Agency> expected) {

        final var expectedIterator = expected.iterator();
        final var actualIterator = agencies.iterator();
        while (expectedIterator.hasNext()) {
            final var expectedThis = expectedIterator.next();
            final var actualThis = actualIterator.next();
            assertThat(actualThis.getAgencyId()).isEqualTo(expectedThis.getAgencyId());
            assertThat(actualThis.getAgencyType()).isEqualTo(expectedThis.getAgencyType());
            assertThat(actualThis.getDescription()).isEqualTo(expectedThis.getDescription());
        }
        assertThat(actualIterator.hasNext()).as("Too many actual events").isFalse();
    }

    @Step("Verify agency property")
    public void verifyField(final String field, final String value) throws ReflectiveOperationException {
        super.verifyField(agency, field, value);
    }

    @Step("Submit request for agency details")
    public void getAgency(final String agencyId, final Boolean activeOnly) {
        dispatchObjectRequest(agencyId, activeOnly);
    }

    public void verifyLocationList(final List<Location> expected) {
        final var expectedIterator = expected.iterator();
        final var actualIterator = locations.iterator();
        while (expectedIterator.hasNext()) {
            final var expectedThis = expectedIterator.next();
            final var actualThis = actualIterator.next();
            assertThat(actualThis.getLocationId()).isEqualTo(expectedThis.getLocationId());
            assertThat(actualThis.getLocationPrefix()).isEqualTo(expectedThis.getLocationPrefix());
            assertThat(actualThis.getDescription()).isEqualTo(expectedThis.getDescription());
            assertThat(actualThis.getUserDescription()).isEqualTo(expectedThis.getUserDescription());
        }
        assertThat(actualIterator.hasNext()).as("Too many actual events").isFalse();
    }

    public void getAgenciesByCaseload(final String caseload) {
        dispatchObjectRequestForCaseload(caseload);
    }



}
