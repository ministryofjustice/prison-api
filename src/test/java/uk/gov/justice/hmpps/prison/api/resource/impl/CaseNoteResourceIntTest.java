package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CaseNoteResourceIntTest extends ResourceTest {
    @MockBean
    private CaseNoteRepository caseNoteRepository;

    @Test
    public void getCaseNoteEvents_noLimit() {
        final var fromDate = LocalDateTime.now();
        final var fredEvent = createEvent("FRED", "JOE");
        final var bobJoeEvent = createEvent("BOB", "JOE");
        when(caseNoteRepository.getCaseNoteEvents(any(), anySet(), anyLong())).thenReturn(List.of(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/case-notes/events_no_limit?type=BOB+JOE&type=FRED&createdDate=" + fromDate, HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "casenoteevents.json");

        verify(caseNoteRepository).getCaseNoteEvents(fromDate, Set.of("BOB", "FRED"), Long.MAX_VALUE);
    }

    @Test
    public void getCaseNoteEvents() {
        final var fromDate = LocalDateTime.now();
        final var fredEvent = createEvent("FRED", "JOE");
        final var bobJoeEvent = createEvent("BOB", "JOE");
        when(caseNoteRepository.getCaseNoteEvents(any(), anySet(), anyLong())).thenReturn(List.of(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/case-notes/events?limit=10&type=BOB+JOE&type=FRED&createdDate=" + fromDate, HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "casenoteevents.json");

        verify(caseNoteRepository).getCaseNoteEvents(fromDate, Set.of("BOB", "FRED"), 10);
    }

    @Test
    public void getCaseNoteEvents_missingLimit() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/case-notes/events?&type=BOB+JOE&type=FRED&createdDate=" + LocalDateTime.now(), HttpMethod.GET, requestEntity, String.class);
        assertThatJsonFileAndStatus(responseEntity, 400, "casenoteevents_validation.json");
    }

    private CaseNoteEvent createEvent(final String type, final String subType) {
        return CaseNoteEvent.builder()
                .mainNoteType(type)
                .subNoteType(subType)
                .content("Some content for " + subType)
                .contactTimestamp(LocalDateTime.parse("2019-02-01T23:22:21"))
                .notificationTimestamp(LocalDateTime.parse("2019-02-01T23:22:21"))
                .establishmentCode("LEI")
                .firstName("FIRST")
                .lastName("LAST")
                .id(1L)
                .nomsId(123 + subType)
                .build();
    }
}
