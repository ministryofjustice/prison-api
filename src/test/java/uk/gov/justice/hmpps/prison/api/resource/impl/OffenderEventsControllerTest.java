package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.OffenderEvent;
import uk.gov.justice.hmpps.prison.service.filters.OffenderEventsFilter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.ResolvableType.forType;


public class OffenderEventsControllerTest extends ResourceTest {

    @Test
    public void canAccessOffenderEvents() {
        final var from = LocalDateTime.of(2018, 10, 29, 15, 30);
        final var to = from.plusMinutes(5L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);

        final var responseEntity = testRestTemplate.exchange(format("/api/events?from=%s&to=%s", filter.getFrom().toString(), filter.getTo().toString()), HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 200);
        assertThatJsonFile(responseEntity.getBody(), "offender_events.json");
    }


    @Test
    public void canAccessOffenderEventsOtherRange() {
        final var from = LocalDateTime.of(2019, 1, 30, 22, 30);
        final var to = from.plusMinutes(2L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);
        final var responseEntity = testRestTemplate.exchange(format("/api/events?from=%s&to=%s", filter.getFrom().toString(), filter.getTo().toString()), HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 200);
        assertThatJsonFile(responseEntity.getBody(), "offender_events_small.json");
    }

    @Test
    public void canFilterOffenderEvents() {
        final var from = LocalDateTime.of(2018, 10, 29, 0, 0);
        final var to = from.plusDays(1L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);
        final var responseEntity = testRestTemplate.exchange(format("/api/events?from=%s&to=%s&type=%s", filter.getFrom().toString(), filter.getTo().toString(), "KA-KS"), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<OffenderEvent>>() {});
        assertThat(responseEntity.getBody()).extracting("eventType").containsOnly("KA-KS");

    }

    @Test
    public void cannotSpecifyMadeUpSortOrder() {
        final var from = LocalDateTime.of(2018, 10, 29, 0, 0);
        final var to = from.plusDays(1L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);
        final var responseEntity = testRestTemplate.exchange(format("/api/events?from=%s&to=%s&sortBy=%s", filter.getFrom().toString(), filter.getTo().toString(), "POPULARITY"), HttpMethod.GET, requestEntity, String.class);
        assertThatStatus(responseEntity, 400);
    }

    @Test
    public void defaultSortOrderIsByEventTimestampDesc() {
        final var from = LocalDateTime.of(2018, 10, 29, 0, 0);
        final var to = from.plusDays(1L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);
        final var responseEntity = testRestTemplate.exchange(format("/api/events?from=%s&to=%s", filter.getFrom().toString(), filter.getTo().toString()), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<OffenderEvent>>() {});

        assertThat(responseEntity.getBody()).isSortedAccordingTo(Comparator.comparing(OffenderEvent::getEventDatetime).reversed());
    }

    @Test
    public void canSpecifyOrderByEventTimestampDesc() {
        final var from = LocalDateTime.of(2018, 10, 29, 0, 0);
        final var to = from.plusDays(1L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);
        final var responseEntity = testRestTemplate.exchange(format("/api/events?from=%s&to=%s&sortBy=%s", filter.getFrom().toString(), filter.getTo().toString(), "TIMESTAMP_DESC"), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<OffenderEvent>>() {});

        assertThat(responseEntity.getBody()).isSortedAccordingTo(Comparator.comparing(OffenderEvent::getEventDatetime).reversed());
    }

    @Test
    public void canSpecifyOrderByEventTimestampAsc() {
        final var from = LocalDateTime.of(2018, 10, 29, 0, 0);
        final var to = from.plusDays(1L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);
        final var responseEntity = testRestTemplate.exchange(format("/api/events?from=%s&to=%s&sortBy=%s", filter.getFrom().toString(), filter.getTo().toString(), "TIMESTAMP_ASC"), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<OffenderEvent>>() {});

        assertThat(responseEntity.getBody()).isSortedAccordingTo(Comparator.comparing(OffenderEvent::getEventDatetime));
    }

    @Test
    public void canAccessTestEvents() {
        final var from = LocalDateTime.of(2018, 10, 29, 15, 30);
        final var to = from.plusMinutes(5L);

        final var filter = OffenderEventsFilter.builder().from(from).to(to).build();

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_PRISON_OFFENDER_EVENTS"), null);

        final var responseEntity = testRestTemplate.exchange(format("/api/test-events?from=%s&to=%s&useEnq=true", filter.getFrom().toString(), filter.getTo().toString()), HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 200);
        assertThatJson(responseEntity.getBody()).isArray().hasSize(2);
    }

    void assertThatJsonFile(final String response, final String jsonFile) {
        final var responseAsJson = getBodyAsJsonContent(response);
        assertThat(responseAsJson).isEqualToJson(jsonFile);
    }

    private <T> JsonContent<T> getBodyAsJsonContent(final String response) {
        return new JsonContent<>(getClass(), forType(String.class), Objects.requireNonNull(response));
    }
}
