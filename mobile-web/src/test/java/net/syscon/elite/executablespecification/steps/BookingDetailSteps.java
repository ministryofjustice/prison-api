package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.InmateDetail;
import net.syscon.elite.api.model.PhysicalCharacteristic;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

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

    @Step("Verify offender booking number")
    public void verifyOffenderBookingNo(String bookingNo) {
        assertThat(inmateDetail.getBookingNo()).isEqualTo(bookingNo);
    }

    @Step("Verify offender assigned officer id")
    public void verifyOffenderAssignedOfficerId(Long assignedOfficerId) {
        assertThat(inmateDetail.getAssignedOfficerId()).isEqualTo(assignedOfficerId);
    }

    @Step("Verify religion")
    public void verifyReligion(String religion) {
        assertThat(inmateDetail.getReligion()).isEqualTo(religion);
    }

    @Step("Verify offender gender")
    public void verifyOffenderGender(String gender) {
        assertThat(inmateDetail.getPhysicalAttributes().getGender()).isEqualTo(gender);
    }

    @Step("Verify offender ethnicity")
    public void verifyOffenderEthnicity(String ethnicity) {
        assertThat(inmateDetail.getPhysicalAttributes().getEthnicity()).isEqualTo(ethnicity);
    }

    @Step("Verify offender height in feet")
    public void verifyOffenderHeightInFeet(Integer heightInFeet) {
        assertThat(inmateDetail.getPhysicalAttributes().getHeightFeet()).isEqualTo(heightInFeet);
    }

    @Step("Verify offender height in inches")
    public void verifyOffenderHeightInInches(Integer heightInInches) {
        assertThat(inmateDetail.getPhysicalAttributes().getHeightInches()).isEqualTo(heightInInches);
    }

    @Step("Verify offender height in centimetres")
    public void verifyOffenderHeightInCentimetres(Integer heightInCentimetres) {
        assertThat(inmateDetail.getPhysicalAttributes().getHeightCentimetres()).isEqualTo(heightInCentimetres);
    }

    @Step("Verify offender height in metres")
    public void verifyOffenderHeightInMetres(BigDecimal heightInMetres) {
        assertThat(inmateDetail.getPhysicalAttributes().getHeightMetres()).isEqualTo(heightInMetres);
    }

    @Step("Verify offender weight in pounds")
    public void verifyOffenderWeightInPounds(Integer weightInPounds) {
        assertThat(inmateDetail.getPhysicalAttributes().getWeightPounds()).isEqualTo(weightInPounds);
    }

    @Step("Verify offender weight in kilograms")
    public void verifyOffenderWeightInKilograms(Integer weightInKilograms) {
        assertThat(inmateDetail.getPhysicalAttributes().getWeightKilograms()).isEqualTo(weightInKilograms);
    }

    @Step("Verify offender physical characteristics")
    public void verifyOffenderPhysicalCharacteristics(String characteristicsList) {
        verifyPropertyMapValues(inmateDetail.getPhysicalCharacteristics(),
                PhysicalCharacteristic::getCharacteristic, PhysicalCharacteristic::getDetail, characteristicsList);
    }

    @Step("Verify active alert count")
    public void verifyActiveCount(int count) {
        assertThat(inmateDetail.getActiveAlertCount())
                .as(String.format("bookingId: %s",inmateDetail.getBookingId()))
                .isEqualTo(count);
    }

    @Step("Verify inactive alert count")
    public void verifyInactiveCount(int count) {

        assertThat(inmateDetail.getInactiveAlertCount())
                .as(String.format("bookingId: %s",inmateDetail.getBookingId()))
                .isEqualTo(count);
    }
}
