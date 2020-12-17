package uk.gov.justice.hmpps.prison.service.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.v1.Event;
import uk.gov.justice.hmpps.prison.api.model.v1.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.repository.v1.AlertV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.BookingV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.CoreV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.EventsV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.FinanceV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.LegalV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.OffenderV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.PrisonV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.VisitV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.model.EventSP;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NomisApiV1ServiceTest {
    @Mock
    private BookingV1Repository bookingV1Repository;
    @Mock
    private OffenderV1Repository offenderV1Repository;
    @Mock
    private LegalV1Repository legalV1Repository;
    @Mock
    private FinanceV1Repository financeV1Repository;
    @Mock
    private AlertV1Repository alertV1Repository;
    @Mock
    private EventsV1Repository eventsV1Repository;
    @Mock
    private PrisonV1Repository prisonV1Repository;
    @Mock
    private CoreV1Repository coreV1Repository;
    @Mock
    private VisitV1Repository visitV1Repository;

    private NomisApiV1Service service;

    @BeforeEach
    public void setUp() {
        service = new NomisApiV1Service(bookingV1Repository, offenderV1Repository, legalV1Repository, financeV1Repository, alertV1Repository, eventsV1Repository, prisonV1Repository, coreV1Repository, visitV1Repository);
    }

    @Test
    public void getVisitAvailableDates_fromInPast() {
        final var from = LocalDate.now().minusDays(1);
        final var to = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> service.getVisitAvailableDates(12345L, from, to))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("Start date cannot be in the past");
    }

    @Test
    public void getVisitAvailableDates_fromTooFarInFuture() {
        final var from = LocalDate.now().plusDays(5);
        final var to = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> service.getVisitAvailableDates(12345L, from, to))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("End date cannot be before the start date");
    }

    @Test
    public void getVisitAvailableDates_TooFarInFuture() {
        final var from = LocalDate.now().plusDays(60);
        final var to = LocalDate.now().plusDays(61);
        assertThatThrownBy(() -> service.getVisitAvailableDates(12345L, from, to))
                .isInstanceOf(HttpClientErrorException.class)
            .hasMessageContaining("End date cannot be more than 60 days in the future");
    }

    @Test
    public void getVisitAvailableDates() {
        final var from = LocalDate.now();
        final var to = LocalDate.now().plusDays(5);
        service.getVisitAvailableDates(12345L, from, to);
        verify(visitV1Repository).getAvailableDates(12345L, from, to);
    }

    @Test
    public void getEvents() {
        final var date = LocalDateTime.parse("2020-01-02T03:02:01");
        when(eventsV1Repository.getEvents(anyString(), isNull(), anyLong(), isNull(), anyString(), any(), anyLong())).thenReturn(
            List.of(new EventSP(5L, date, "MDI", "A1234", "ETYPE", "Event ", "Data 2", " and 3"))
        );
        final var events = service.getEvents("prison", new OffenderIdentifier("12345"), "type", LocalDateTime.now(), 5L);
        assertThat(events).containsExactly(new Event("ETYPE", 5L, "A1234", "MDI", date, "Event Data 2 and 3"));
    }

    @Test
    public void getEventsCopesWithAllNull() {
        final var date = LocalDateTime.parse("2020-01-02T03:02:01");
        when(eventsV1Repository.getEvents(anyString(), isNull(), anyLong(), isNull(), anyString(), any(), anyLong())).thenReturn(
            List.of(new EventSP(5L, date, "MDI", "A1234", "ETYPE", null, null, null))
        );
        final var events = service.getEvents("prison", new OffenderIdentifier("12345"), "type", LocalDateTime.now(), 5L);
        assertThat(events).containsExactly(new Event("ETYPE", 5L, "A1234", "MDI", date, "{}"));
    }

    @Test
    public void getEventsCopesWithSomeNull() {
        final var date = LocalDateTime.parse("2020-01-02T03:02:01");
        when(eventsV1Repository.getEvents(anyString(), isNull(), anyLong(), isNull(), anyString(), any(), anyLong())).thenReturn(
            List.of(new EventSP(5L, date, "MDI", "A1234", "ETYPE", null, " a value ", null))
        );
        final var events = service.getEvents("prison", new OffenderIdentifier("12345"), "type", LocalDateTime.now(), 5L);
        assertThat(events).containsExactly(new Event("ETYPE", 5L, "A1234", "MDI", date, " a value "));
    }
}
