package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * BDD step implementations for Booking Visits feature.
 */
public class BookingVisitSteps extends ScheduledEventSteps {
    private static final String BOOKING_VISITS_API_URL = API_PREFIX + "bookings/{bookingId}/visits";
    private static final String BOOKING_VISIT_LAST_API_URL = API_PREFIX + "bookings/{bookingId}/visits/last";

    private ScheduledEvent lastVisit;

    @Override
    protected String getResourcePath() {
        return BOOKING_VISITS_API_URL;
    }

    @Step("Get visits for booking")
    public void getBookingVisits(Long bookingId, String fromDate, String toDate, String sortFields, Order sortOrder) {
        dispatchRequest(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Step("Get visits for booking for current day only")
    public void getBookingVisitsForCurrentDay(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.TODAY);
    }

    @Step("Get last visit for booking")
    public void getBookingVisitLast(Long bookingId) {
        dispatchRequest(BOOKING_VISIT_LAST_API_URL, bookingId);
    }

    private void dispatchRequest(String url, Long bookingId) {
        init();
        ResponseEntity<ScheduledEvent> response;

        try {
            response = restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<ScheduledEvent>() {},
                            bookingId);
            lastVisit = response.getBody();

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void verifyVisitField(String field, String value) throws ReflectiveOperationException {
        verifyField(lastVisit, field, value);
    }
}
