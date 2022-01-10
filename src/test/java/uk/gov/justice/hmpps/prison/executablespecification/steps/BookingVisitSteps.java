package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking Visits feature.
 */
public class BookingVisitSteps extends ScheduledEventSteps {
    private static final String BOOKING_VISITS_API_URL = API_PREFIX + "bookings/{bookingId}/visits";
    private static final String BOOKING_VISIT_NEXT_API_URL = API_PREFIX + "bookings/{bookingId}/visits/next";


    private VisitDetails lastVisitDetails;

    @Override
    protected String getResourcePath() {
        return BOOKING_VISITS_API_URL;
    }

    @Step("Get visits for booking")
    public void getBookingVisits(final Long bookingId, final String fromDate, final String toDate, final String sortFields, final Order sortOrder) {
        dispatchRequest(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Step("Get visits for booking for current day only")
    public void getBookingVisitsForCurrentDay(final Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.TODAY);
    }

    @Step("Get next visit for booking")
    public void getBookingVisitNext(final Long bookingId) {
        dispatchRequest(BOOKING_VISIT_NEXT_API_URL, bookingId);
    }

    private void dispatchRequest(final String url, final Long bookingId) {
        init();
        final ResponseEntity<VisitDetails> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<VisitDetails>() {
                    },
                    bookingId);
            lastVisitDetails = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void verifyVisitField(final String field, final String value) throws ReflectiveOperationException {
        verifyField(lastVisitDetails, field, value);
    }

    public void verifyStartDateTime(final LocalDateTime expectedStartDateTime) {
        assertThat(lastVisitDetails.getStartTime()).isEqualTo(expectedStartDateTime);
    }

    public void verifyEndDateTime(final LocalDateTime expectedEndDateTime) {
        assertThat(lastVisitDetails.getEndTime()).isEqualTo(expectedEndDateTime);

    }
}
