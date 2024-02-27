package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingAssessmentSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingDetailSteps;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * BDD step definitions for the following Booking API endpoints:
 * <ul>
 *     <li>/booking/{bookingId}</li>
 *     <li>/bookings/{bookingId}/sentenceDetail</li>
 *     <li>/bookings/{bookingId}/balances</li>
 *     <li>/bookings/{bookingId}/mainSentence</li>
 * </ul>
 * <p>
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class BookingStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BookingDetailSteps bookingDetail;

    @Autowired
    private BookingAssessmentSteps bookingAssessment;

    @When("^an offender booking request is made with booking id \"([^\"]*)\"$")
    public void anOffenderBookingRequestIsMadeWithBookingId(final String bookingId) {
        bookingDetail.findBookingDetails(Long.valueOf(bookingId), false);
    }

    @When("^a basic offender booking request is made with booking id \"([^\"]*)\"$")
    public void aBasicOffenderBookingRequestIsMadeWithBookingId(final String bookingId) {
        bookingDetail.findBookingDetails(Long.valueOf(bookingId), true);
    }

    @Then("^booking number of offender booking returned is \"([^\"]*)\"$")
    public void bookingNumberOfOffenderBookingReturnedIs(final String bookingNo) {
        bookingDetail.verifyOffenderBookingNo(bookingNo);
    }

    @And("^language of offender booking returned is \"([^\"]*)\"$")
    public void languageOfOffenderBookingReturnedIs(final String language) throws ReflectiveOperationException {
        bookingDetail.verifyLanguage(language);
    }

    @And("^firstname of offender booking returned is \"([^\"]*)\"$")
    public void firstnameOfOffenderBookingReturnedIs(final String firstname) {
        bookingDetail.verifyOffenderFirstName(firstname);
    }

    @And("^lastName of offender booking returned is \"([^\"]*)\"$")
    public void lastnameOfOffenderBookingReturnedIs(final String lastName) {
        bookingDetail.verifyOffenderLastName(lastName);
    }

    @And("^offenderNo of offender booking returned is \"([^\"]*)\"$")
    public void offendernoOfOffenderBookingReturnedIs(final String offenderNo) {
        bookingDetail.verifyOffenderNo(offenderNo);
    }

    @And("^activeFlag of offender booking returned is \"(true|false)\"$")
    public void activeflagOfOffenderBookingReturnedIs(final boolean activeFlag) {
        bookingDetail.verifyOffenderActiveFlag(activeFlag);
    }

    @And("^gender matches \"([^\"]*)\"$")
    public void genderMatches(final String gender) {
        bookingDetail.verifyOffenderGender(gender);
    }

    @And("^ethnicity matches \"([^\"]*)\"$")
    public void ethnicityMatches(final String ethnicity) {
        bookingDetail.verifyOffenderEthnicity(ethnicity);
    }

    @And("^height in feet matches \"([^\"]*)\"$")
    public void heightInFeetMatches(final String feet) {
        bookingDetail.verifyOffenderHeightInFeet(isBlank(feet) ? null : Integer.parseInt(feet));
    }

    @And("^height in inches matches \"([^\"]*)\"$")
    public void heightInInchesMatches(final String inches) {
        bookingDetail.verifyOffenderHeightInInches(isBlank(inches) ? null : Integer.parseInt(inches));
    }

    @And("^height in centimetres matches \"([^\"]*)\"$")
    public void heightInCentimetresMatches(final String centimetres) {
        bookingDetail.verifyOffenderHeightInCentimetres(isBlank(centimetres) ? null : Integer.parseInt(centimetres));
    }

    @And("^height in metres matches \"([^\"]*)\"$")
    public void heightInMetresMatches(final String metres) {
        bookingDetail.verifyOffenderHeightInMetres(isBlank(metres) ? null : new BigDecimal(metres));
    }

    @And("^weight in pounds matches \"([^\"]*)\"$")
    public void weightInPoundsMatches(final String pounds) {
        bookingDetail.verifyOffenderWeightInPounds(isBlank(pounds) ? null : Integer.parseInt(pounds));
    }

    @And("^weight in kilograms matches \"([^\"]*)\"$")
    public void weightInKilogramsMatches(final String kilograms) {
        bookingDetail.verifyOffenderWeightInKilograms(isBlank(kilograms) ? null : Integer.parseInt(kilograms));
    }

    @And("^characteristics match \"([^\"]*)\"$")
    public void characteristicsMatch(final String characteristicsList) {
        bookingDetail.verifyOffenderPhysicalCharacteristics(characteristicsList);
    }

    // ----------------------------- Assessments --------------------------

    @And("^the CSRA is \"([^\"]*)\"$")
    public void theCsraIs(final String csra) throws ReflectiveOperationException {
        bookingDetail.verifyField("csra", csra);
    }

    @And("^the category is \"([^\"]*)\"$")
    public void theCategoryIs(final String category) throws ReflectiveOperationException {
        bookingDetail.verifyField("category", category);
    }

    @Then("^bad request response is received from booking assessments API with message \"([^\"]*)\"$")
    public void badRequestResponseIsReceivedFromBookingAssessmentsAPI(final String message) {
        bookingAssessment.verifyBadRequest(message);
    }

    @Then("^access denied response is received from booking assessments API$")
    public void accessDeniedResponseIsReceivedFromAssessmentsAPI() {
        bookingAssessment.verifyAccessDenied();
    }

    @Then("^resource not found response is received from booking assessments API$")
    public void resourceNotFoundResponseIsReceivedFromBookingAssessmentsAPI() {
        bookingAssessment.verifyResourceNotFound();
    }

    @When("^a request is made for uncategorised offenders at \"([^\"]*)\"$")
    public void requestUncategorisedOffenders(final String agencyId) {
        bookingAssessment.getUncategorisedOffenders(agencyId);
    }

    @When("^a request is made for categorised offenders at \"([^\"]*)\" with an approval from Date of \"([^\"]*)\"$")
    public void aRequestIsMadeForCategorisedOffendersAtWithAnApprovalFromDateOf(final String agencyId, final String fromDateString) {
        bookingAssessment.getCategorisedOffenders(agencyId, fromDateString);
    }

    @When("^a request is made for offenders who need to be recategorised at \"([^\"]*)\" with cutoff Date of \"([^\"]*)\"$")
    public void aRequestIsMadeForRecategorisingOffenders(final String agencyId, final String cutoff) {
        bookingAssessment.getRecategorise(agencyId, cutoff);
    }

    @Then("^some uncategorised offenders are returned$")
    public void returnedUncategorisedOffenders() {
        bookingAssessment.verifyOffenderCategoryListNotEmpty();
    }

    @Then("^([0-9]+) categorised offenders are returned$")
    public void returnedCategorisedOffenders(final int size) {
        bookingAssessment.verifyOffenderCategoryListSize(size);
    }

    @Then("^the number of active alerts is ([0-9-]+)$")
    public void theNumberOfActiveAlertsIs(final int count) {
        bookingDetail.verifyActiveCount(count);
    }

    @And("^the number of inactive alerts is ([0-9-]+)$")
    public void theNumberOfInactiveAlertsIs(final int count) {
        bookingDetail.verifyInactiveCount(count);
    }

    @And("^the list of active alert types is \"([^\"]*)\"$")
    public void theListOfActiveAlertTypesIs(final String types) {
        bookingDetail.verifyAlertTypes(types);
    }

    @Then("^\"(\\d+)\" row of assessment data is returned$")
    public void rowOfDataIsReturned(final long expectedCount) {
        bookingAssessment.verifyResourceRecordsReturned(expectedCount);
    }

    @Then("^\"(\\d+)\" row of physical characteristics is returned$")
    public void rowOfPhysicalCharacteristicsIsReturned(final long expectedCount) {
        bookingDetail.verifyResourceRecordsReturned(expectedCount);
    }

    @When("^a categorisation is approved for booking \"([^\"]*)\" with category \"([^\"]*)\" date \"([^\"]*)\" and comment \"([^\"]*)\"$")
    public void aCategorisationApprovalForBookingWithCategory(final String bookingId, final String category, final String date, final String comment) {
        final var localDate = StringUtils.isBlank(date) ? null : LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        final var id = StringUtils.isBlank(bookingId) ? null : Long.parseLong(bookingId);
        bookingAssessment.approveCategorisation(id, StringUtils.trimToNull(category), localDate, StringUtils.trimToNull(comment));
    }

}
