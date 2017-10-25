package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Step("Verify booking id for all activities")
    public void verifyBookingId(Long expectedBookingId) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getBookingId()).isEqualTo(expectedBookingId);
        });
    }

    @Step("Verify event class for all activities")
    public void verifyEventClass(String expectedEventClass) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventClass()).isEqualTo(expectedEventClass);
        });
    }

    @Step("Verify event status for all activities")
    public void verifyEventStatus(String expectedEventStatus) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventStatus()).isEqualTo(expectedEventStatus);
        });
    }

    @Step("Verify event type for all activities")
    public void verifyEventType(String expectedEventType) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventType()).isEqualTo(expectedEventType);
        });
    }

    @Step("Verify event type description for all activities")
    public void verifyEventTypeDescription(String expectedEventTypeDescription) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventTypeDesc()).isEqualTo(expectedEventTypeDescription);
        });
    }

    @Step("Verify event source for all activities")
    public void verifyEventSource(String expectedEventSource) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventSource()).isEqualTo(expectedEventSource);
        });
    }

    @Step("Verify event sub type for specific activity")
    public void verifyEventSubType(int index, String expectedEventSubType) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSubType()).isEqualTo(expectedEventSubType);
    }

    @Step("Verify event sub type description for specific activity")
    public void verifyEventSubTypeDescription(int index, String expectedEventSubTypeDescription) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSubTypeDesc()).isEqualTo(expectedEventSubTypeDescription);
    }

    @Step("Verify event date for specific activity")
    public void verifyEventDate(int index, String expectedEventDate) {
        validateResourcesIndex(index);
        verifyLocalDate(scheduledEvents.get(index).getEventDate(), expectedEventDate);
    }

    @Step("Verify start time for specific activity")
    public void verifyStartTime(int index, String expectedStartTime) {
        validateResourcesIndex(index);
        verifyLocalDateTime(scheduledEvents.get(index).getStartTime(), expectedStartTime);
    }

    @Step("Verify end time for specific activity")
    public void verifyEndTime(int index, String expectedEndTime) {
        validateResourcesIndex(index);
        verifyLocalDateTime(scheduledEvents.get(index).getEndTime(), expectedEndTime);
    }

    @Step("Verify event location for specific activity")
    public void verifyEventLocation(int index, String expectedEventLocation) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventLocation()).isEqualTo(expectedEventLocation);
    }

    @Step("Verify event source code for specific activity")
    public void verifyEventSourceCode(int index, String expectedEventSource) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSourceCode()).isEqualTo(expectedEventSource);
    }

    @Step("Verify event source description for specific activity")
    public void verifyEventSourceDescription(int index, String expectedEventSourceDescription) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSourceDesc()).isEqualTo(expectedEventSourceDescription);
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
