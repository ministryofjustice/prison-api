package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PersonalCareNeed;
import net.syscon.elite.api.model.ReasonableAdjustment;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.InmateRepository;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BookingResourceImplIntTest extends ResourceTest {

    @MockBean
    private InmateRepository inmateRepository;
    @SpyBean
    private BookingRepository bookingRepository;

    @Test
    public void getPersonalCaseNeeds() {
        final var bookingId = -1;

        when(inmateRepository.findPersonalCareNeeds(bookingId, Set.of("DISAB", "MATSTAT"))).thenReturn(List.of(createPersonalCareNeeds()));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/personal-care-needs?type=MATSTAT&type=DISAB+RM&type=DISAB+RC", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "personalcareneeds.json");

        verify(inmateRepository).findPersonalCareNeeds(bookingId, Set.of("DISAB", "MATSTAT"));
    }

    @Test
    public void getPersonalCaseNeeds_missingProblemType() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/personal-care-needs", HttpMethod.GET, requestEntity, String.class);
        assertThatJsonFileAndStatus(responseEntity, 400, "personalcareneeds_validation.json");
    }

    @Test
    public void getReasonableAdjustment() {
        final var bookingId = -1;
        final var treatmentCodes = List.of("WHEELCHR_ACC", "PEEP");
        when(inmateRepository.findReasonableAdjustments(bookingId, treatmentCodes)).thenReturn(
                List.of(
                        new ReasonableAdjustment("WHEELCHR_ACC", "abcd", LocalDate.of(2010, 6, 21), null),
                        new ReasonableAdjustment("PEEP", "efgh", LocalDate.of(2010, 6, 21), null))
        );

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/reasonable-adjustments?type=WHEELCHR_ACC&type=PEEP", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "reasonableadjustment.json");

        verify(inmateRepository).findReasonableAdjustments(bookingId, treatmentCodes);
    }

    private PersonalCareNeed createPersonalCareNeeds() {
        return PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").problemDescription("Preg, acc under 9mths").startDate(LocalDate.of(2010, 6, 21)).build();
    }

    @Test
    public void getReasonableAdjustment_missingTreatmentCodes() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/reasonable-adjustments", HttpMethod.GET, requestEntity, String.class);
        assertThatJsonFileAndStatus(responseEntity, 400, "reasonableadjustment_validation.json");
    }

    @Test
    public void getVisitBalances() {
        final var offenderNo = "A1234AA";

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/" + offenderNo + "/visit/balances", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "visitbalances.json");
    }

    @Test
    public void getVisitBalances_invalidBookingId() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/-3/visit/balances", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 404, "visitbalancesinvalidbookingid.json");
    }

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
        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/events", HttpMethod.GET, requestEntity, String.class);

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
