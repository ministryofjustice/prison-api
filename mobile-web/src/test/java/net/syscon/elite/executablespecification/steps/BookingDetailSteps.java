package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.*;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingDetailSteps extends CommonSteps {
    private static final String API_BOOKING_REQUEST_URL = API_PREFIX + "bookings/{bookingId}";

    private InmateDetail inmateDetail;
    private PhysicalAttributes physicalAttributes;
    private List<PhysicalCharacteristic> physicalCharacteristics;
    private ImageDetail imageDetail;

    @Override
    protected void init() {
        super.init();

        inmateDetail = null;
    }

    @Step("Retrieve offender booking details record")
    public void findBookingDetails(Long bookingId, boolean basicInfo) {
        init();

        ResponseEntity<InmateDetail> response;

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
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getPhysicalAttributes(Long bookingId) {
        init();

        ResponseEntity<PhysicalAttributes> response;

        try {
            response =
                    restTemplate.exchange(
                            API_BOOKING_REQUEST_URL + "/physicalAttributes",
                            HttpMethod.GET,
                            createEntity(),
                            PhysicalAttributes.class,
                            bookingId);

            physicalAttributes = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getPhysicalCharacteristics(Long bookingId) {
        init();
        try {
            ResponseEntity<List<PhysicalCharacteristic>> response = restTemplate.exchange(
                    API_BOOKING_REQUEST_URL + "/physicalCharacteristics", HttpMethod.GET,
                    createEntity(null, null),
                    new ParameterizedTypeReference<List<PhysicalCharacteristic>>() {}, bookingId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            physicalCharacteristics = response.getBody();
            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getProfileInformation(Long bookingId) {
        init();
        try {
            ResponseEntity<List<ProfileInformation>> response = restTemplate.exchange(
                    API_BOOKING_REQUEST_URL + "/profileInformation", HttpMethod.GET,
                    createEntity(null, null),
                    new ParameterizedTypeReference<List<ProfileInformation>>() {}, bookingId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getOffenderIdentifiers(Long bookingId) {
        init();
        try {
            ResponseEntity<List<OffenderIdentifier>> response = restTemplate.exchange(
                    API_BOOKING_REQUEST_URL + "/identifiers", HttpMethod.GET,
                    createEntity(null, null),
                    new ParameterizedTypeReference<List<OffenderIdentifier>>() {}, bookingId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getImageMetadata(Long bookingId) {
        init();

        ResponseEntity<ImageDetail> response;

        try {
            response =
                    restTemplate.exchange(
                            API_BOOKING_REQUEST_URL + "/image",
                            HttpMethod.GET,
                            createEntity(),
                            ImageDetail.class,
                            bookingId);

            imageDetail = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Verify offender booking number")
    public void verifyOffenderBookingNo(String bookingNo) {
        assertThat(inmateDetail.getBookingNo()).isEqualTo(bookingNo);
    }

    @Step("Verify offender first name")
    public void verifyOffenderFirstName(String firstName) {
        assertThat(inmateDetail.getFirstName()).isEqualTo(firstName);
    }

    @Step("Verify offender last name")
    public void verifyOffenderLastName(String lastName) {
        assertThat(inmateDetail.getLastName()).isEqualTo(lastName);
    }

    @Step("Verify offender display no")
    public void verifyOffenderNo(String offenderNo) {
        assertThat(inmateDetail.getOffenderNo()).isEqualTo(offenderNo);
    }

    @Step("Verify offender booking number")
    public void verifyOffenderActiveFlag(boolean activeFlag) {
        assertThat(inmateDetail.getActiveFlag()).isEqualTo(activeFlag);
    }
    @Step("Verify offender assigned officer id")
    public void verifyOffenderAssignedOfficerId(Long assignedOfficerId) {
        assertThat(inmateDetail.getAssignedOfficerId())
                .as("assignedOfficerId expected %d but was %d, inmateDetail = %s", assignedOfficerId,
                        inmateDetail.getAssignedOfficerId(), inmateDetail.toString())
                .isEqualTo(assignedOfficerId);
    }

    @Step("Verify religion")
    public void verifyReligion(String religion) {
        assertThat(inmateDetail.getReligion()).isEqualTo(religion);
    }

    @Step("Verify offender gender")
    public void verifyOffenderGender(String gender) {
        assertThat(physicalAttributes.getGender()).isEqualTo(gender);
    }

    @Step("Verify offender ethnicity")
    public void verifyOffenderEthnicity(String ethnicity) {
        assertThat(physicalAttributes.getEthnicity()).isEqualTo(ethnicity);
    }

    @Step("Verify offender height in feet")
    public void verifyOffenderHeightInFeet(Integer heightInFeet) {
        assertThat(physicalAttributes.getHeightFeet()).isEqualTo(heightInFeet);
    }

    @Step("Verify offender height in inches")
    public void verifyOffenderHeightInInches(Integer heightInInches) {
        assertThat(physicalAttributes.getHeightInches()).isEqualTo(heightInInches);
    }

    @Step("Verify offender height in centimetres")
    public void verifyOffenderHeightInCentimetres(Integer heightInCentimetres) {
        assertThat(physicalAttributes.getHeightCentimetres()).isEqualTo(heightInCentimetres);
    }

    @Step("Verify offender height in metres")
    public void verifyOffenderHeightInMetres(BigDecimal heightInMetres) {
        assertThat(physicalAttributes.getHeightMetres()).isEqualTo(heightInMetres);
    }

    @Step("Verify offender weight in pounds")
    public void verifyOffenderWeightInPounds(Integer weightInPounds) {
        assertThat(physicalAttributes.getWeightPounds()).isEqualTo(weightInPounds);
    }

    @Step("Verify offender weight in kilograms")
    public void verifyOffenderWeightInKilograms(Integer weightInKilograms) {
        assertThat(physicalAttributes.getWeightKilograms()).isEqualTo(weightInKilograms);
    }

    @Step("Verify offender physical characteristics")
    public void verifyOffenderPhysicalCharacteristics(String characteristicsList) {
        verifyPropertyMapValues(physicalCharacteristics,
                PhysicalCharacteristic::getCharacteristic, PhysicalCharacteristic::getDetail, characteristicsList);
    }

    @Step("Verify active alert count")
    public void verifyActiveCount(int count) {
        assertThat(inmateDetail.getActiveAlertCount())
                .as(format("bookingId: %s",inmateDetail.getBookingId()))
                .isEqualTo(count);
    }

    @Step("Verify inactive alert count")
    public void verifyInactiveCount(int count) {

        assertThat(inmateDetail.getInactiveAlertCount())
                .as(format("bookingId: %s",inmateDetail.getBookingId()))
                .isEqualTo(count);
    }

    public void verifyImageMetadataExists() {
        assertThat(imageDetail).isNotNull();
    }

    public void verifyAlertTypes(String types) {
        assertThat(inmateDetail.getAlertsCodes()).asList().containsAll(csv2list(types));
    }

    public void verifyField(String field, String value) throws ReflectiveOperationException {
        assertNotNull(inmateDetail);
        super.verifyField(inmateDetail, field, value);
    }
}
