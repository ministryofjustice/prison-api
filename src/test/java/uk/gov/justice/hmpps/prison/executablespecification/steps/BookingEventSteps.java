package uk.gov.justice.hmpps.prison.executablespecification.steps;

import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    public void getTodaysEvents(final Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.TODAY);
    }

    public void verifyEvents(final List<ScheduledEvent> expected) {
        final var expectedIterator = expected.iterator();
        final var actualIterator = scheduledEvents.iterator();
        while (expectedIterator.hasNext()) {
            final var expectedThis = expectedIterator.next();
            final var actualThis = actualIterator.next();
            assertThat(actualThis.getEventType()).as(getDump()).isEqualTo(expectedThis.getEventType());
            assertThat(actualThis.getEventLocation()).as(getDump()).isEqualTo(expectedThis.getEventLocation());
        }
        assertThat(actualIterator.hasNext()).as("Too many actual events").isFalse();
    }

    private String getDump() {
        return "Actual event list is \r\n" + scheduledEvents.toString();
    }
}
