package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PersonalCareNeed;
import net.syscon.elite.api.model.ReasonableAdjustment;
import net.syscon.elite.repository.InmateRepository;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BookingResourceImplIntTest extends ResourceTest {

    @MockBean
    private InmateRepository inmateRepository;

    @Test
    public void getPersonalCaseNeeds() {
        final var bookingId = -1;

        when(inmateRepository.findPersonalCareNeeds(bookingId)).thenReturn(List.of(createPersonalCareNeeds()));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/bookings/-1/personal-care-needs?type=MATSTAT&type=DISAB+RM&type=DISAB+RC", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "personalcareneeds.json");

        verify(inmateRepository).findPersonalCareNeeds(bookingId);
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
                        new ReasonableAdjustment("WHEELCHR_ACC", "abcd", null, LocalDate.of(2010, 6, 21), null),
                        new ReasonableAdjustment("PEEP", "efgh", null, LocalDate.of(2010, 6, 21), null))
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
}
