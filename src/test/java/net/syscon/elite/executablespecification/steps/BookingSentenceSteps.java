package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Offence;
import net.syscon.elite.api.model.OffenceDetail;
import net.syscon.elite.api.model.OffenceHistoryDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingSentenceSteps extends CommonSteps {
    private static final String BOOKING_MAIN_OFFENCE_API_GET_URL = API_PREFIX + "bookings/{bookingId}/mainOffence";
    private static final String BOOKING_MAIN_OFFENCE_API_POST_URL = API_PREFIX + "bookings/mainOffence";
    private static final String BOOKING_OFFENCE_HISTORY_API_URL = API_PREFIX + "bookings/offenderNo/{offenderNo}/offenceHistory";

    private List<OffenceDetail> offenceDetails;
    private List<Offence> offences;
    private List<OffenceHistoryDetail> offenceHistory;

    @Step("Get main offence details for offender")
    public void getMainOffenceDetails(final Long bookingId) {
        dispatchMainOffenceGETRequest(bookingId);
    }

    @Step("Verify offence description for specific offence")
    public void verifyOffenceDescription(final int index, final String expectedOffenceDescription) {
        validateResourcesIndex(index);
        assertThat(offenceDetails.get(index).getOffenceDescription()).isEqualTo(expectedOffenceDescription);
    }

    @Step("Get main offence details for offender - POST")
    public void getMainOffenceDetails(final List<String> bookingIds) {
        dispatchMainOffencePOSTRequest(bookingIds);
    }

    @Step("Verify offence codes for specific offence - POST")
    public void verifyOffenceCodes(final int index, final String expectedOffenceCode, final String expectedStatuteCode) {
        validateResourcesIndex(index);
        assertThat(offences.get(index).getOffenceCode()).isEqualTo(expectedOffenceCode);
        assertThat(offences.get(index).getStatuteCode()).isEqualTo(expectedStatuteCode);
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

    private void dispatchMainOffenceGETRequest(final Long bookingId) {
        init();

        final ResponseEntity<List<OffenceDetail>> response;

        try {
            response =
                    restTemplate.exchange(
                            BOOKING_MAIN_OFFENCE_API_GET_URL,
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

    private void dispatchMainOffencePOSTRequest(final List<String> bookingIds) {
        init();

        try {
            final var response =
                    restTemplate.exchange(
                            BOOKING_MAIN_OFFENCE_API_POST_URL,
                            HttpMethod.POST,
                            createEntity(bookingIds),
                            new ParameterizedTypeReference<List<Offence>>() {
                            });

            offences = response.getBody();
            offences.sort(Comparator.comparing(Offence::getBookingId).thenComparing(Offence::getOffenceCode));

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
        offences = null;
        offenceHistory = null;
    }
}
