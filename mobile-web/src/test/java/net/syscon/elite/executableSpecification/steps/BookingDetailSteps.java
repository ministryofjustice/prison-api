package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.InmateDetails;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingDetailSteps extends CommonSteps {
    private static final String API_BOOKING_REQUEST_URL = API_PREFIX + "booking/{bookingId}";

    private InmateDetails inmateDetails;

    @Step("Retrieve offender booking details record")
    public void findBookingDetails(Long bookingId) {
        ResponseEntity<InmateDetails> response =
                restTemplate.exchange(
                        API_BOOKING_REQUEST_URL,
                        HttpMethod.GET,
                        createEntity(),
                        InmateDetails.class,
                        bookingId);

        inmateDetails = response.getBody();

        if (response.getStatusCode() != HttpStatus.OK) {
            setAdditionalResponseProperties(inmateDetails.getAdditionalProperties());
            setReceivedResponse(response);
        }
    }

    @Step("Verify offender booking - booking number")
    public void verifyOffenderBookingBookingNo(String bookingNo) {
        assertThat(inmateDetails.getBookingNo()).isEqualTo(bookingNo);
    }

    @Step("Verify offender booking - assigned officer id")
    public void verifyOffenderBookingAssignedOfficerId(Long assignedOfficerId) {
        assertThat(inmateDetails.getAssignedOfficerId()).isEqualTo(assignedOfficerId);
    }
}
