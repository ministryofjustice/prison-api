package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * BDD step implementations for Booking Activities feature.
 */
public class BookingActivitySteps extends CommonSteps {
    private static final String BOOKING_ACTIVITIES_API_URL = API_PREFIX + "bookings/{bookingId}/activities";

    private List<ScheduledEvent> scheduledEvents;

    protected void init() {
        super.init();

        scheduledEvents = null;
    }

    @Step("Get activities for booking")
    public void getBookingActivities(Long bookingId) {
        dispatchRequest(bookingId);
    }

    private void dispatchRequest(Long bookingId) {
        init();

        ResponseEntity<List<ScheduledEvent>> response;

        try {
            response =
                    restTemplate.exchange(
                            BOOKING_ACTIVITIES_API_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<ScheduledEvent>>() {},
                            bookingId);

            scheduledEvents = response.getBody();

            buildResourceData(response, "scheduledEvents");
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}
