package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

/**
 * BDD step implementations for Booking Activities feature.
 */
public class BookingActivitySteps extends ScheduledEventSteps {
    private static final String BOOKING_ACTIVITIES_API_URL = API_PREFIX + "bookings/{bookingId}/activities";
    private static final String API_REQUEST_FOR_UPDATE = API_PREFIX + "bookings/offenderNo/{offenderNo}/activities/{activityId}/attendance";
    @Autowired
    private SchedulesSteps schedulesSteps;

    @Override
    protected String getResourcePath() {
        return BOOKING_ACTIVITIES_API_URL;
    }

    @Step("Get activities for booking")
    public void getBookingActivities(final Long bookingId, final String fromDate, final String toDate, final String sortFields, final Order sortOrder) {
        dispatchRequest(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Step("Get activities for booking for current day only")
    public void getBookingActivitiesForCurrentDay(final Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.TODAY);
    }

    private void dispatchUpdateRequest(final String offenderNo, final Long eventId, final UpdateAttendance updateAttendance) {
        init();
        try {
            restTemplate.exchange(API_REQUEST_FOR_UPDATE, HttpMethod.PUT,
                    createEntity(updateAttendance), Object.class, offenderNo, eventId);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchUpdateRequest(final Long bookingId, final Long eventId, final UpdateAttendance updateAttendance) {
        init();
        try {
            restTemplate.exchange(BOOKING_ACTIVITIES_API_URL + "/{activityId}/attendance", HttpMethod.PUT,
                    createEntity(updateAttendance), Object.class, bookingId, eventId);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }


    @Step("Update Attendance")
    public void updateAttendance(final String offenderNo, final Long activityId, final String outcome, final String performance, final String comment) {
        dispatchUpdateRequest(offenderNo, activityId,
                UpdateAttendance.builder()
                        .eventOutcome(outcome)
                        .performance(performance)
                        .outcomeComment(comment)
                        .build());
    }

    @Step("Update Attendance by Id")
    public void updateAttendance(final Long bookingId, final Long activityId, final String outcome, final String performance, final String comment) {
        dispatchUpdateRequest(bookingId, activityId,
                UpdateAttendance.builder()
                        .eventOutcome(outcome)
                        .performance(performance)
                        .outcomeComment(comment)
                        .build());
    }

    @Step("Verify Already Paid")
    public void verifyOffenderAlreadyPaid(final String paidActivity) {
        verifyBadRequest("Prisoner A1234AC has already been paid for '" + paidActivity + "'");
    }
}
