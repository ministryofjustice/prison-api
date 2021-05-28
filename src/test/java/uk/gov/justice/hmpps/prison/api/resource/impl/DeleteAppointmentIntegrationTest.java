package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.CreatedAppointmentDetails;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.Repeat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.hmpps.prison.api.model.bulkappointments.RepeatPeriod.DAILY;

public class DeleteAppointmentIntegrationTest extends ResourceTest {
    @Test
    public void deleteMultipleAppointments() {

        final var createAppointmentResponse = makeCreateAppointmentsRequest(getCreateAppointmentBody());
        final var appointmentIds = requireNonNull(createAppointmentResponse.getBody())
            .stream()
            .map(CreatedAppointmentDetails::getAppointmentEventId)
            .collect(toList());

        final var appointments = appointmentIds
            .stream()
            .map(this::makeGetAppointmentDetails)
            .map(org.springframework.http.HttpEntity::getBody)
            .filter(Objects::nonNull)
            .map(ScheduledEvent::getEventId)
            .collect(toList());

        assertThat(appointments).isEqualTo(appointmentIds);

        final var response = testRestTemplate.exchange(
            "/api/appointments/delete",
            HttpMethod.POST,
            createHttpEntity(validToken(List.of("ROLE_GLOBAL_APPOINTMENT")), appointmentIds),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        final var getAppointmentStatsCodes = appointmentIds
            .stream()
            .map(this::makeGetAppointmentDetails)
            .map(ResponseEntity::getStatusCodeValue)
            .collect(toList());

        assertThat(getAppointmentStatsCodes).containsExactly(404, 404);
    }

    @Test
    public void returnsNotAuthorised_whenMissingTheRightRole() {
        final var response = testRestTemplate.exchange(
            "/api/appointments/delete",
            HttpMethod.POST,
            createHttpEntity(validToken(List.of("1000")), List.of(1L)),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<List<CreatedAppointmentDetails>> makeCreateAppointmentsRequest(final AppointmentsToCreate body) {
        return testRestTemplate.exchange(
            "/api/appointments",
            HttpMethod.POST,
            createHttpEntity(validToken(List.of("ROLE_GLOBAL_APPOINTMENT")), body),
            new ParameterizedTypeReference<>() {
            });
    }

    private ResponseEntity<ScheduledEvent> makeGetAppointmentDetails(final Long appointmentId) {
        return testRestTemplate.exchange(
            "/api/appointments/{appointmentId}",
            HttpMethod.GET,
            createHttpEntity(validToken(List.of("ROLE_GLOBAL_APPOINTMENT")), null),
            new ParameterizedTypeReference<>() {
            }, appointmentId);
    }

    private AppointmentsToCreate getCreateAppointmentBody() {
        final var now = LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.SECONDS);
        final var in1Hour = now.plusHours(6);

        final var defaults = AppointmentDefaults
            .builder()
            .locationId(-25L)
            .appointmentType("ACTI")
            .startTime(now)
            .endTime(in1Hour)
            .build();

        return AppointmentsToCreate.builder()
            .appointmentDefaults(defaults)
            .repeat(Repeat.builder().repeatPeriod(DAILY).count(2).build())
            .appointments(List.of(AppointmentDetails
                .builder()
                .bookingId(-31L)
                .comment("Comment")
                .build())).build();
    }
}
