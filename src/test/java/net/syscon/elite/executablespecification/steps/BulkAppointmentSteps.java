package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.bulkappointments.AppointmentDefaults;
import net.syscon.elite.api.model.bulkappointments.AppointmentDetails;
import net.syscon.elite.api.model.bulkappointments.AppointmentsToCreate;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
    private ErrorResponse errorResponse;
    private Map<Long, List<ScheduledEvent>> eventsByBookingId;
    private int httpStatus;

    @Step("bulkAppointmentDefaults")
    public void appointmentDefaults(AppointmentDefaults appointmentDefaults) {
        defaults = appointmentDefaults;
    }

    @Step("bulkAppointmentDetails")
    public void appointmentDetails(List<AppointmentDetails> appointmentDetails) {
        details = appointmentDetails;
    }

    public void createBulkAppointments() {
        errorResponse = null;
        eventsByBookingId = new HashMap<>();
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    BULK_APPOINTMENTS_URL,
                    HttpMethod.POST,
                    createEntity(AppointmentsToCreate
                            .builder()
                            .appointmentDefaults(defaults)
                            .appointments(details)
                            .build()),
                    Void.class
            );
            httpStatus = response.getStatusCodeValue();
        } catch (EliteClientException e) {
            errorResponse = e.getErrorResponse();
            httpStatus = errorResponse.getStatus();
        }
    }

    @Step("appointmentsOnDateAre")
    public void appointmentsAre(LocalDate date, List<Map<String, String>> appointments) {
        Function<Map<String, String>, Long> classifier = item -> Long.valueOf(item.get("bookingId"));

        Map<Long, Set<Map<String, String>>> expectedAppointments = appointments.stream()
                .collect(Collectors.groupingBy(classifier, Collectors.toSet()));

        expectedAppointments.forEach((id, x) -> getAppointments(id, date));

        Map<Long, Set<Map<String, String>>> actualAppointments = eventsByBookingId
                .entrySet()
                .stream()
                .map(this::transformEntry)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(actualAppointments).isEqualTo(expectedAppointments);
    }

    private void getAppointments(long bookingId, LocalDate date) {
        URI uri = UriComponentsBuilder
                .fromPath(BOOKING_APPOINTMENT_URL)
                .queryParam("fromDate", date.toString())
                .queryParam("toDate", date.toString())
                .build(bookingId)
                .normalize();

        ResponseEntity<List<ScheduledEvent>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                createEntity(),
                LIST_OF_SCHEDULED_EVENT
        );

        eventsByBookingId.put(bookingId, response.getBody());
    }

    private Map.Entry<Long, Set<Map<String, String>>> transformEntry(Map.Entry<Long, List<ScheduledEvent>> entry) {
        return new SimpleImmutableEntry<>(entry.getKey(), scheduledEventsToMaps(entry.getValue()));
    }

    private Set<Map<String, String>> scheduledEventsToMaps(List<ScheduledEvent> events) {
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

    public void assertHttpStatusCode(int expectedStatusCode) {
        assertThat(httpStatus).isEqualTo(expectedStatusCode);
    }
}
