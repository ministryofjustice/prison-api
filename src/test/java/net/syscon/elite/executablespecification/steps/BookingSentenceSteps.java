package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.OffenceDetail;
import net.syscon.elite.api.model.OffenceHistoryDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingSentenceSteps extends CommonSteps {
    private static final String BOOKING_MAIN_OFFENCE_API_URL = API_PREFIX + "bookings/{bookingId}/mainOffence";
    private static final String BOOKING_OFFENCE_HISTORY_API_URL = API_PREFIX + "bookings/offenderNo/{offenderNo}/offenceHistory";

    private List<OffenceDetail> offenceDetails;
    private List<OffenceHistoryDetail> offenceHistory;

    @Step("Get main offence details for offender")
    public void getMainOffenceDetails(final Long bookingId) {
        dispatchMainOffenceRequest(bookingId);
    }

    @Step("Verify offence description for specific offence")
    public void verifyOffenceDescription(final int index, final String expectedOffenceDescription) {
        validateResourcesIndex(index);
        assertThat(offenceDetails.get(index).getOffenceDescription()).isEqualTo(expectedOffenceDescription);
    }

    @Step("Get offence history for offender")
    public void getOffenceHistory(final String offenderNo) {
        dispatchHistoryRequest(offenderNo);
    }

    @Step("Verify offence history")
    public void verifyOffenceHistory(final int index, final String expectedOffenceDescription) {
        validateResourcesIndex(index);
        assertThat(offenceHistory.get(index).getOffenceDescription()).isEqualTo(expectedOffenceDescription);
    }

    public void verifyHistoryRecordsReturned(final long expectedCount) {
    }

    private void dispatchMainOffenceRequest(final Long bookingId) {
        init();

        final ResponseEntity<List<OffenceDetail>> response;

        try {
            response =
                    restTemplate.exchange(
                            BOOKING_MAIN_OFFENCE_API_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<OffenceDetail>>() {
                            },
                            bookingId);

            offenceDetails = response.getBody();

            buildResourceData(response);
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchHistoryRequest(final String offenderNo) {
        init();
        try {
            final var response =
                    restTemplate.exchange(
                            BOOKING_OFFENCE_HISTORY_API_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<OffenceHistoryDetail>>() {
                            },
                            offenderNo);

            offenceHistory = response.getBody();

            buildResourceData(response);
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();

        offenceDetails = null;
        offenceHistory = null;
    }
}
