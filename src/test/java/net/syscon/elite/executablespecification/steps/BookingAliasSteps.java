package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Alias;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingAliasSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "bookings/{bookingId}/aliases";

    private List<Alias> aliases;

    @Step("Retrieve aliases for offender booking")
    public void getAliasesForBooking(final Long bookingId) {
        dispatchRequest(bookingId);
    }

    @Step("Verify returned offender alias first names")
    public void verifyAliasFirstNames(final String firstNames) {
        verifyPropertyValues(aliases, Alias::getFirstName, firstNames);
    }

    @Step("Verify returned offender alias last names")
    public void verifyAliasLastNames(final String lastNames) {
        verifyPropertyValues(aliases, Alias::getLastName, lastNames);
    }

    @Step("Verify returned offender alias ethnicities")
    public void verifyAliasEthnicities(final String ethnicities) {
        verifyPropertyValues(aliases, Alias::getEthnicity, ethnicities);
    }

    private void dispatchRequest(final Long bookingId) {
        init();

        try {
            final var response =
                    restTemplate.exchange(
                            API_REQUEST_BASE_URL,
                            HttpMethod.GET, createEntity(),
                            new ParameterizedTypeReference<List<Alias>>() {
                            },
                            bookingId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            aliases = response.getBody();

            buildResourceData(response);
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();

        aliases = null;
    }
}
