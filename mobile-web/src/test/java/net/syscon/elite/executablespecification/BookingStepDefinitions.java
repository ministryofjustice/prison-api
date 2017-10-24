package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.isBlank;

import net.syscon.elite.executablespecification.steps.*;

/**
 * BDD step definitions for the following Booking API endpoints:
 * <ul>
 *     <li>/booking</li>
 *     <li>/booking/{bookingId}</li>
 *     <li>/booking/{bookingId}/alerts</li>
 *     <li>/booking/{bookingId}/alerts/{alertId}</li>
 *     <li>/booking/{bookingId}/aliases</li>
 *     <li>/bookings/{bookingId}/sentenceDetail</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class BookingStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private BookingSearchSteps bookingSearch;

    @Autowired
    private BookingAliasSteps bookingAlias;

    @Autowired
    private BookingDetailSteps bookingDetail;

    @Autowired
    private BookingSentenceDetailSteps bookingSentenceDetail;

    @Autowired
    private BookingIEPSteps bookingIEPSteps;

    @Autowired
    private BookingAlertSteps bookingAlerts;

    @When("^a booking search is made with full last \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithFullLastNameOfExistingOffender(String fullLastName) throws Throwable {
        bookingSearch.fullLastNameSearch(fullLastName);
    }

    @When("^a booking search is made with partial last \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithPartialLastNameOfExistingOffender(String partialLastName) throws Throwable {
        bookingSearch.partialLastNameSearch(partialLastName);
    }

    @When("^a booking search is made with full first \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithFullFirstNameOfExistingOffender(String fullFirstName) throws Throwable {
        bookingSearch.fullFirstNameSearch(fullFirstName);
    }

    @When("^a booking search is made with partial first \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithPartialFirstNameOfExistingOffender(String partialFirstName) throws Throwable {
        bookingSearch.partialFirstNameSearch(partialFirstName);
    }

    @And("^offender first names match \"([^\"]*)\"$")
    public void offenderFirstNamesMatch(String firstNames) throws Throwable {
        bookingSearch.verifyFirstNames(firstNames);
    }

    @And("^offender middle names match \"([^\"]*)\"$")
    public void offenderMiddleNamesMatch(String middleNames) throws Throwable {
        bookingSearch.verifyMiddleNames(middleNames);
    }

    @When("^a booking search is made without any criteria$")
    public void aBookingSearchIsMadeWithoutAnyCriteria() throws Throwable {
        bookingSearch.findAll();
    }

    @And("^offender last names match \"([^\"]*)\"$")
    public void offenderLastNamesMatch(String lastNames) throws Throwable {
        bookingSearch.verifyLastNames(lastNames);
    }

    @And("^living unit descriptions match \"([^\"]*)\"$")
    public void livingUnitDescriptionsMatch(String livingUnits) throws Throwable {
        bookingSearch.verifyLivingUnits(livingUnits);
    }

    @And("^image id match \"([^\"]*)\"$")
    public void imageIdMatch(String imageIds) throws Throwable {
       bookingSearch.verifyImageIds(imageIds);
    }

    @And("^their dob match \"([^\"]*)\"$")
    public void dateOfBirthMatch(String dobs) throws Throwable {
        bookingSearch.verifyDobs(dobs);
    }

    @When("^a booking search is made with \"([^\"]*)\" and \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithAndOfExistingOffender(String firstName, String lastName) throws Throwable {
        bookingSearch.firstNameAndLastNameSearch(firstName, lastName);
    }

    @When("^a booking search is made with \"([^\"]*)\" or \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithOrOfExistingOffender(String firstName, String lastName) throws Throwable {
        bookingSearch.firstNameOrLastNameSearch(firstName, lastName);
    }

    @Then("^\"([^\"]*)\" booking records are returned$")
    public void bookingRecordsAreReturned(String expectedCount) throws Throwable {
        bookingSearch.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @Then("^\"([^\"]*)\" total booking records are available$")
    public void totalBookingRecordsAreAvailable(String expectedCount) throws Throwable {
        bookingSearch.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @When("^aliases are requested for an offender booking \"([^\"]*)\"$")
    public void aliasesAreRequestedForAnOffenderBooking(String bookingId) throws Throwable {
        bookingAlias.getAliasesForBooking(Long.valueOf(bookingId));
    }

    @Then("^\"([^\"]*)\" aliases are returned$")
    public void aliasesAreReturned(String expectedCount) throws Throwable {
        bookingAlias.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^alias first names match \"([^\"]*)\"$")
    public void aliasFirstNamesMatch(String firstNames) throws Throwable {
        bookingAlias.verifyAliasFirstNames(firstNames);
    }

    @And("^alias last names match \"([^\"]*)\"$")
    public void aliasLastNamesMatch(String lastNames) throws Throwable {
        bookingAlias.verifyAliasLastNames(lastNames);
    }

    @And("^alias ethnicities match \"([^\"]*)\"$")
    public void aliasEthnicitiesMatch(String ethnicities) throws Throwable {
        bookingAlias.verifyAliasEthnicities(ethnicities);
    }

    @When("^an offender booking request is made with booking id \"([^\"]*)\"$")
    public void anOffenderBookingRequestIsMadeWithBookingId(String bookingId) throws Throwable {
        bookingDetail.findBookingDetails(Long.valueOf(bookingId));
    }

    @Then("^resource not found response is received from bookings API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsAPI() throws Throwable {
        bookingDetail.verifyResourceNotFound();
    }

    @Then("^booking number of offender booking returned is \"([^\"]*)\"$")
    public void bookingNumberOfOffenderBookingReturnedIs(String bookingNo) throws Throwable {
        bookingDetail.verifyOffenderBookingNo(bookingNo);
    }

    @And("^assigned officer id of offender booking returned is \"([^\"]*)\"$")
    public void assignedOfficerIdOfOffenderBookingReturnedIs(Long assignedOfficerId) throws Throwable {
        bookingDetail.verifyOffenderAssignedOfficerId(assignedOfficerId);
    }

    // Sentence Detail Step Definitions (for testing of /bookings/{bookingId}/sentenceDetail endpoint)
    @When("^sentence details are requested for an offender with booking id \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingSentenceDetail.getBookingSentenceDetail(Long.valueOf(bookingId));
    }

    @Then("^sentence start date matches \"([^\"]*)\"$")
    public void sentenceStartDateMatches(String sentenceStartDate) throws Throwable {
        bookingSentenceDetail.verifySentenceStartDate(sentenceStartDate);
    }

    @And("^sentence expiry date matches \"([^\"]*)\"$")
    public void sentenceExpiryDateMatches(String sentenceExpiryDate) throws Throwable {
        bookingSentenceDetail.verifySentenceExpiryDate(sentenceExpiryDate);
    }

    @And("^early term date matches \"([^\"]*)\"$")
    public void earlyTermDateMatches(String earlyTermDate) throws Throwable {
        bookingSentenceDetail.verifyEarlyTermDate(earlyTermDate);
    }

    @And("^mid term date matches \"([^\"]*)\"$")
    public void midTermDateMatches(String midTermDate) throws Throwable {
        bookingSentenceDetail.verifyMidTermDate(midTermDate);
    }

    @And("^late term date matches \"([^\"]*)\"$")
    public void lateTermDateMatches(String lateTermDate) throws Throwable {
        bookingSentenceDetail.verifyLateTermDate(lateTermDate);
    }

    @And("^automatic release date matches \"([^\"]*)\"$")
    public void automaticReleaseDateMatches(String automaticReleaseDate) throws Throwable {
        bookingSentenceDetail.verifyAutomaticReleaseDate(automaticReleaseDate);
    }

    @And("^override automatic release date matches \"([^\"]*)\"$")
    public void automaticOverrideReleaseDateMatches(String overrideAutomaticReleaseDate) throws Throwable {
        bookingSentenceDetail.verifyOverrideAutomaticReleaseDate(overrideAutomaticReleaseDate);
    }

    @And("^conditional release date matches \"([^\"]*)\"$")
    public void conditionalReleaseDateMatches(String conditionalReleaseDate) throws Throwable {
        bookingSentenceDetail.verifyConditionalReleaseDate(conditionalReleaseDate);
    }

    @And("^override conditional release date matches \"([^\"]*)\"$")
    public void conditionalOverrideReleaseDateMatches(String overrideConditionalReleaseDate) throws Throwable {
        bookingSentenceDetail.verifyOverrideConditionalReleaseDate(overrideConditionalReleaseDate);
    }

    @And("^non-parole date matches \"([^\"]*)\"$")
    public void nonParoleDateMatches(String nonParoleDate) throws Throwable {
        bookingSentenceDetail.verifyNonParoleDate(nonParoleDate);
    }

    @And("^override non-parole date matches \"([^\"]*)\"$")
    public void nonParoleOverrideDateMatches(String overrideNonParoleDate) throws Throwable {
        bookingSentenceDetail.verifyOverrideNonParoleDate(overrideNonParoleDate);
    }

    @And("^post-recall release date matches \"([^\"]*)\"$")
    public void postRecallReleaseDateMatches(String postRecallReleaseDate) throws Throwable {
        bookingSentenceDetail.verifyPostRecallReleaseDate(postRecallReleaseDate);
    }

    @And("^override post-recall release date matches \"([^\"]*)\"$")
    public void postRecallOverrideReleaseDateMatches(String overridePostRecallReleaseDate) throws Throwable {
        bookingSentenceDetail.verifyOverridePostRecallReleaseDate(overridePostRecallReleaseDate);
    }

    @And("^home detention curfew eligibility date matches \"([^\"]*)\"$")
    public void homeDetentionCurfewEligibilityDateMatches(String homeDetentionCurfewEligibilityDate) throws Throwable {
        bookingSentenceDetail.verifyHomeDetentionCurfewEligibilityDate(homeDetentionCurfewEligibilityDate);
    }

    @And("^parole eligibility date matches \"([^\"]*)\"$")
    public void paroleEligibilityDateMatches(String paroleEligibilityDate) throws Throwable {
        bookingSentenceDetail.verifyParoleEligibilityDate(paroleEligibilityDate);
    }

    @And("^licence expiry date matches \"([^\"]*)\"$")
    public void licenceExpiryDateMatches(String licenceExpiryDate) throws Throwable {
        bookingSentenceDetail.verifyLicenceExpiryDate(licenceExpiryDate);
    }

    @And("^release date matches \"([^\"]*)\"$")
    public void releaseDateMatches(String releaseDate) throws Throwable {
        bookingSentenceDetail.verifyNonDtoReleaseDate(releaseDate);
    }

    @And("^additional days awarded matches \"([^\"]*)\"$")
    public void additionalDaysAwardedMatches(String additionalDaysAwarded) throws Throwable {
        bookingSentenceDetail.verifyAdditionalDaysAwarded(
                isBlank(additionalDaysAwarded) ? null : Integer.valueOf(additionalDaysAwarded));
    }

    @And("^release date type matches \"([^\"]*)\"$")
    public void releaseDateTypeMatches(String releaseDateType) throws Throwable {
        bookingSentenceDetail.verifyNonDtoReleaseDateType(releaseDateType);
    }

    @And("^home detention curfew approved date matches \"([^\"]*)\"$")
    public void homeDetentionCurfewApprovedDateMatches(String homeDetentionCurfewApprovedDate) throws Throwable {
        bookingSentenceDetail.verifyHomeDetentionCurfewApprovedDate(homeDetentionCurfewApprovedDate);
    }

    @And("^approved parole date matches \"([^\"]*)\"$")
    public void approvedParoleDateMatches(String approvedParoleDate) throws Throwable {
        bookingSentenceDetail.verifyApprovedParoleDate(approvedParoleDate);
    }

    @And("^release on temporary licence date matches \"([^\"]*)\"$")
    public void releaseOnTemporaryLicenceDateMatches(String releaseOnTemporaryLicenceDate) throws Throwable {
        bookingSentenceDetail.verifyReleaseOnTemporaryLicenceDate(releaseOnTemporaryLicenceDate);
    }

    @And("^early release scheme eligibility date matches \"([^\"]*)\"$")
    public void earlyReleaseSchemeEligibilityDateMatches(String earlyReleaseSchemeEligibilityDate) throws Throwable {
        bookingSentenceDetail.verifyEarlyReleaseSchemeEligibilityDate(earlyReleaseSchemeEligibilityDate);
    }

    @When("^sentence details with nonexistent booking id is requested$")
    public void aNonexistentIdIsRequested() {
        bookingSentenceDetail.getNonexistentSentenceDetails();
    }

    @When("^sentence details with booking id in different caseload is requested$")
    public void anIdInDifferentCaseloadIsRequested() {
        bookingSentenceDetail.getSentenceDetailsInDifferentCaseload();
    }

    @Then("^resource not found response is received from sentence details API$")
    public void resourceNotFoundResponseIsReceivedFromSentenceDetailsAPI() throws Throwable {
        bookingSentenceDetail.verifyResourceNotFound();
    }

    @When("^an IEP summary only is requested for an offender with booking id \"([^\"]*)\"$")
    public void anIEPSummaryOnlyIsRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingIEPSteps.getBookingIEPSummary(Long.valueOf(bookingId), false);
    }

    @When("^an IEP summary, with details, is requested for an offender with booking id \"([^\"]*)\"$")
    public void anIEPSummaryWithDetailsIsRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingIEPSteps.getBookingIEPSummary(Long.valueOf(bookingId), true);
    }

    @Then("^IEP summary is returned with IEP level of \"([^\"]*)\"$")
    public void iepSummaryIsReturnedWithIEPLevelOf(String iepLevel) throws Throwable {
        bookingIEPSteps.verifyCurrentIEPLevel(iepLevel);
    }

    @And("^IEP summary contains \"([^\"]*)\" detail records$")
    public void iepSummaryContainsDetailRecords(String detailRecordCount) throws Throwable {
        bookingIEPSteps.verifyIEPDetailRecordCount(Integer.parseInt(detailRecordCount));
    }

    @And("^IEP days since review is correct for IEP date of \"([^\"]*)\"$")
    public void iepDaysSinceReviewIsCorrectForIEPDateOf(String iepDate) throws Throwable {
        bookingIEPSteps.verifyDaysSinceReview(iepDate);
    }

    @Then("^resource not found response is received from bookings IEP summary API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsIEPSummaryAPI() throws Throwable {
        bookingIEPSteps.verifyResourceNotFound();
    }

    @And("^gender matches \"([^\"]*)\"$")
    public void genderMatches(String gender) throws Throwable {
        bookingDetail.verifyOffenderGender(gender);
    }

    @And("^ethnicity matches \"([^\"]*)\"$")
    public void ethnicityMatches(String ethnicity) throws Throwable {
        bookingDetail.verifyOffenderEthnicity(ethnicity);
    }

    @And("^height in feet matches \"([^\"]*)\"$")
    public void heightInFeetMatches(String feet) throws Throwable {
        bookingDetail.verifyOffenderHeightInFeet(isBlank(feet) ? null : Integer.parseInt(feet));
    }

    @And("^height in inches matches \"([^\"]*)\"$")
    public void heightInInchesMatches(String inches) throws Throwable {
        bookingDetail.verifyOffenderHeightInInches(isBlank(inches) ? null : Integer.parseInt(inches));
    }

    @And("^height in centimetres matches \"([^\"]*)\"$")
    public void heightInCentimetresMatches(String centimetres) throws Throwable {
        bookingDetail.verifyOffenderHeightInCentimetres(isBlank(centimetres) ? null : Integer.parseInt(centimetres));
    }

    @And("^height in metres matches \"([^\"]*)\"$")
    public void heightInMetresMatches(String metres) throws Throwable {
        bookingDetail.verifyOffenderHeightInMetres(isBlank(metres) ? null : new BigDecimal(metres));
    }

    @And("^weight in pounds matches \"([^\"]*)\"$")
    public void weightInPoundsMatches(String pounds) throws Throwable {
        bookingDetail.verifyOffenderWeightInPounds(isBlank(pounds) ? null : Integer.parseInt(pounds));
    }

    @And("^weight in kilograms matches \"([^\"]*)\"$")
    public void weightInKilogramsMatches(String kilograms) throws Throwable {
        bookingDetail.verifyOffenderWeightInKilograms(isBlank(kilograms) ? null : Integer.parseInt(kilograms));
    }

    @And("^characteristics match \"([^\"]*)\"$")
    public void characteristicsMatch(String characteristicsList) throws Throwable {
        bookingDetail.verifyOffenderPhysicalCharacteristics(characteristicsList);
    }

    @When("^alerts are requested for an offender booking \"([^\"]*)\"$")
    public void alertsAreRequestedForOffenderBooking(Long bookingId) throws Throwable {
        bookingAlerts.getAlerts(bookingId);
    }

    @Then("^\"([^\"]*)\" alerts are returned$")
    public void numberAlertsAreReturned(int number) throws Throwable {
        bookingAlerts.verifyNumber(number);
    }

    @And("alerts codes match ^\"([^\"]*)\"$")
    public void alertsCodesMatch(String codes) throws Throwable {
        bookingAlerts.verifyCodeList(codes);
    }

    @When("^alert is requested for an offender booking \"([^\"]*)\" and alert id \"([^\"]*)\"$")
    public void alertIsRequestedForOffenderBooking(Long bookingId, Long alertId) throws Throwable {
        bookingAlerts.getAlert(bookingId, alertId);
    }

    @Then("^alert ^\"([^\"]*)\" is ^\"([^\"]*)\"$")
    public void alertValueIs(String field, String value) throws Throwable {
        bookingAlerts.verifyAlertField(field, value);
    }

  /*  @And("alert Type is ^\"([^\"]*)\"$")
    public void alertTypeIs(String value) throws Throwable {
        BookingAlerts.verifyAlertType(value);
    }

    @And("alert Type Description is ^\"([^\"]*)\"$")
    public void alertTypeDescriptionIs(String value) throws Throwable {
        BookingAlerts.verifyAlertTypeDescription(value);
    }

    @And("alert Code is ^\"([^\"]*)\"$")
    public void alertCodeIs(String value) throws Throwable {
        BookingAlerts.verifyAlertCode(value);
    }

    @And("alert Code Description is ^\"([^\"]*)\"$")
    public void alertCodeDescriptionIs(String value) throws Throwable {
        BookingAlerts.verifyAlertCodeDescription(value);
    }

    @And("alert comment is ^\"([^\"]*)\"$")
    public void alertCommentIs(String value) throws Throwable {
        BookingAlerts.verifyAlertComment(value);
    }

    @And("alert date Created is ^\"([^\"]*)\"$")
    public void alertDateCreatedIs(String value) throws Throwable {
        BookingAlerts.verifyAlertDateCreated(value);
    }

    @And("alert date Expires is ^\"([^\"]*)\"$")
    public void alertDateExpiresIs(String value) throws Throwable {
        BookingAlerts.verifyAlertDateExpires(value);
    }

    @And("alert expired is ^\"([^\"]*)\"$")
    public void alertExpiredIs(Boolean value) throws Throwable {
        BookingAlerts.verifyAlertExpired(value);
    }*/
}
