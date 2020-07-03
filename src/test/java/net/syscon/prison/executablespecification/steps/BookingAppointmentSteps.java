package net.syscon.prison.executablespecification.steps;

import net.syscon.prison.api.model.NewAppointment;
import net.syscon.prison.api.model.ScheduledEvent;
import net.syscon.prison.api.support.Order;
import net.syscon.prison.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void getBookingAppointments(final Long bookingId, final String fromDate, final String toDate, final String sortFields, final Order sortOrder) {
        dispatchRequest(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Step("Get appointments for booking for current day only")
    public void getBookingAppointmentsForCurrentDay(final Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.TODAY);
    }

    @Step("Get appointments for booking for 7 days ahead starting from current day")
    public void getBookingAppointmentsForThisWeek(final Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.THISWEEK);
    }

    @Step("Get appointments for booking for 7 days ahead starting from a week from current day")
    public void getBookingAppointmentsForNextWeek(final Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.NEXTWEEK);
    }

    @Step("Create appointment")
    public void createAppointment(final Long bookingId, final String eventType, final LocalDateTime startDateTime, final Long locationId, final String comment) {
        dispatchCreateRequest(bookingId, NewAppointment.builder()
                .appointmentType(eventType)
                .startTime(startDateTime)
                .locationId(locationId)
                .comment(comment)
                .build());
    }

    private void dispatchCreateRequest(final Long bookingId, final NewAppointment newAppointment) {
        init();
        try {
            final var response = restTemplate.exchange(BOOKING_APPOINTMENTS_API_URL,
                    HttpMethod.POST, createEntity(newAppointment), ScheduledEvent.class, bookingId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            scheduledEvent = response.getBody();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Verify created appointment")
    public void verifyCreatedAppointment() {
        assertThat(scheduledEvent).isNotNull();
        assertThat(scheduledEvent.getEventId()).isNotNull();
        assertThat(scheduledEvent.getEventId()).isNotZero();
        assertThat(scheduledEvent.getBookingId()).isEqualTo(-4L);
        assertThat(scheduledEvent.getEventDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(scheduledEvent.getEventLocation()).isEqualTo("Visiting Room");
        assertThat(scheduledEvent.getEventType()).isEqualTo("APP");
        assertThat(scheduledEvent.getEventSubType()).isEqualTo("MEDE");
        assertThat(scheduledEvent.getEventStatus()).isEqualTo("SCH");
        assertThat(scheduledEvent.getEventClass()).isEqualTo("INT_MOV");
        assertThat(scheduledEvent.getEventSource()).isEqualTo("APP");
        assertThat(scheduledEvent.getEventSourceCode()).isEqualTo("APP");
        assertThat(scheduledEvent.getEventSourceDesc()).isEqualTo("a comment");
        assertThat(scheduledEvent.getStartTime().toString().substring(11)).isEqualTo("16:00");
        assertThat(scheduledEvent.getEndTime()).isNull();
    }
}
