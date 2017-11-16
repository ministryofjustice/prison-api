package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ScheduledEvent;

import java.util.List;

/**
 * BDD step implementations for Reference Domains service.
 */
public class BookingEventSteps extends ScheduledEventSteps {
    private static final String BOOKING_EVENTS_API_URL = API_PREFIX + "bookings/{bookingId}/events";

    private List<ScheduledEvent> result;

    protected void init() {
        super.init();
        result = null;
    }

    public void verifyField(String field, String value) throws ReflectiveOperationException {
        super.verifyField(result, field, value);
    }

    @Override
    protected String getResourcePath() {
        return BOOKING_EVENTS_API_URL;
    }

    public void getThisWeeksEvents(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.THISWEEK);
    }

    public void getNextWeeksEvents(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.NEXTWEEK);
    }

    public void getTodaysEvents(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.TODAY);
    }

    public void verifyStartTimes(String timestampList) {
        verifyLocalDateTimeValues(result, ScheduledEvent::getStartTime, timestampList);
    }
}
