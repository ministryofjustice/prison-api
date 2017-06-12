package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.InmateSummaries;
import net.syscon.util.QueryOperator;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking search feature.
 */
public class BookingSearchSteps extends CommonSteps {
    private static final String API_PREFIX = "/api/booking?query=";

    private InmateSummaries inmateSummaries;

    @Step("Perform search using full last name")
    public void fullLastNameSearch(String criteria) {
        String query = buildSimpleQuery("lastName", QueryOperator.EQUAL, criteria);
        ResponseEntity<InmateSummaries> response = restTemplate.exchange(query, HttpMethod.GET, createEntity(), InmateSummaries.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        inmateSummaries = response.getBody();
    }

    @Step("Verify expected number of inmates returned by search")
    public void verifySearchCount(int expectedCount) {
        assertThat(inmateSummaries.getInmatesSummaries().size()).isEqualTo(expectedCount);
    }

    private String buildSimpleQuery(String fieldName, QueryOperator operator, Object criteria) {
        StringJoiner sj = new StringJoiner(":", API_PREFIX, "");

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
}
