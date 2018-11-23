package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.OffenceDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class BookingSentenceSteps extends CommonSteps {
    private static final String BOOKING_MAIN_OFFENCE_API_URL = API_PREFIX + "bookings/{bookingId}/mainOffence";

    private List<OffenceDetail> offenceDetails;

    @Step("Get main offence details for offender")
    public void getMainOffenceDetails(Long bookingId) {
        dispatchRequest(bookingId);
    }

    @Step("Verify offence description for specific offence")
    public void verifyOffenceDescription(int index, String expectedOffenceDescription) {
        validateResourcesIndex(index);
        assertThat(offenceDetails.get(index).getOffenceDescription()).isEqualTo(expectedOffenceDescription);
    }

    private void dispatchRequest(Long bookingId) {
        init();

        ResponseEntity<List<OffenceDetail>> response;

        try {
            response =
                    restTemplate.exchange(
                            BOOKING_MAIN_OFFENCE_API_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<OffenceDetail>>() {},
                            bookingId);

            offenceDetails = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();

        offenceDetails = null;
    }
}
