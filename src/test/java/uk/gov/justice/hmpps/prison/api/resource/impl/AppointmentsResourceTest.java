package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.aop.ProxyUserAspect;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppointmentsResourceTest extends ResourceTest {
    @SpyBean
    private ProxyUserAspect proxyUserAspect;

    @MockBean
    private BookingRepository bookingRepository;

    @Test
    public void createAnAppointment() {

        when(bookingRepository.checkBookingExists(anyLong())).thenReturn(true);
        when(bookingRepository.findBookingsIdsInAgency(any(),anyString())).thenReturn(List.of(1L, 2L));

        final var response = makeCreateAppointmentsRequest();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(bookingRepository).createMultipleAppointments(any(), any(), anyString());
    }

    @Test
    public void triggerProxyUserAspect() throws Throwable {
        makeCreateAppointmentsRequest();

        verify(proxyUserAspect).controllerCall(any());
    }

    private ResponseEntity<String> makeCreateAppointmentsRequest() {
        final AppointmentsToCreate body = getCreateAppointmentBody();

        return testRestTemplate.exchange(
                "/api/appointments",
                HttpMethod.POST,
                createHttpEntity(validToken(List.of("BULK_APPOINTMENTS")), body),
                new ParameterizedTypeReference<>() {
                });
    }

    private AppointmentsToCreate getCreateAppointmentBody() {
        final var now = LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.SECONDS);
        final var in1Hour = now.plusHours(6);
        final var bookingIds = Arrays.asList(-31L, -32L);

        final var appointments = bookingIds
                .stream()
                .map(id -> AppointmentDetails
                        .builder()
                        .bookingId(id)
                        .comment("Comment")
                        .build())
                .collect(Collectors.toList());

        final var defaults = AppointmentDefaults
                .builder()
                .locationId(-25L) // LEI-CHAP
                .appointmentType("ACTI") // Activity
                .startTime(now)
                .endTime(in1Hour)
                .build();

        return AppointmentsToCreate.builder()
                .appointmentDefaults(defaults)
                .appointments(appointments).build();
    }
}
