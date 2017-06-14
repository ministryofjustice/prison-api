package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateSummaries;
import net.syscon.util.QueryOperator;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking search feature.
 */
public class BookingSearchSteps extends CommonSteps {
    private static final String API_PREFIX = "/api/booking";
    private static final String API_QUERY_PREFIX = "/api/booking?query=";

    private InmateSummaries inmateSummaries;

    @Step("Perform search using full last name")
    public void fullLastNameSearch(String criteria) {
        String query = buildSimpleQuery("lastName", QueryOperator.EQUAL, criteria);
        ResponseEntity<InmateSummaries> response = restTemplate.exchange(query, HttpMethod.GET, createEntity(), InmateSummaries.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        inmateSummaries = response.getBody();
    }

    @Step("Perform search using partial last name")
    public void partialLastNameSearch(String criteria) {
        String query = buildSimpleQuery("lastName", QueryOperator.LIKE, criteria);
        ResponseEntity<InmateSummaries> response = restTemplate.exchange(query, HttpMethod.GET, createEntity(), InmateSummaries.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        inmateSummaries = response.getBody();
    }

    @Step("Perform search without any criteria")
    public void findAll() {
        ResponseEntity<InmateSummaries> response = restTemplate.exchange(API_PREFIX, HttpMethod.GET, createEntity(), InmateSummaries.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        inmateSummaries = response.getBody();
    }

    @Step("Verify expected number of inmates returned by search")
    public void verifySearchCount(int expectedCount) {
        assertThat(inmateSummaries.getInmatesSummaries().size()).isEqualTo(expectedCount);
    }

    @Step("Verify first names of inmates returned by search")
    public void verifyFirstNames(String nameList) {
        // Using List (not Set) to handle duplicate names
        List<String> queriedNames = extractFirstNames();
        List<String> expectedNames = csv2list(nameList);

        verifyIdentical(queriedNames, expectedNames);
    }

    @Step("Verify middle names of inmates returned by search")
    public void verifyMiddleNames(String nameList) {
        // Using List (not Set) to handle duplicate names
        List<String> queriedNames = extractMiddleNames();
        List<String> expectedNames = csv2list(nameList);

        verifyIdentical(queriedNames, expectedNames);
    }

    private String buildSimpleQuery(String fieldName, QueryOperator operator, Object criteria) {
        StringJoiner sj = new StringJoiner(":", API_QUERY_PREFIX, "");

        sj.add(fieldName);
        sj.add(operator.getJsonOperator());
        sj.add(parseCriteria(criteria));

        return sj.toString();
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

    private List<String> csv2list(String commaSeparatedList) {
        List<String> out;

        if (StringUtils.isBlank(commaSeparatedList)) {
            out = Collections.emptyList();
        } else {
            out = Arrays.asList(commaSeparatedList.split("\\s*,\\s*"));
        }

        return out;
    }

    private List<String> extractFirstNames() {
        return inmateSummaries.getInmatesSummaries()
                .stream().map(AssignedInmate::getFirstName).collect(Collectors.toList());
    }

    private List<String> extractMiddleNames() {
        return inmateSummaries.getInmatesSummaries()
                .stream().map(AssignedInmate::getMiddleName).collect(Collectors.toList());
    }

    private void verifyIdentical(List<String> listActual, List<String> listExpected) {
        // Both lists are expected to be provided (i.e. non-null). Empty lists are ok.
        // Sorting and converting back to String so that details of non-matching lists are clearly disclosed
        Collections.sort(listActual);
        Collections.sort(listExpected);

        String actual = String.join(",", listActual);
        String expected = String.join(",", listExpected);

        assertThat(actual).isEqualTo(expected);
    }
}
