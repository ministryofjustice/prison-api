package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.support.Order;
import net.thucydides.core.annotations.Step;

/**
 * BDD step implementations for Booking Appointments feature.
 */
public class BookingAppointmentSteps extends ScheduledEventSteps {
    private static final String BOOKING_APPOINTMENTS_API_URL = API_PREFIX + "bookings/{bookingId}/appointments";

    @Override
    protected String getResourcePath() {
        return BOOKING_APPOINTMENTS_API_URL;
    }

    @Step("Get appointments for booking")
    public void getBookingAppointments(Long bookingId, String fromDate, String toDate, String sortFields, Order sortOrder) {
        dispatchRequest(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Step("Get appointments for booking for current day only")
    public void getBookingAppointmentsForCurrentDay(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.TODAY);
    }

    @Step("Get appointments for booking for 7 days ahead starting from current day")
    public void getBookingAppointmentsForThisWeek(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.THISWEEK);
    }

    @Step("Get appointments for booking for 7 days ahead starting from a week from current day")
    public void getBookingAppointmentsForNextWeek(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.NEXTWEEK);
    }
}
