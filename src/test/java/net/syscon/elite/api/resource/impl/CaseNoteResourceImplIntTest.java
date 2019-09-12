package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CaseNoteEvent;
import net.syscon.elite.repository.CaseNoteRepository;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CaseNoteResourceImplIntTest extends ResourceTest {
    @MockBean
    private CaseNoteRepository caseNoteRepository;

    @Test
    public void transferTransaction() {
        final var fromDate = LocalDateTime.now();
        final var fredEvent = createEvent("FRED", "JOE");
        final var bobJoeEvent = createEvent("BOB", "JOE");
        when(caseNoteRepository.getCaseNoteEvents(any())).thenReturn(List.of(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/case-notes/events?type=BOB+JOE&type=FRED&createdDate=" + fromDate.toString(), HttpMethod.GET, requestEntity, String.class);

        System.out.println(responseEntity.getBody());
        assertThatJsonFileAndStatus(responseEntity, 200, "casenoteevents.json");

        verify(caseNoteRepository).getCaseNoteEvents(fromDate);
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
