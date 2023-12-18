package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for 'Scheduled Event' APIs (e.g. Booking Activities, Booking Visits, etc.)
 */
public abstract class ScheduledEventSteps extends CommonSteps {
    private static final String FROM_DATE_QUERY_PARAM_PREFIX = "&fromDate=";
    private static final String TO_DATE_QUERY_PARAM_PREFIX = "&toDate=";

    protected List<ScheduledEvent> scheduledEvents;
    protected ScheduledEvent scheduledEvent;

    @Override
    protected void init() {
        super.init();
        scheduledEvents = null;
        scheduledEvent = null;
    }

    protected abstract String getResourcePath();

    @Step("Verify booking id for all scheduled events")
    public void verifyBookingId(final Long expectedBookingId) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getBookingId()).isEqualTo(expectedBookingId);
        });
    }

    @Step("Verify event class for all scheduled events")
    public void verifyEventClass(final String expectedEventClass) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventClass()).isEqualTo(expectedEventClass);
        });
    }

    @Step("Verify event status for all scheduled events")
    public void verifyEventStatus(final String expectedEventStatus) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventStatus()).isEqualTo(expectedEventStatus);
        });
    }

    @Step("Verify event type for all scheduled events")
    public void verifyEventType(final String expectedEventType) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventType()).isEqualTo(expectedEventType);
        });
    }

    @Step("Verify event type description for all scheduled events")
    public void verifyEventTypeDescription(final String expectedEventTypeDescription) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventTypeDesc()).isEqualTo(expectedEventTypeDescription);
        });
    }

    @Step("Verify event source for all scheduled events")
    public void verifyEventSource(final String expectedEventSource) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventSource()).isEqualTo(expectedEventSource);
        });
    }

    @Step("Verify event status for specific scheduled event")
    public void verifyEventStatus(final int index, final String expectedEventStatus) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventStatus()).isEqualTo(expectedEventStatus);
    }

    @Step("Verify event sub type for specific scheduled event")
    public void verifyEventSubType(final int index, final String expectedEventSubType) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSubType()).isEqualTo(expectedEventSubType);
    }

    @Step("Verify event sub type description for specific scheduled event")
    public void verifyEventSubTypeDescription(final int index, final String expectedEventSubTypeDescription) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSubTypeDesc()).isEqualTo(expectedEventSubTypeDescription);
    }

    @Step("Verify event date for specific scheduled event")
    public void verifyEventDate(final int index, final String expectedEventDate) {
        validateResourcesIndex(index);
        verifyLocalDate(scheduledEvents.get(index).getEventDate(), expectedEventDate);
    }

    @Step("Verify start time for specific scheduled event")
    public void verifyStartTime(final int index, final String expectedStartTime) {
        validateResourcesIndex(index);
        verifyLocalDateTime(scheduledEvents.get(index).getStartTime(), expectedStartTime);
    }

    @Step("Verify end time for specific scheduled event")
    public void verifyEndTime(final int index, final String expectedEndTime) {
        validateResourcesIndex(index);
        verifyLocalDateTime(scheduledEvents.get(index).getEndTime(), expectedEndTime);
    }

    @Step("Verify event location for specific scheduled event")
    public void verifyEventLocation(final int index, final String expectedEventLocation) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventLocation()).isEqualTo(expectedEventLocation);
    }

    @Step("Verify event source code for specific scheduled event")
    public void verifyEventSourceCode(final int index, final String expectedEventSource) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSourceCode()).isEqualTo(expectedEventSource);
    }

    @Step("Verify event source description for specific scheduled event")
    public void verifyEventSourceDescription(final int index, final String expectedEventSourceDescription) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSourceDesc()).isEqualTo(expectedEventSourceDescription);
    }

    public void verifyEmpty() {
        assertThat(scheduledEvents.isEmpty()).as("Expecting no results").isTrue();
    }

    protected void dispatchRequest(final Long bookingId, final String fromDate, final String toDate, final String sortFields, final Order sortOrder) {
        var urlModifier = "";

        if (StringUtils.isNotBlank(fromDate)) {
            urlModifier += (FROM_DATE_QUERY_PARAM_PREFIX + fromDate);
        }

        if (StringUtils.isNotBlank(toDate)) {
            urlModifier += (TO_DATE_QUERY_PARAM_PREFIX + toDate);
        }

        if (StringUtils.isNotBlank(urlModifier)) {
            urlModifier = "?" + urlModifier.substring(1);
        }

        final var headers = buildSortHeaders(sortFields, sortOrder);

        dispatchRequest(bookingId, urlModifier, headers);
    }

    protected void dispatchRequestForPeriod(final Long bookingId, final ScheduledEventPeriod period) {
        dispatchRequest(bookingId, period.getUrlModifier(), null);
    }

    private void dispatchRequest(final Long bookingId, final String urlModifier, final Map<String, String> headers) {
        init();

        final ResponseEntity<List<ScheduledEvent>> response;

        final var url = getResourcePath() + urlModifier;

        try {
            response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            createEntity(null, headers),
                            new ParameterizedTypeReference<List<ScheduledEvent>>() {
                            },
                            bookingId);

            scheduledEvents = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    enum ScheduledEventPeriod {
        TODAY("/today"),
        THISWEEK("/thisWeek");

        private String urlModifier;

        ScheduledEventPeriod(final String urlModifier) {
            this.urlModifier = urlModifier;
        }

        public String getUrlModifier() {
            return urlModifier;
        }
    }
}
