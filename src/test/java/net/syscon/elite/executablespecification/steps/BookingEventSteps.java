package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ScheduledEvent;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * BDD step implementations for Scheduled Events service.
 */
public class BookingEventSteps extends ScheduledEventSteps {
    private static final String BOOKING_EVENTS_API_URL = API_PREFIX + "bookings/{bookingId}/events";

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected String getResourcePath() {
        return BOOKING_EVENTS_API_URL;
    }

    public void getThisWeeksEvents(final Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.THISWEEK);
    }

    public void getNextWeeksEvents(final Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.NEXTWEEK);
    }

    public void getTodaysEvents(final Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.TODAY);
    }

    public void verifyEvents(final List<ScheduledEvent> expected) {
        final var expectedIterator = expected.iterator();
        final var actualIterator = scheduledEvents.iterator();
        while (expectedIterator.hasNext()) {
            final var expectedThis = expectedIterator.next();
            final var actualThis = actualIterator.next();
            assertEquals(getDump(), expectedThis.getEventType(), actualThis.getEventType());
            assertEquals(getDump(), expectedThis.getEventLocation(), actualThis.getEventLocation());
        }
        assertFalse("Too many actual events", actualIterator.hasNext());
    }

    private String getDump() {
        return "Actual event list is \r\n" + scheduledEvents.toString();
    }
}
