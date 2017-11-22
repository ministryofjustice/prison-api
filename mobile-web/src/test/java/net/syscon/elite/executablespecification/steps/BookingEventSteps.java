package net.syscon.elite.executablespecification.steps;

/**
 * BDD step implementations for Reference Domains service.
 */
public class BookingEventSteps extends ScheduledEventSteps {
    private static final String BOOKING_EVENTS_API_URL = API_PREFIX + "bookings/{bookingId}/events";

    private int index = 0;

    protected void init() {
        super.init();
    }

    public void verifyField(String field, String value) throws ReflectiveOperationException {
        super.verifyField(scheduledEvents, field, value);
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

    public void setIndex(int index) {
        this.index = index;
    }

    public void verifyIndexedEventType(String value) throws ReflectiveOperationException {
        verifyField(scheduledEvents.get(index), "eventType", value);
    }

    public void verifyEventLocation(String value) {
        verifyEventLocation(index, value);
    }
}
