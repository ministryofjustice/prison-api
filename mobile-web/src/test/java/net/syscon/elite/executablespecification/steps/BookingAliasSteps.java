package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Alias;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingAliasSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "bookings/{bookingId}/aliases";

    private List<Alias> aliases;

    @Step("Retrieve aliases for offender booking")
    public void getAliasesForBooking(Long bookingId) {
        dispatchRequest(bookingId, null);
    }

    @Step("Verify returned offender alias first names")
    public void verifyAliasFirstNames(String firstNames) {
        verifyPropertyValues(aliases, Alias::getFirstName, firstNames);
    }

    @Step("Verify returned offender alias last names")
    public void verifyAliasLastNames(String lastNames) {
        verifyPropertyValues(aliases, Alias::getLastName, lastNames);
    }

    @Step("Verify returned offender alias ethnicities")
    public void verifyAliasEthnicities(String ethnicities) {
        verifyPropertyValues(aliases, Alias::getEthinicity, ethnicities);
    }

    private void dispatchRequest(Long bookingId, String query) {
        init();

        String requestUrl = API_REQUEST_BASE_URL + StringUtils.trimToEmpty(query);
        ResponseEntity<List<Alias>> response = restTemplate.exchange(requestUrl, HttpMethod.GET, createEntity(),
                new ParameterizedTypeReference<List<Alias>>() {}, bookingId.toString());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        aliases = response.getBody();
        buildResourceData(response);
    }

    protected void init() {
        super.init();

        aliases = null;
    }
}
