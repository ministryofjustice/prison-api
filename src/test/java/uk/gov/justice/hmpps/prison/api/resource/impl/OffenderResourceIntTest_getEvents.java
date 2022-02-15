package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("OffenderResource get Events")
public class OffenderResourceIntTest_getEvents extends ResourceTest {
    @SpyBean
    private BookingRepository bookingRepository;

    @Test
    public void getEvents() {
        when(bookingRepository.getBookingActivities(anyLong(), any(), any(), anyString(), any())).thenReturn(
            List.of(createEvent("act", "10:11:12"),
                createEvent("act", "08:59:50"))
        );
        when(bookingRepository.getBookingVisits(anyLong(), any(), any(), anyString(), any())).thenReturn(
            List.of(createEvent("vis", "09:02:03"))
        );
        when(bookingRepository.getBookingAppointments(anyLong(), any(), any(), anyString(), any())).thenReturn(
            List.of(createEvent("app", null))
        );
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/offenders/A1234AA/events", HttpMethod.GET, requestEntity, String.class);
    
        assertThatJsonFileAndStatus(responseEntity, 200, "events.json");
    }

    private ScheduledEvent createEvent(final String type, final String time) {
        return ScheduledEvent.builder().bookingId(-1L)
            .startTime(Optional.ofNullable(time).map(t -> "2019-01-02T" + t).map(LocalDateTime::parse).orElse(null))
            .eventType(type + time)
            .eventSubType("some sub " + type)
            .build();
    }
}
