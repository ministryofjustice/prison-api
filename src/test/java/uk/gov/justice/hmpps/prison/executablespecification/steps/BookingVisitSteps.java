package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
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
}
