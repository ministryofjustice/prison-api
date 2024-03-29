package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.PhysicalCharacteristic;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingDetailSteps extends CommonSteps {
    private static final String API_BOOKING_REQUEST_URL = API_PREFIX + "bookings/{bookingId}";

    private InmateDetail inmateDetail;
    private PhysicalAttributes physicalAttributes;
    private List<PhysicalCharacteristic> physicalCharacteristics;

    @Override
    protected void init() {
        super.init();

        inmateDetail = null;
        physicalAttributes = null;
        physicalCharacteristics = null;
    }

    @Step("Retrieve offender booking details record")
    public void findBookingDetails(final Long bookingId, final boolean basicInfo) {
        init();

        final ResponseEntity<InmateDetail> response;

        try {
            response =
                    restTemplate.exchange(
                            API_BOOKING_REQUEST_URL + format("?basicInfo=%s", basicInfo),
                            HttpMethod.GET,
                            createEntity(),
                            InmateDetail.class,
                            bookingId);

            inmateDetail = response.getBody();
            physicalAttributes = inmateDetail.getPhysicalAttributes();
            physicalCharacteristics = inmateDetail.getPhysicalCharacteristics();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Verify offender booking number")
    public void verifyOffenderBookingNo(final String bookingNo) {
        assertThat(inmateDetail.getBookingNo()).isEqualTo(bookingNo);
    }

    @Step("Verify offender first name")
    public void verifyOffenderFirstName(final String firstName) {
        assertThat(inmateDetail.getFirstName()).isEqualTo(firstName);
    }

    @Step("Verify offender last name")
    public void verifyOffenderLastName(final String lastName) {
        assertThat(inmateDetail.getLastName()).isEqualTo(lastName);
    }

    @Step("Verify offender display no")
    public void verifyOffenderNo(final String offenderNo) {
        assertThat(inmateDetail.getOffenderNo()).isEqualTo(offenderNo);
    }

    @Step("Verify offender booking number")
    public void verifyOffenderActiveFlag(final boolean activeFlag) {
        assertThat(inmateDetail.isActiveFlag()).isEqualTo(activeFlag);
    }

    @Step("Verify language")
    public void verifyLanguage(final String language) throws ReflectiveOperationException {
        verifyField(inmateDetail, "language", language);
    }

    @Step("Verify offender gender")
    public void verifyOffenderGender(final String gender) {
        assertThat(physicalAttributes.getGender()).isEqualTo(gender);
    }

    @Step("Verify offender ethnicity")
    public void verifyOffenderEthnicity(final String ethnicity) {
        assertThat(physicalAttributes.getEthnicity()).isEqualTo(ethnicity);
    }

    @Step("Verify offender height in feet")
    public void verifyOffenderHeightInFeet(final Integer heightInFeet) {
        assertThat(physicalAttributes.getHeightFeet()).isEqualTo(heightInFeet);
    }

    @Step("Verify offender height in inches")
    public void verifyOffenderHeightInInches(final Integer heightInInches) {
        assertThat(physicalAttributes.getHeightInches()).isEqualTo(heightInInches);
    }

    @Step("Verify offender height in centimetres")
    public void verifyOffenderHeightInCentimetres(final Integer heightInCentimetres) {
        assertThat(physicalAttributes.getHeightCentimetres()).isEqualTo(heightInCentimetres);
    }

    @Step("Verify offender height in metres")
    public void verifyOffenderHeightInMetres(final BigDecimal heightInMetres) {
        assertThat(physicalAttributes.getHeightMetres()).isEqualTo(heightInMetres);
    }

    @Step("Verify offender weight in pounds")
    public void verifyOffenderWeightInPounds(final Integer weightInPounds) {
        assertThat(physicalAttributes.getWeightPounds()).isEqualTo(weightInPounds);
    }

    @Step("Verify offender weight in kilograms")
    public void verifyOffenderWeightInKilograms(final Integer weightInKilograms) {
        assertThat(physicalAttributes.getWeightKilograms()).isEqualTo(weightInKilograms);
    }

    @Step("Verify offender physical characteristics")
    public void verifyOffenderPhysicalCharacteristics(final String characteristicsList) {
        verifyPropertyMapValues(physicalCharacteristics,
                PhysicalCharacteristic::getCharacteristic, PhysicalCharacteristic::getDetail, characteristicsList);
    }

    @Step("Verify active alert count")
    public void verifyActiveCount(final int count) {
        assertThat(inmateDetail.getActiveAlertCount())
                .as(format("bookingId: %s", inmateDetail.getBookingId()))
                .isEqualTo(count);
    }

    @Step("Verify inactive alert count")
    public void verifyInactiveCount(final int count) {

        assertThat(inmateDetail.getInactiveAlertCount())
                .as(format("bookingId: %s", inmateDetail.getBookingId()))
                .isEqualTo(count);
    }

    public void verifyAlertTypes(final String types) {
        assertThat(inmateDetail.getAlertsCodes()).asList().containsAll(csv2list(types));
    }

    public void verifyField(final String field, final String value) throws ReflectiveOperationException {
        assertThat(inmateDetail).isNotNull();
        super.verifyField(inmateDetail, field, value);
    }

}
