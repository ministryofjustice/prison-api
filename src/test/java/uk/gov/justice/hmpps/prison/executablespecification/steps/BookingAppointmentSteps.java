package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.NewAppointment;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

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
        } catch (final PrisonApiClientException ex) {
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
        assertThat(scheduledEvent.getEventLocationId()).isEqualTo(-28L);
        assertThat(scheduledEvent.getAgencyId()).isEqualTo("LEI");
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
