package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.api.model.InmateDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingDetailSteps extends CommonSteps {
    private static final String API_BOOKING_REQUEST_URL = API_PREFIX + "bookings/{bookingId}";

    private InmateDetail inmateDetail;

    @Step("Retrieve offender booking details record")
    public void findBookingDetails(Long bookingId) {
        ResponseEntity<InmateDetail> response;

        try {
            response =
                    restTemplate.exchange(
                            API_BOOKING_REQUEST_URL,
                            HttpMethod.GET,
                            createEntity(),
                            InmateDetail.class,
                            bookingId);

            inmateDetail = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Verify offender booking - booking number")
    public void verifyOffenderBookingBookingNo(String bookingNo) {
        assertThat(inmateDetail.getBookingNo()).isEqualTo(bookingNo);
    }

    @Step("Verify offender booking - assigned officer id")
    public void verifyOffenderBookingAssignedOfficerId(Long assignedOfficerId) {
        assertThat(inmateDetail.getAssignedOfficerId()).isEqualTo(assignedOfficerId);
    }
}
