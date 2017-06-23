package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.Alias;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingAliasSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "booking/{bookingId}/aliases";

    private List<Alias> aliases;

    @Step("Retrieve aliases for offender booking")
    public void getAliasesForBooking(Long bookingId) {
        dispatchRequest(bookingId, null);
    }

    @Step("Verify returned offender alias first names")
    public void verifyAliasFirstNames(String firstNames) {
        List<String> queriedNames = extractFirstNames();
        List<String> expectedNames = csv2list(firstNames);

        verifyIdentical(queriedNames, expectedNames);
    }

    @Step("Verify returned offender alias last names")
    public void verifyAliasLastNames(String lastNames) {
        List<String> queriedNames = extractLastNames();
        List<String> expectedNames = csv2list(lastNames);

        verifyIdentical(queriedNames, expectedNames);
    }

    @Step("Verify returned offender alias ethnicities")
    public void verifyAliasEthnicities(String ethnicities) {
        List<String> queriedEthnicities = extractEthnicities();
        List<String> expectedEthnicities = csv2list(ethnicities);

        verifyIdentical(queriedEthnicities, expectedEthnicities);
    }

    private void dispatchRequest(Long bookingId, String query) {
        init();

        String requestUrl = API_REQUEST_BASE_URL + StringUtils.trimToEmpty(query);

        ResponseEntity<List<Alias>> response = restTemplate.exchange(requestUrl, HttpMethod.GET, createEntity(),
                new ParameterizedTypeReference<List<Alias>>() {}, bookingId.toString());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        aliases = response.getBody();

        setResourceMetaData(aliases, null);
    }

    private List<String> extractFirstNames() {
        return aliases
                .stream()
                .map(Alias::getFirstName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private List<String> extractLastNames() {
        return aliases
                .stream()
                .map(Alias::getLastName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private List<String> extractEthnicities() {
        return aliases
                .stream()
                .map(Alias::getEthinicity)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private void init() {
        aliases = null;
    }
}
