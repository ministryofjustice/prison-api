package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.aop.ProxyUserAspect;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class AppointmentsResourceTest extends ResourceTest {
    @SpyBean
    private ProxyUserAspect proxyUserAspect;

    @MockBean
    private BookingRepository bookingRepository;

    @Test
    public void createAnAppointment() {

        when(bookingRepository.checkBookingExists(anyLong())).thenReturn(true);
        when(bookingRepository.findBookingsIdsInAgency(any(), anyString())).thenReturn(List.of(1L, 2L));

        final var response = makeCreateAppointmentsRequest();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(bookingRepository).createMultipleAppointments(any(), any(), anyString());
    }

    @Test
    public void triggerProxyUserAspect() throws Throwable {
        makeCreateAppointmentsRequest();

        verify(proxyUserAspect).controllerCall(any());
    }

    @Test
    public void deleteAnAppointment() {
        final var scheduledEvent = ScheduledEvent
            .builder()
            .eventId(1L)
            .eventType("APP")
            .eventSubType("VLB")
            .startTime(LocalDateTime.of(2020, 1, 1, 1, 1))
            .endTime(LocalDateTime.of(2020, 1, 1, 1, 31))
            .eventLocationId(2L)
            .build();

        when(bookingRepository.getBookingAppointmentByEventId(anyLong())).thenReturn(Optional.of(scheduledEvent));


        final var response = testRestTemplate.exchange(
            "/api/appointments/1",
            HttpMethod.DELETE,
            createHttpEntity(validToken(List.of("ROLE_GLOBAL_APPOINTMENT")), null),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(bookingRepository).getBookingAppointmentByEventId(1L);
    }

    @Test
    public void deleteAnAppointment_notFound() {
        when(bookingRepository.getBookingAppointmentByEventId(1L)).thenReturn(Optional.empty());

        final var response = testRestTemplate.exchange(
            "/api/appointments/1",
            HttpMethod.DELETE,
            createHttpEntity(validToken(List.of("ROLE_GLOBAL_APPOINTMENT")), null),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(bookingRepository).getBookingAppointmentByEventId(1L);
    }

    @Test
    public void deleteAnAppointment_notAuthorised() {
        final var response = testRestTemplate.exchange(
            "/api/appointments/1",
            HttpMethod.DELETE,
            createHttpEntity(validToken(List.of()), null),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    public void getAnAppointment() {
        final var scheduledEvent = ScheduledEvent
            .builder()
            .eventId(1L)
            .eventType("APP")
            .eventSubType("VLB")
            .startTime(LocalDateTime.of(2020, 1, 1, 1, 1))
            .endTime(LocalDateTime.of(2020, 1, 1, 1, 31))
            .eventLocationId(2L)
            .build();

        when(bookingRepository.getBookingAppointmentByEventId(anyLong())).thenReturn(Optional.of(scheduledEvent));

        final var response = testRestTemplate.exchange(
            "/api/appointments/1",
            HttpMethod.GET,
            createHttpEntity(validToken(List.of("ROLE_GLOBAL_APPOINTMENT")), null),
            ScheduledEvent.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(scheduledEvent);
        verify(bookingRepository).getBookingAppointmentByEventId(1L);
    }

    @Test
    public void getAnAppointment_notFound() {
        when(bookingRepository.getBookingAppointmentByEventId(anyLong())).thenReturn(Optional.empty());

        final var response = testRestTemplate.exchange(
            "/api/appointments/1",
            HttpMethod.GET,
            createHttpEntity(validToken(List.of("ROLE_GLOBAL_APPOINTMENT")), null),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(bookingRepository).getBookingAppointmentByEventId(1L);
    }

    @Test
    public void getAnAppointment_notAuthorised() {
        when(bookingRepository.getBookingAppointmentByEventId(anyLong())).thenReturn(Optional.empty());

        final var response = testRestTemplate.exchange(
            "/api/appointments/1",
            HttpMethod.GET,
            createHttpEntity(validToken(List.of()), null),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(bookingRepository);
    }

    private HttpHeaders headers(final String token, final MediaType contentType) {
        final var headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    public void updateAppointmentComment() {
        when(bookingRepository.updateBookingAppointmentComment(anyLong(), anyString())).thenReturn(true);

        final var response = testRestTemplate
            .exchange(
                "/api/appointments/1/comment",
                HttpMethod.PUT,
                new HttpEntity<>(
                    "Comment",
                    headers(
                        validToken(List.of("ROLE_GLOBAL_APPOINTMENT")),
                        MediaType.TEXT_PLAIN)
                ),
                Void.class
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(bookingRepository).updateBookingAppointmentComment(1L, "Comment");
    }

    @Test
    public void updateAppointmentComment_emptyComment() {
        when(bookingRepository.updateBookingAppointmentComment(anyLong(), isNull())).thenReturn(true);

        final var response = testRestTemplate
            .exchange(
                "/api/appointments/1/comment",
                HttpMethod.PUT,
                new HttpEntity<>(
                    "",
                    headers(validToken(List.of("ROLE_GLOBAL_APPOINTMENT")), MediaType.TEXT_PLAIN)),
                Void.class
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(bookingRepository).updateBookingAppointmentComment(1L, null);
    }

    @Test
    public void updateAppointmentComment_noComment() {
        when(bookingRepository.updateBookingAppointmentComment(anyLong(), isNull())).thenReturn(true);

        final var response = testRestTemplate
            .exchange(
                "/api/appointments/1/comment",
                HttpMethod.PUT,
                new HttpEntity<>(headers(validToken(List.of("ROLE_GLOBAL_APPOINTMENT")), MediaType.TEXT_PLAIN)),
                Void.class
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(bookingRepository).updateBookingAppointmentComment(1L, null);
    }

    @Test
    public void updateAppointmentComment_notFound() {
        when(bookingRepository.updateBookingAppointmentComment(anyLong(), anyString())).thenReturn(false);

        final var response = testRestTemplate
            .exchange(
                "/api/appointments/1/comment",
                HttpMethod.PUT,
                new HttpEntity<>(
                    "Comment",
                    headers(
                        validToken(List.of("ROLE_GLOBAL_APPOINTMENT")),
                        MediaType.TEXT_PLAIN)
                ),
                Void.class
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(bookingRepository).updateBookingAppointmentComment(1L, "Comment");
    }

    @Test
    public void updateAppointmentComment_unauthorised() {
        final var response = testRestTemplate
            .exchange(
                "/api/appointments/1/comment",
                HttpMethod.PUT,
                new HttpEntity<>(
                    "Comment",
                    headers(
                        validToken(List.of()),
                        MediaType.TEXT_PLAIN)
                ),
                Void.class
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        verifyNoInteractions(bookingRepository);
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
