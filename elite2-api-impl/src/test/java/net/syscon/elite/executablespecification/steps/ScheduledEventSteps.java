package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void verifyBookingId(Long expectedBookingId) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getBookingId()).isEqualTo(expectedBookingId);
        });
    }

    @Step("Verify event class for all scheduled events")
    public void verifyEventClass(String expectedEventClass) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventClass()).isEqualTo(expectedEventClass);
        });
    }

    @Step("Verify event status for all scheduled events")
    public void verifyEventStatus(String expectedEventStatus) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventStatus()).isEqualTo(expectedEventStatus);
        });
    }

    @Step("Verify event type for all scheduled events")
    public void verifyEventType(String expectedEventType) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventType()).isEqualTo(expectedEventType);
        });
    }

    @Step("Verify event type description for all scheduled events")
    public void verifyEventTypeDescription(String expectedEventTypeDescription) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventTypeDesc()).isEqualTo(expectedEventTypeDescription);
        });
    }

    @Step("Verify event source for all scheduled events")
    public void verifyEventSource(String expectedEventSource) {
        scheduledEvents.forEach(event -> {
            assertThat(event.getEventSource()).isEqualTo(expectedEventSource);
        });
    }

    @Step("Verify event status for specific scheduled event")
    public void verifyEventStatus(int index, String expectedEventStatus) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventStatus()).isEqualTo(expectedEventStatus);
    }

    @Step("Verify event sub type for specific scheduled event")
    public void verifyEventSubType(int index, String expectedEventSubType) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSubType()).isEqualTo(expectedEventSubType);
    }

    @Step("Verify event sub type description for specific scheduled event")
    public void verifyEventSubTypeDescription(int index, String expectedEventSubTypeDescription) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSubTypeDesc()).isEqualTo(expectedEventSubTypeDescription);
    }

    @Step("Verify event date for specific scheduled event")
    public void verifyEventDate(int index, String expectedEventDate) {
        validateResourcesIndex(index);
        verifyLocalDate(scheduledEvents.get(index).getEventDate(), expectedEventDate);
    }

    @Step("Verify start time for specific scheduled event")
    public void verifyStartTime(int index, String expectedStartTime) {
        validateResourcesIndex(index);
        verifyLocalDateTime(scheduledEvents.get(index).getStartTime(), expectedStartTime);
    }

    @Step("Verify end time for specific scheduled event")
    public void verifyEndTime(int index, String expectedEndTime) {
        validateResourcesIndex(index);
        verifyLocalDateTime(scheduledEvents.get(index).getEndTime(), expectedEndTime);
    }

    @Step("Verify event location for specific scheduled event")
    public void verifyEventLocation(int index, String expectedEventLocation) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventLocation()).isEqualTo(expectedEventLocation);
    }

    @Step("Verify event source code for specific scheduled event")
    public void verifyEventSourceCode(int index, String expectedEventSource) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSourceCode()).isEqualTo(expectedEventSource);
    }

    @Step("Verify event source description for specific scheduled event")
    public void verifyEventSourceDescription(int index, String expectedEventSourceDescription) {
        validateResourcesIndex(index);
        assertThat(scheduledEvents.get(index).getEventSourceDesc()).isEqualTo(expectedEventSourceDescription);
    }

    public void verifyEmpty() {
        assertTrue("Expecting no results", scheduledEvents.isEmpty());
    }

    public void verifyNumber(int number) {
        assertEquals(number, scheduledEvents.size());
    }

    protected void dispatchRequest(Long bookingId, String fromDate, String toDate, String sortFields, Order sortOrder) {
        String urlModifier = "";

        if (StringUtils.isNotBlank(fromDate)) {
            urlModifier += (FROM_DATE_QUERY_PARAM_PREFIX + fromDate);
        }

        if (StringUtils.isNotBlank(toDate)) {
            urlModifier += (TO_DATE_QUERY_PARAM_PREFIX + toDate);
        }

        if (StringUtils.isNotBlank(urlModifier)) {
            urlModifier = "?" + urlModifier.substring(1);
        }

        Map<String,String> headers = buildSortHeaders(sortFields, sortOrder);

        dispatchRequest(bookingId, urlModifier, headers);
    }

    protected void dispatchRequestForPeriod(Long bookingId, ScheduledEventPeriod period) {
        dispatchRequest(bookingId, period.getUrlModifier(), null);
    }

    private void dispatchRequest(Long bookingId, String urlModifier, Map<String,String> headers) {
        init();

        ResponseEntity<List<ScheduledEvent>> response;

        String url = getResourcePath() + urlModifier;

        try {
            response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            createEntity(null, headers),
                            new ParameterizedTypeReference<List<ScheduledEvent>>() {},
                            bookingId);

            scheduledEvents = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
    
    enum ScheduledEventPeriod {
        TODAY("/today"),
        THISWEEK("/thisWeek"),
        NEXTWEEK("/nextWeek");

        private String urlModifier;

        ScheduledEventPeriod(String urlModifier) {
            this.urlModifier = urlModifier;
        }

        public String getUrlModifier() {
            return urlModifier;
        }
    }
}
