package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Visit;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking Visits feature.
 */
public class BookingVisitSteps extends ScheduledEventSteps {
    private static final String BOOKING_VISITS_API_URL = API_PREFIX + "bookings/{bookingId}/visits";
    private static final String BOOKING_VISIT_LAST_API_URL = API_PREFIX + "bookings/{bookingId}/visits/last";
    private static final String BOOKING_VISIT_NEXT_API_URL = API_PREFIX + "bookings/{bookingId}/visits/next";


    private Visit lastVisit;

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

    @Step("Get last visit for booking")
    public void getBookingVisitLast(final Long bookingId) {
        dispatchRequest(BOOKING_VISIT_LAST_API_URL, bookingId);
    }

    @Step("Get next visit for booking")
    public void getBookingVisitNext(final Long bookingId) {
        dispatchRequest(BOOKING_VISIT_NEXT_API_URL, bookingId);
    }

    private void dispatchRequest(final String url, final Long bookingId) {
        init();
        final ResponseEntity<Visit> response;
        try {
            response = restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<Visit>() {},
                            bookingId);
            lastVisit = response.getBody();

        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void verifyVisitField(final String field, final String value) throws ReflectiveOperationException {
        verifyField(lastVisit, field, value);
    }

    public void verifyStartDateTime(final LocalDateTime expectedStartDateTime) {
        assertThat(lastVisit.getStartTime()).isEqualTo(expectedStartDateTime);
    }

    public void verifyEndDateTime(final LocalDateTime expectedEndDateTime) {
        assertThat(lastVisit.getEndTime()).isEqualTo(expectedEndDateTime);

    }
}
