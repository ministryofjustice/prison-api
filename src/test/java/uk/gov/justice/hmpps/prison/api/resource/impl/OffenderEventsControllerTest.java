package uk.gov.justice.hmpps.prison.api.resource.impl;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.OffenderEvent;
import uk.gov.justice.hmpps.prison.service.XtagEventsService;
import uk.gov.justice.hmpps.prison.service.filters.OffenderEventsFilter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.core.ResolvableType.forType;


public class OffenderEventsControllerTest extends ResourceTest {

    @MockBean
    public XtagEventsService xtagEventsService;

    @Test
    public void canAccessBothOffenderAndXtagEvents() {
        final var from = LocalDateTime.of(2018, 10, 29, 15, 30);
        final var to = from.plusMinutes(5L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();
        when(xtagEventsService.findAll(ArgumentMatchers.eq(filter))).thenReturn(someXtagEvents(from));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);

        final var responseEntity = testRestTemplate.exchange(format("/api/events?from=%s&to=%s", filter.getFrom().toString(), filter.getTo().toString()), HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 200);

        assertThatJsonFile(responseEntity.getBody(), "offender_events.json");
    }


    @Test
    public void canAccessBothOffenderAndXtagEventsOtherRange() {
        final var from = LocalDateTime.of(2019, 1, 30, 22, 30);
        final var to = from.plusMinutes(2L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();
        when(xtagEventsService.findAll(ArgumentMatchers.eq(filter))).thenReturn(someXtagEventsSmaller(from));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);

        final var responseEntity = testRestTemplate.exchange(format("/api/events?from=%s&to=%s", filter.getFrom().toString(), filter.getTo().toString()), HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 200);

        assertThatJsonFile(responseEntity.getBody(), "offender_events_small.json");
    }

    private List<OffenderEvent> someXtagEventsSmaller(final LocalDateTime now) {
        return ImmutableList.of(
                OffenderEvent.builder()
                        .nomisEventType("BOOK_UPD_OASYS")
                        .eventType("BOOKING_NUMBER-CHANGED")
                        .eventDatetime(now.plusSeconds(1L))
                        .offenderId(-1001L)
                        .rootOffenderId(-1001L)
                        .build());
    }

    private List<OffenderEvent> someXtagEvents(final LocalDateTime now) {
        return ImmutableList.of(
                OffenderEvent.builder()
                        .nomisEventType("BOOK_UPD_OASYS")
                        .eventType("BOOKING_NUMBER-CHANGED")
                        .eventDatetime(now.plusSeconds(1L))
                        .offenderId(-1001L)
                        .rootOffenderId(-1001L)
                        .build(),
                OffenderEvent.builder()
                        .nomisEventType("OFF_RECEP_OASYS")
                        .eventType("OFFENDER_MOVEMENT-RECEPTION")
                        .eventDatetime(now.plusSeconds(2L))
                        .offenderId(-1001L)
                        .rootOffenderId(-1001L)
                        .build(),
                OffenderEvent.builder()
                        .nomisEventType("OFF_DISCH_OASYS")
                        .eventType("OFFENDER_MOVEMENT-DISCHARGE")
                        .eventDatetime(now.plusSeconds(3L))
                        .offenderId(-1001L)
                        .rootOffenderId(-1001L)
                        .build(),
                OffenderEvent.builder()
                        .nomisEventType("OFF_UPD_OASYS")
                        .eventType("OFFENDER_DETAILS-CHANGED")
                        .eventDatetime(now.plusSeconds(4L))
                        .offenderId(-1001L)
                        .rootOffenderId(-1001L)
                        .build(),
                OffenderEvent.builder()
                        .nomisEventType("OFF_UPD_OASYS")
                        .eventType("OFFENDER_DETAILS-CHANGED")
                        .eventDatetime(now.plusSeconds(5L))
                        .offenderId(-1002L)
                        .rootOffenderId(-1002L)
                        .build());
    }

    <T> void assertThatJsonFile(final String response, final String jsonFile) {
        final var responseAsJson = getBodyAsJsonContent(response);
        assertThat(responseAsJson).isEqualToJson(jsonFile);
    }

    private <T> JsonContent<T> getBodyAsJsonContent(final String response) {
        return new JsonContent<T>(getClass(), forType(String.class), Objects.requireNonNull(response));
    }
}
