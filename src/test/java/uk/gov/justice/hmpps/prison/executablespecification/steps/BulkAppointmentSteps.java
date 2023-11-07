package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.Repeat;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.RepeatPeriod;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BulkAppointmentSteps extends CommonSteps {

    private static final String BULK_APPOINTMENTS_URL = API_PREFIX + "appointments";
    private static final String BOOKING_APPOINTMENT_URL = API_PREFIX + "bookings/{bookingId}/appointments";

    private static final ParameterizedTypeReference<List<ScheduledEvent>> LIST_OF_SCHEDULED_EVENT = new ParameterizedTypeReference<>() {
    };
    private AppointmentDefaults defaults;
    private List<AppointmentDetails> details;
    private Repeat repeat;
    private ErrorResponse errorResponse;
    private Map<Long, List<ScheduledEvent>> eventsByBookingId;
    private int httpStatus;

    @Step("bulkAppointmentDefaults")
    public void appointmentDefaults(final AppointmentDefaults appointmentDefaults) {
        details = null;
        repeat = null;
        defaults = appointmentDefaults;
    }

    @Step("bulkAppointmentDetails")
    public void appointmentDetails(final List<AppointmentDetails> appointmentDetails) {
        details = appointmentDetails;
    }

    public void createBulkAppointments() {
        errorResponse = null;
        eventsByBookingId = new HashMap<>();
        try {
            final var response = restTemplate.exchange(
                    BULK_APPOINTMENTS_URL,
                    HttpMethod.POST,
                    createEntity(AppointmentsToCreate
                            .builder()
                            .appointmentDefaults(defaults)
                            .appointments(details)
                            .repeat(repeat)
                            .build()),
                    Void.class
            );
            httpStatus = response.getStatusCodeValue();
        } catch (final PrisonApiClientException e) {
            errorResponse = e.getErrorResponse();
            httpStatus = errorResponse.getStatus();
        }
    }

    public void repeats(RepeatPeriod period, int count) {
        repeat = Repeat.builder().repeatPeriod(period).count(count).build();
    }


    @Step("appointmentsOnDateAre")
    public void appointmentsAre(final LocalDate date, final List<Map<String, String>> appointments) {
        final Function<Map<String, String>, Long> classifier = item -> Long.valueOf(item.get("bookingId"));

        final var expectedAppointments = appointments.stream()
                .collect(Collectors.groupingBy(classifier, Collectors.toSet()));

        expectedAppointments.forEach((id, x) -> getAppointments(id, date));

        final var actualAppointments = eventsByBookingId
                .entrySet()
                .stream()
                .map(this::transformEntry)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(actualAppointments).isEqualTo(expectedAppointments);
    }

    private void getAppointments(final long bookingId, final LocalDate date) {
        final var uri = UriComponentsBuilder
                .fromPath(BOOKING_APPOINTMENT_URL)
                .queryParam("fromDate", date.toString())
                .queryParam("toDate", date.toString())
                .build(bookingId)
                .normalize();

        final var response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                createEntity(),
                LIST_OF_SCHEDULED_EVENT
        );

        eventsByBookingId.put(bookingId, response.getBody());
    }

    private Map.Entry<Long, Set<Map<String, String>>> transformEntry(final Map.Entry<Long, List<ScheduledEvent>> entry) {
        return new SimpleImmutableEntry<>(entry.getKey(), scheduledEventsToMaps(entry.getValue()));
    }

    private Set<Map<String, String>> scheduledEventsToMaps(final List<ScheduledEvent> events) {
        return events.stream()
                .map(se -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("bookingId", se.getBookingId().toString());
                    m.put("appointmentType", se.getEventSubType());
                    m.put("startTime", se.getStartTime().toString());
                    m.put("endTime", se.getEndTime() == null ? "" : se.getEndTime().toString());
                    m.put("eventLocation", se.getEventLocation());
                    return m;
                })
                .collect(Collectors.toSet());
    }

    public void assertRequestRejected() {
        assertThat(errorResponse).isNotNull();
    }

    public void assertHttpStatusCode(final int expectedStatusCode) {
        assertThat(httpStatus).isEqualTo(expectedStatusCode);
    }

}
