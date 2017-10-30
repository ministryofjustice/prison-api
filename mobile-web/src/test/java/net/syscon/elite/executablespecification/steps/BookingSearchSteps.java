package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.util.QueryOperator;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking search feature.
 */
public class BookingSearchSteps extends CommonSteps {
    private static final String API_QUERY_PREFIX = API_PREFIX + "bookings?query=";

    private List<OffenderBooking> inmateSummaries;

    @Step("Perform search using full last name")
    public void fullLastNameSearch(String criteria) {
        String query = buildSimpleQuery("lastName", criteria);

        dispatchQuery(query);
    }

    @Step("Perform search using partial last name")
    public void partialLastNameSearch(String criteria) {
        String query = buildSimpleQuery("lastName", criteria);

        dispatchQuery(query);
    }

    @Step("Perform search using full first name")
    public void fullFirstNameSearch(String criteria) {
        String query = buildSimpleQuery("firstName", criteria);

        dispatchQuery(query);
    }

    @Step("Perform search using partial first name")
    public void partialFirstNameSearch(String criteria) {
        String query = buildSimpleQuery("firstName", criteria);

        dispatchQuery(query);
    }

    @Step("Perform search using first name and last name")
    public void firstNameAndLastNameSearch(String firstName, String lastName) {
        String firstNameQuery = buildSimpleQuery("firstName", firstName);
        String lastNameQuery = buildSimpleQuery("lastName", lastName);

        String query = buildAndQuery(firstNameQuery, lastNameQuery);

        dispatchQuery(query);
    }

    @Step("Perform search using first name or last name")
    public void firstNameOrLastNameSearch(String firstName, String lastName) {
        String firstNameQuery = buildSimpleQuery("firstName", firstName);
        String lastNameQuery = buildSimpleQuery("lastName", lastName);

        String query = buildOrQuery(firstNameQuery, lastNameQuery);

        dispatchQuery(query);
    }

    @Step("Perform booking search without any criteria")
    public void findAll() {
        dispatchQuery(null);
    }

    @Step("Verify first names of inmates returned by search")
    public void verifyFirstNames(String nameList) {
        verifyPropertyValues(inmateSummaries, OffenderBooking::getFirstName, nameList);
    }

    @Step("Verify middle names of inmates returned by search")
    public void verifyMiddleNames(String nameList) {
        verifyPropertyValues(inmateSummaries, OffenderBooking::getMiddleName, nameList);
    }

    @Step("Verify last names of inmates returned by search")
    public void verifyLastNames(String nameList) {
        verifyPropertyValues(inmateSummaries, OffenderBooking::getLastName, nameList);
    }

    @Step("Verify living unit descriptions returned by search")
    public void verifyLivingUnits(String livingUnitList) {
        verifyPropertyValues(inmateSummaries, OffenderBooking::getAssignedLivingUnitDesc, livingUnitList);
    }

    @Step("Verify image ids returned by search")
    public void verifyImageIds(String imageIds) {
        verifyLongValues(inmateSummaries, OffenderBooking::getFacialImageId, imageIds);
    }

    @Step("Verify dobs returned by search")
    public void verifyDobs(String dobs) {
        verifyLocalDateValues(inmateSummaries, OffenderBooking::getDateOfBirth, dobs);
    }

    private void dispatchQuery(String query) {
        init();

        String queryUrl = API_QUERY_PREFIX + StringUtils.trimToEmpty(query);

        ResponseEntity<List<OffenderBooking>> response = restTemplate.exchange(queryUrl,
                HttpMethod.GET, createEntity(null, addPaginationHeaders()), new ParameterizedTypeReference<List<OffenderBooking>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        inmateSummaries = response.getBody();
        buildResourceData(response);
    }

    private String buildSimpleQuery(String fieldName, Object criteria) {
        StringJoiner sj = new StringJoiner(":");

        sj.add(fieldName);
        sj.add(deriveOperator(criteria).getJsonOperator());
        sj.add(parseCriteria(criteria));

        return sj.toString();
    }

    private String buildAndQuery(String... terms) {
        return Stream.of(terms).collect(Collectors.joining(",and:"));
    }

    private String buildOrQuery(String... terms) {
        return Stream.of(terms).collect(Collectors.joining(",or:"));
    }

    private QueryOperator deriveOperator(Object criteria) {
        QueryOperator operator;

        if (criteria instanceof String) {
            operator = (((String) criteria).contains("%") ? QueryOperator.LIKE : QueryOperator.EQUAL);
        } else {
            operator = QueryOperator.EQUAL;
        }

        return operator;
    }

    private String parseCriteria(Object criteria) {
        String formatter;

        if (criteria instanceof String) {
            formatter = "'%s'";
        } else {
            formatter = "%s";
        }

        return String.format(formatter, criteria);
    }

    protected void init() {
        super.init();

        inmateSummaries = null;
    }
}
