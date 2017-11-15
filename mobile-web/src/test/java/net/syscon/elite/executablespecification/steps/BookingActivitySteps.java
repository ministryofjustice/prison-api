package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.support.Order;
import net.thucydides.core.annotations.Step;

/**
 * BDD step implementations for Booking Activities feature.
 */
public class BookingActivitySteps extends ScheduledEventSteps {
    private static final String BOOKING_ACTIVITIES_API_URL = API_PREFIX + "bookings/{bookingId}/activities";

    @Override
    protected String getResourcePath() {
        return BOOKING_ACTIVITIES_API_URL;
    }

    @Step("Get activities for booking")
    public void getBookingActivities(Long bookingId, String fromDate, String toDate, String sortFields, Order sortOrder) {
        dispatchRequest(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Step("Get activities for booking for current day only")
    public void getBookingActivitiesForCurrentDay(Long bookingId) {
        dispatchRequestForCurrentDay(bookingId);
    }
}
