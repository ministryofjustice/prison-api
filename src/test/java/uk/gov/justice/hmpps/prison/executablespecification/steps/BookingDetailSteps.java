package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.PhysicalCharacteristic;
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation;
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
    private static final String API_OFFENDER_IMAGE_REQUEST_URL = API_PREFIX + "bookings/offenderNo/{offenderNo}/image/data";
    private static final String API_BOOKING_DETAILS_BY_OFFENDERS = API_PREFIX + "bookings/offenders";

    private InmateDetail inmateDetail;
    private PhysicalAttributes physicalAttributes;
    private List<PhysicalCharacteristic> physicalCharacteristics;
    private ImageDetail imageDetail;
    private byte[] imageBytes;
    private List<InmateDetail> offenders;
    private List<InmateBasicDetails> offendersBasic;
    private List<ProfileInformation> profileInformation;

    @Override
    protected void init() {
        super.init();

        inmateDetail = null;
        physicalAttributes = null;
        physicalCharacteristics = null;
        imageDetail = null;
        profileInformation = null;
        offenders = null;
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

    public void getPhysicalAttributes(final Long bookingId) {
        init();

        final ResponseEntity<PhysicalAttributes> response;

        try {
            response =
                    restTemplate.exchange(
                            API_BOOKING_REQUEST_URL + "/physicalAttributes",
                            HttpMethod.GET,
                            createEntity(),
                            PhysicalAttributes.class,
                            bookingId);

            physicalAttributes = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getPhysicalCharacteristics(final Long bookingId) {
        init();
        try {
            final var response = restTemplate.exchange(
                    API_BOOKING_REQUEST_URL + "/physicalCharacteristics", HttpMethod.GET,
                    createEntity(null, null),
                    new ParameterizedTypeReference<List<PhysicalCharacteristic>>() {
                    }, bookingId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            physicalCharacteristics = response.getBody();
            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getProfileInformation(final Long bookingId) {
        init();
        try {
            final var response = restTemplate.exchange(
                    API_BOOKING_REQUEST_URL + "/profileInformation", HttpMethod.GET,
                    createEntity(null, null),
                    new ParameterizedTypeReference<List<ProfileInformation>>() {
                    }, bookingId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            profileInformation = response.getBody();
            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getOffenderIdentifiers(final Long bookingId) {
        init();
        try {
            final var response = restTemplate.exchange(
                    API_BOOKING_REQUEST_URL + "/identifiers", HttpMethod.GET,
                    createEntity(null, null),
                    new ParameterizedTypeReference<List<OffenderIdentifier>>() {
                    }, bookingId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getImageMetadata(final Long bookingId) {
        init();

        final ResponseEntity<ImageDetail> response;

        try {
            response =
                    restTemplate.exchange(
                            API_BOOKING_REQUEST_URL + "/image",
                            HttpMethod.GET,
                            createEntity(),
                            ImageDetail.class,
                            bookingId);

            imageDetail = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getImageData(final Long bookingId, final boolean fullSizeImage) {
        init();

        final ResponseEntity<byte[]> response;
        try {
            response =
                    restTemplate.exchange(
                            API_BOOKING_REQUEST_URL + format("/image/data?fullSizeImage=%s", (fullSizeImage ? "true" : "false")),
                            HttpMethod.GET,
                            createEntity(),
                            byte[].class,
                            bookingId);

            imageBytes = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getImageData(final String offenderNo, final boolean fullSizeImage) {
        init();

        final ResponseEntity<byte[]> response;
        try {
            response =
                    restTemplate.exchange(
                            API_OFFENDER_IMAGE_REQUEST_URL + format("?fullSizeImage=%s", (fullSizeImage ? "true" : "false")),
                            HttpMethod.GET,
                            createEntity(),
                            byte[].class,
                            offenderNo);

            imageBytes = response.getBody();
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

    public void verifyImageBytesExists() {
        assertThat(imageBytes).isNotNull();
    }

    public void verifyImageMetadataExists() {
        assertThat(imageDetail).isNotNull();
    }

    public void verifyAlertTypes(final String types) {
        assertThat(inmateDetail.getAlertsCodes()).asList().containsAll(csv2list(types));
    }

    public void verifyField(final String field, final String value) throws ReflectiveOperationException {
        assertThat(inmateDetail).isNotNull();
        super.verifyField(inmateDetail, field, value);
    }

    public void verifyProfileInformation() {
        assertThat(profileInformation).asList().contains(
                ProfileInformation.builder().type("RELF").question("Religion").resultValue("Church of England").build(),
                ProfileInformation.builder().type("NAT").question("Nationality?").resultValue("Spaniard").build(),
                ProfileInformation.builder().type("SMOKE").question("Is the Offender a smoker?").resultValue("No").build());
    }

    public void findBookingDetails(final List<String> offenderNumbers) {
        init();
        try {
            final var response =
                    restTemplate.exchange(
                            API_BOOKING_DETAILS_BY_OFFENDERS,
                            HttpMethod.POST,
                            createEntity(offenderNumbers),
                            new ParameterizedTypeReference<List<InmateDetail>>() {
                            });

            offenders = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }


    public void verifyOffenders(final String firstName, final String lastName, final String middleName, final String offenderNo, final String bookingId, final String agencyId) {

        assertThat(offenders
                .stream()
                .filter(offender -> offender.getFirstName().equals(firstName) &&
                        offender.getLastName().equals(lastName) &&
                        offender.getMiddleName().equals(middleName) &&
                        offender.getBookingId().equals(Long.parseLong(bookingId)) &&
                        offender.getAgencyId().equals(agencyId) &&
                        offender.getOffenderNo().equals(offenderNo))
                .count())
                .isEqualTo(1);
    }

    public void verifyOffenderCount(final int size) {
        assertThat(offenders).hasSize(size);
    }

    public void verifyOffendersBasicCount(final int size) {
        assertThat(offendersBasic).hasSize(size);
    }
}
