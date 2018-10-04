package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * BDD step definitions for the following Booking API endpoints:
 * <ul>
 *     <li>/booking</li>
 *     <li>/booking/{bookingId}</li>
 *     <li>/booking/{bookingId}/alerts</li>
 *     <li>/booking/{bookingId}/alerts/{alertId}</li>
 *     <li>/booking/{bookingId}/aliases</li>
 *     <li>/bookings/{bookingId}/sentenceDetail</li>
 *     <li>/bookings/{bookingId}/balances</li>
 *     <li>/bookings/{bookingId}/mainSentence</li>
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
    private BookingIEPSteps bookingIEP;

    @Autowired
    private BookingAlertSteps bookingAlerts;

    @Autowired
    private BookingSentenceSteps bookingSentence;

    @Autowired
    private BookingAssessmentSteps bookingAssessment;

    @When("^a booking search is made with full last \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithFullLastNameOfExistingOffender(String fullLastName) {
        bookingSearch.fullLastNameSearch(fullLastName);
    }

    @When("^a booking search is made with partial last \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithPartialLastNameOfExistingOffender(String partialLastName) {
        bookingSearch.partialLastNameSearch(partialLastName);
    }

    @When("^a booking search is made with full first \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithFullFirstNameOfExistingOffender(String fullFirstName) {
        bookingSearch.fullFirstNameSearch(fullFirstName);
    }

    @When("^a booking search is made with partial first \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithPartialFirstNameOfExistingOffender(String partialFirstName) {
        bookingSearch.partialFirstNameSearch(partialFirstName);
    }

    @And("^offender first names match \"([^\"]*)\"$")
    public void offenderFirstNamesMatch(String firstNames) {
        bookingSearch.verifyFirstNames(firstNames);
    }

    @And("^offender middle names match \"([^\"]*)\"$")
    public void offenderMiddleNamesMatch(String middleNames) {
        bookingSearch.verifyMiddleNames(middleNames);
    }

    @When("^a booking search is made without any criteria$")
    public void aBookingSearchIsMadeWithoutAnyCriteria() {
        bookingSearch.findAll();
    }

    @And("^offender last names match \"([^\"]*)\"$")
    public void offenderLastNamesMatch(String lastNames) {
        bookingSearch.verifyLastNames(lastNames);
    }

    @And("^living unit descriptions match \"([^\"]*)\"$")
    public void livingUnitDescriptionsMatch(String livingUnits) {
        bookingSearch.verifyLivingUnits(livingUnits);
    }

    @And("^image id match \"([^\"]*)\"$")
    public void imageIdMatch(String imageIds) {
       bookingSearch.verifyImageIds(imageIds);
    }

    @And("^their dob match \"([^\"]*)\"$")
    public void dateOfBirthMatch(String dobs) {
        bookingSearch.verifyDobs(dobs);
    }

    @When("^a booking search is made with \"([^\"]*)\" and \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithAndOfExistingOffender(String firstName, String lastName) {
        bookingSearch.firstNameAndLastNameSearch(firstName, lastName);
    }

    @When("^a booking search is made with \"([^\"]*)\" or \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithOrOfExistingOffender(String firstName, String lastName) {
        bookingSearch.firstNameOrLastNameSearch(firstName, lastName);
    }

    @Then("^\"([^\"]*)\" booking records are returned$")
    public void bookingRecordsAreReturned(String expectedCount) {
        bookingSearch.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @Then("^\"([^\"]*)\" total booking records are available$")
    public void totalBookingRecordsAreAvailable(String expectedCount) {
        bookingSearch.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @When("^aliases are requested for an offender booking \"([^\"]*)\"$")
    public void aliasesAreRequestedForAnOffenderBooking(String bookingId) {
        bookingAlias.getAliasesForBooking(Long.valueOf(bookingId));
    }

    @Then("^\"([^\"]*)\" aliases are returned$")
    public void aliasesAreReturned(String expectedCount) {
        bookingAlias.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^alias first names match \"([^\"]*)\"$")
    public void aliasFirstNamesMatch(String firstNames) {
        bookingAlias.verifyAliasFirstNames(firstNames);
    }

    @And("^alias last names match \"([^\"]*)\"$")
    public void aliasLastNamesMatch(String lastNames) {
        bookingAlias.verifyAliasLastNames(lastNames);
    }

    @And("^alias ethnicities match \"([^\"]*)\"$")
    public void aliasEthnicitiesMatch(String ethnicities) {
        bookingAlias.verifyAliasEthnicities(ethnicities);
    }

    @Then("^resource not found response is received from offender aliases API$")
    public void resourceNotFoundResponseIsReceivedFromOffenderAliasesAPI() {
        bookingAlias.verifyResourceNotFound();
    }

    @When("^an offender booking request is made with booking id \"([^\"]*)\"$")
    public void anOffenderBookingRequestIsMadeWithBookingId(String bookingId) {
        bookingDetail.findBookingDetails(Long.valueOf(bookingId), false);
    }

    @When("^a basic offender booking request is made with booking id \"([^\"]*)\"$")
    public void aBasicOffenderBookingRequestIsMadeWithBookingId(String bookingId) {
        bookingDetail.findBookingDetails(Long.valueOf(bookingId), true);
    }

    @Then("^resource not found response is received from bookings API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsAPI() {
        bookingDetail.verifyResourceNotFound();
    }

    @Then("^booking number of offender booking returned is \"([^\"]*)\"$")
    public void bookingNumberOfOffenderBookingReturnedIs(String bookingNo) {
        bookingDetail.verifyOffenderBookingNo(bookingNo);
    }

    @And("^assigned officer id of offender booking returned is \"([^\"]*)\"$")
    public void assignedOfficerIdOfOffenderBookingReturnedIs(Long assignedOfficerId) {
        bookingDetail.verifyOffenderAssignedOfficerId(assignedOfficerId);
    }

    @And("^language of offender booking returned is \"([^\"]*)\"$")
    public void languageOfOffenderBookingReturnedIs(String language) throws ReflectiveOperationException {
        bookingDetail.verifyLanguage(language);
    }

    @And("^firstname of offender booking returned is \"([^\"]*)\"$")
    public void firstnameOfOffenderBookingReturnedIs(String firstname) {
        bookingDetail.verifyOffenderFirstName(firstname);
    }

    @And("^lastName of offender booking returned is \"([^\"]*)\"$")
    public void lastnameOfOffenderBookingReturnedIs(String lastName) {
        bookingDetail.verifyOffenderLastName(lastName);
    }

    @And("^offenderNo of offender booking returned is \"([^\"]*)\"$")
    public void offendernoOfOffenderBookingReturnedIs(String offenderNo) {
        bookingDetail.verifyOffenderNo(offenderNo);
    }

    @And("^activeFlag of offender booking returned is \"(true|false)\"$")
    public void activeflagOfOffenderBookingReturnedIs(boolean activeFlag) {
        bookingDetail.verifyOffenderActiveFlag(activeFlag);
    }

    // Sentence Detail Step Definitions (for testing of /bookings/{bookingId}/sentenceDetail endpoint)
    @When("^sentence details are requested for an offender with booking id \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForAnOffenderWithBookingId(String bookingId) {
        bookingSentenceDetail.getBookingSentenceDetail(Long.valueOf(bookingId));
    }

    @When("^I look at row \"([^\"]*)\"$")
    public void iLookAtRow(String index) {
        bookingSentenceDetail.putARowFromListInContext(Integer.valueOf(index)-1);
    }

    @Then("^sentence start date matches \"([^\"]*)\"$")
    public void sentenceStartDateMatches(String sentenceStartDate) {
        bookingSentenceDetail.verifySentenceStartDate(sentenceStartDate);
    }

    @And("^sentence expiry date matches \"([^\"]*)\"$")
    public void sentenceExpiryDateMatches(String sentenceExpiryDate) {
        bookingSentenceDetail.verifySentenceExpiryDate(sentenceExpiryDate);
    }

    @And("^early term date matches \"([^\"]*)\"$")
    public void earlyTermDateMatches(String earlyTermDate) {
        bookingSentenceDetail.verifyEarlyTermDate(earlyTermDate);
    }

    @And("^mid term date matches \"([^\"]*)\"$")
    public void midTermDateMatches(String midTermDate) {
        bookingSentenceDetail.verifyMidTermDate(midTermDate);
    }

    @And("^late term date matches \"([^\"]*)\"$")
    public void lateTermDateMatches(String lateTermDate) {
        bookingSentenceDetail.verifyLateTermDate(lateTermDate);
    }

    @And("^automatic release date matches \"([^\"]*)\"$")
    public void automaticReleaseDateMatches(String automaticReleaseDate) {
        bookingSentenceDetail.verifyAutomaticReleaseDate(automaticReleaseDate);
    }

    @And("^override automatic release date matches \"([^\"]*)\"$")
    public void automaticOverrideReleaseDateMatches(String overrideAutomaticReleaseDate) {
        bookingSentenceDetail.verifyOverrideAutomaticReleaseDate(overrideAutomaticReleaseDate);
    }

    @And("^conditional release date matches \"([^\"]*)\"$")
    public void conditionalReleaseDateMatches(String conditionalReleaseDate) {
        bookingSentenceDetail.verifyConditionalReleaseDate(conditionalReleaseDate);
    }

    @And("^override conditional release date matches \"([^\"]*)\"$")
    public void conditionalOverrideReleaseDateMatches(String overrideConditionalReleaseDate) {
        bookingSentenceDetail.verifyOverrideConditionalReleaseDate(overrideConditionalReleaseDate);
    }

    @And("^non-parole date matches \"([^\"]*)\"$")
    public void nonParoleDateMatches(String nonParoleDate) {
        bookingSentenceDetail.verifyNonParoleDate(nonParoleDate);
    }

    @And("^override non-parole date matches \"([^\"]*)\"$")
    public void nonParoleOverrideDateMatches(String overrideNonParoleDate) {
        bookingSentenceDetail.verifyOverrideNonParoleDate(overrideNonParoleDate);
    }

    @And("^post-recall release date matches \"([^\"]*)\"$")
    public void postRecallReleaseDateMatches(String postRecallReleaseDate) {
        bookingSentenceDetail.verifyPostRecallReleaseDate(postRecallReleaseDate);
    }

    @And("^override post-recall release date matches \"([^\"]*)\"$")
    public void postRecallOverrideReleaseDateMatches(String overridePostRecallReleaseDate) {
        bookingSentenceDetail.verifyOverridePostRecallReleaseDate(overridePostRecallReleaseDate);
    }

    @And("^home detention curfew eligibility date matches \"([^\"]*)\"$")
    public void homeDetentionCurfewEligibilityDateMatches(String homeDetentionCurfewEligibilityDate) {
        bookingSentenceDetail.verifyHomeDetentionCurfewEligibilityDate(homeDetentionCurfewEligibilityDate);
    }

    @And("^parole eligibility date matches \"([^\"]*)\"$")
    public void paroleEligibilityDateMatches(String paroleEligibilityDate) {
        bookingSentenceDetail.verifyParoleEligibilityDate(paroleEligibilityDate);
    }

    @And("^licence expiry date matches \"([^\"]*)\"$")
    public void licenceExpiryDateMatches(String licenceExpiryDate) {
        bookingSentenceDetail.verifyLicenceExpiryDate(licenceExpiryDate);
    }

    @And("^non-DTO release date matches \"([^\"]*)\"$")
    public void nonDtoReleaseDateMatches(String releaseDate) {
        bookingSentenceDetail.verifyNonDtoReleaseDate(releaseDate);
    }

    @And("^additional days awarded matches \"([^\"]*)\"$")
    public void additionalDaysAwardedMatches(String additionalDaysAwarded) {
        bookingSentenceDetail.verifyAdditionalDaysAwarded(
                isBlank(additionalDaysAwarded) ? null : Integer.valueOf(additionalDaysAwarded));
    }

    @And("^non-DTO release date type matches \"([^\"]*)\"$")
    public void nonDtoReleaseDateTypeMatches(String releaseDateType) {
        bookingSentenceDetail.verifyNonDtoReleaseDateType(releaseDateType);
    }

    @And("^home detention curfew actual date matches \"([^\"]*)\"$")
    public void homeDetentionCurfewActualDateMatches(String homeDetentionCurfewActualDate) {
        bookingSentenceDetail.verifyHomeDetentionCurfewActualDate(homeDetentionCurfewActualDate);
    }

    @And("^actual parole date matches \"([^\"]*)\"$")
    public void actualParoleDateMatches(String actualParoleDate) {
        bookingSentenceDetail.verifyActualParoleDate(actualParoleDate);
    }

    @And("^release on temporary licence date matches \"([^\"]*)\"$")
    public void releaseOnTemporaryLicenceDateMatches(String releaseOnTemporaryLicenceDate) {
        bookingSentenceDetail.verifyReleaseOnTemporaryLicenceDate(releaseOnTemporaryLicenceDate);
    }

    @And("^early removal scheme eligibility date matches \"([^\"]*)\"$")
    public void earlyRemovalSchemeEligibilityDateMatches(String earlyRemovalSchemeEligibilityDate) {
        bookingSentenceDetail.verifyEarlyRemovalSchemeEligibilityDate(earlyRemovalSchemeEligibilityDate);
    }

    @Then("^resource not found response is received from sentence details API$")
    public void resourceNotFoundResponseIsReceivedFromSentenceDetailsAPI() {
        bookingSentenceDetail.verifyResourceNotFound();
    }

    @And("^confirmed release date matches \"([^\"]*)\"$")
    public void confirmedReleaseDateMatches(String confirmedReleaseDate) {
        bookingSentenceDetail.verifyConfirmedReleaseDate(confirmedReleaseDate);
    }

    @And("^topup supervision expiry date matches \"([^\"]*)\"$")
    public void topupSupervisionExpiryDateMatches(String topupSupervisionExpiryDate) {
        bookingSentenceDetail.verifyTopupSupervisionExpiryDate(topupSupervisionExpiryDate);
    }

    @And("^release date matches \"([^\"]*)\"$")
    public void releaseDateMatches(String releaseDate) {
        bookingSentenceDetail.verifyReleaseDate(releaseDate);
    }

    @And("^tariff date matches \"([^\"]*)\"$")
    public void tariffDateMatches(String tariffDate) {
        bookingSentenceDetail.verifyTariffDate(tariffDate);
    }

    @When("^an IEP summary only is requested for an offender with booking id \"([^\"]*)\"$")
    public void anIEPSummaryOnlyIsRequestedForAnOffenderWithBookingId(String bookingId) {
        bookingIEP.getBookingIEPSummary(Long.valueOf(bookingId), false);
    }

    @When("^an IEP summary, with details, is requested for an offender with booking id \"([^\"]*)\"$")
    public void anIEPSummaryWithDetailsIsRequestedForAnOffenderWithBookingId(String bookingId) {
        bookingIEP.getBookingIEPSummary(Long.valueOf(bookingId), true);
    }

    @Then("^IEP summary is returned with IEP level of \"([^\"]*)\"$")
    public void iepSummaryIsReturnedWithIEPLevelOf(String iepLevel) {
        bookingIEP.verifyCurrentIEPLevel(iepLevel);
    }

    @And("^IEP summary contains \"([^\"]*)\" detail records$")
    public void iepSummaryContainsDetailRecords(String detailRecordCount) {
        bookingIEP.verifyIEPDetailRecordCount(Integer.parseInt(detailRecordCount));
    }

    @And("^IEP days since review is correct for IEP date of \"([^\"]*)\"$")
    public void iepDaysSinceReviewIsCorrectForIEPDateOf(String iepDate) {
        bookingIEP.verifyDaysSinceReview(iepDate);
    }

    @Then("^resource not found response is received from bookings IEP summary API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsIEPSummaryAPI() {
        bookingIEP.verifyResourceNotFound();
    }

    @And("^user message in resource not found response from bookings IEP summary API is \"([^\"]*)\"$")
    public void userMessageInResourceNotFoundResponseFromBookingsIEPSummaryAPIIs(String expectedUserMessage) {
        bookingIEP.verifyErrorUserMessage(expectedUserMessage);
    }

    @And("^gender matches \"([^\"]*)\"$")
    public void genderMatches(String gender) {
        bookingDetail.verifyOffenderGender(gender);
    }

    @And("^ethnicity matches \"([^\"]*)\"$")
    public void ethnicityMatches(String ethnicity) {
        bookingDetail.verifyOffenderEthnicity(ethnicity);
    }

    @And("^height in feet matches \"([^\"]*)\"$")
    public void heightInFeetMatches(String feet) {
        bookingDetail.verifyOffenderHeightInFeet(isBlank(feet) ? null : Integer.parseInt(feet));
    }

    @And("^height in inches matches \"([^\"]*)\"$")
    public void heightInInchesMatches(String inches) {
        bookingDetail.verifyOffenderHeightInInches(isBlank(inches) ? null : Integer.parseInt(inches));
    }

    @And("^height in centimetres matches \"([^\"]*)\"$")
    public void heightInCentimetresMatches(String centimetres) {
        bookingDetail.verifyOffenderHeightInCentimetres(isBlank(centimetres) ? null : Integer.parseInt(centimetres));
    }

    @And("^height in metres matches \"([^\"]*)\"$")
    public void heightInMetresMatches(String metres) {
        bookingDetail.verifyOffenderHeightInMetres(isBlank(metres) ? null : new BigDecimal(metres));
    }

    @And("^weight in pounds matches \"([^\"]*)\"$")
    public void weightInPoundsMatches(String pounds) {
        bookingDetail.verifyOffenderWeightInPounds(isBlank(pounds) ? null : Integer.parseInt(pounds));
    }

    @And("^weight in kilograms matches \"([^\"]*)\"$")
    public void weightInKilogramsMatches(String kilograms) {
        bookingDetail.verifyOffenderWeightInKilograms(isBlank(kilograms) ? null : Integer.parseInt(kilograms));
    }

    @And("^characteristics match \"([^\"]*)\"$")
    public void characteristicsMatch(String characteristicsList) {
        bookingDetail.verifyOffenderPhysicalCharacteristics(characteristicsList);
    }

    // ----------------------------- Alerts --------------------------

    @When("^alerts are requested for an offender booking \"([^\"]*)\"$")
    public void alertsAreRequestedForOffenderBooking(Long bookingId) {
        bookingAlerts.getAlerts(bookingId);
    }

    @Then("^\"([^\"]*)\" alerts are returned$")
    public void numberAlertsAreReturned(String expectedCount) {
        bookingAlerts.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("alerts codes match \"([^\"]*)\"$")
    public void alertsCodesMatch(String codes) {
        bookingAlerts.verifyCodeList(codes);
    }

    @When("^alert is requested for an offender booking \"([^\"]*)\" and alert id \"([^\"]*)\"$")
    public void alertIsRequestedForOffenderBooking(Long bookingId, Long alertId) {
        bookingAlerts.getAlert(bookingId, alertId);
    }

    @Then("^alert (\\w+) is \"([^\"]*)\"$")
    public void alertValueIs(String field, String value) throws Throwable {
        bookingAlerts.verifyAlertField(field, value);
    }

    @Then("^resource not found response is received from alert API$")
    public void resourceNotFoundResponseIsReceivedFromAlertAPI() {
        bookingAlerts.verifyResourceNotFound();
    }

    // ----------------------------- Main Sentence --------------------------
    @When("^a sentence with booking id ([0-9-]+) is requested$")
    public void sentenceWithBookingId(Long bookingId) {
        bookingSentence.getMainOffenceDetails(bookingId);
    }

    @Then("^(\\d+) offence detail records are returned$")
    public void offenceDetailRecordsAreReturned(long expectedCount) {
        bookingSentence.verifyResourceRecordsReturned(expectedCount);
    }

    @And("^offence description of \"([^\"]*)\" offence detail record is \"([^\"]*)\"$")
    public void offenceDescriptionOfOffenceDetailRecordIs(String ordinal, String expectedDescription) {
        bookingSentence.verifyOffenceDescription(ord2idx(ordinal), expectedDescription);
    }

    @Then("resource not found response is received from sentence API")
    public void resourceNotFoundResponse() {
        bookingSentence.verifyResourceNotFound();
    }

    // ----------------------------- Assessments --------------------------
    @When("^an offender booking assessment information request is made with booking id ([0-9-]+) and \"([^\"]*)\"$")
    public void anOffenderBookingAssessmentInformationRequestIsMadeWithBookingIdAnd(Long bookingId, String assessmentCode) {
        bookingAssessment.getAssessmentByCode(bookingId, assessmentCode);
    }

    @Then("^the classification is \"([^\"]*)\"$")
    public void theClassificationIsCorrect(String classification) throws Throwable {
        bookingAssessment.verifyField("classification", classification);
    }

    @And("^the Cell Sharing Alert is (true|false)$")
    public void theCellSharingAlertIs(boolean csra) {
        bookingAssessment.verifyCsra(csra);
    }

    @And("^the Next Review Date is \"([^\"]*)\"$")
    public void theNextReviewDateIs(String nextReviewDate) {
        bookingAssessment.verifyNextReviewDate(nextReviewDate);
    }

    @And("^the CSRA is \"([^\"]*)\"$")
    public void theCsraIs(String csra) throws ReflectiveOperationException {
        bookingDetail.verifyField("csra", csra);
    }

    @And("^the category is \"([^\"]*)\"$")
    public void theCategoryIs(String category) throws ReflectiveOperationException {
        bookingDetail.verifyField("category", category);
    }

    @When("^an offender booking assessment information request is made with offender numbers \"([^\"]*)\" and \"([^\"]*)\"$")
    public void anOffenderBookingAssessmentInformationRequestIsMadeWithBookingIdAnd(String offenderNoList, String assessmentCode) {
        bookingAssessment.getAssessmentsByCode(offenderNoList, assessmentCode);
    }

    @When("^an offender booking assessment information POST request is made with offender numbers \"([^\"]*)\" and \"([^\"]*)\"$")
    public void anOffenderBookingAssessmentInformationRequestIsMadeUsingPost(String offenderNoList, String assessmentCode) {
        bookingAssessment.getAssessmentsByCodeUsingPost(offenderNoList, assessmentCode);
    }

    @When("^an offender booking CSRA information POST request is made with offender numbers \"([^\"]*)\"$")
    public void anOffenderBookingCSRAInformationRequestIsMadeUsingPost(String offenderNoList) {
        bookingAssessment.getCsrasUsingPost(offenderNoList);
    }

    @Then("^bad request response is received from booking assessments API$")
    public void badRequestResponseIsReceivedFromBookingAssessmentsAPI() {
        bookingAssessment.verifyBadRequest("List of Offender Ids must be provided");
    }

    @Then("^correct results are returned as for single assessment$")
    public void multipleIsCorrect() {
        bookingAssessment.verifyMultipleAssessments();
    }

    @Then("^resource not found response is received from booking assessments API$")
    public void resourceNotFoundResponseIsReceivedFromBookingAssessmentsAPI() {
        bookingAssessment.verifyResourceNotFound();
    }

    @And("^user message in resource not found response from booking assessments API is \"([^\"]*)\"$")
    public void userMessageInResourceNotFoundResponseFromBookingAssessmentsAPIIs(String expectedUserMessage) {
        bookingAssessment.verifyErrorUserMessage(expectedUserMessage);
    }

    @Then("^the number of active alerts is ([0-9-]+)$")
    public void theNumberOfActiveAlertsIs(int count) {
        bookingDetail.verifyActiveCount(count);
    }

    @And("^the number of inactive alerts is ([0-9-]+)$")
    public void theNumberOfInactiveAlertsIs(int count) {
        bookingDetail.verifyInactiveCount(count);
    }

    @And("^the list of active alert types is \"([^\"]*)\"$")
    public void theListOfActiveAlertTypesIs(String types) {
        bookingDetail.verifyAlertTypes(types);
    }

    @When("^sentence details are requested for an offenders in logged in users caseloads with offender No \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForAnOffendersInLoggedInUsersCaseloadsWithBookingId(String offenderNos) {
        bookingSentenceDetail.getOffenderSentenceDetails(offenderNos, null);
    }

    @When("^sentence details are requested by a POST request for offender Nos \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedByPostForOffenderNos(String offenderNos) {
        bookingSentenceDetail.getOffenderSentenceDetailsUsingPostRequest(offenderNos);
    }

    @When("^sentence details are requested by a POST request for booking ids \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedByPostForBookingIds(String bookingIds) {
        bookingSentenceDetail.getBookingSentenceDetailsUsingPostRequest(bookingIds);
    }

    @Then("^bad request response is received from booking sentence API$")
    public void badRequestResponseIsReceivedFromBookingSentenceAPI() {
        bookingSentenceDetail.verifyBadRequest("List of Offender Ids must be provided");
    }

    @When("^sentence details are requested of offenders for the logged in users caseloads$")
    public void sentenceDetailsAreRequestedForAnOffendersInLoggedInUsersCaseloads() {
        bookingSentenceDetail.getOffenderSentenceDetails();
    }

    @Then("^\"([0-9-]+)\" offenders are returned$")
    public void offendersAreReturned(long total) {
        bookingSentenceDetail.verifyNoError();
        bookingSentenceDetail.verifyResourceRecordsReturned(total);
    }

    @And("^\"([0-9-]+)\" offenders in total$")
    public void offendersInTotal(long total) {
        bookingSentenceDetail.verifyTotalResourceRecordsAvailable(total);
    }

    @Then("some offender sentence details are returned")
    public void someOffenderSentenceDetailsAreReturned() {
        bookingSentenceDetail.verifySomeResourceRecordsReturned();
    }

    @When("^sentence details are requested for offender Nos of \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForOffenderNosOf(String offenderNos) {
        bookingSentenceDetail.getOffenderSentenceDetails(offenderNos, null);
    }

    @When("^assessment information is requested for Booking Id \"([^\"]*)\"$")
    public void assessmentInformationIsRequestedForBookingId(String bookingId) {
        bookingAssessment.getAssessments(Long.valueOf(bookingId));
    }

    @Then("^\"(\\d+)\" row of assessment data is returned$")
    public void rowOfDataIsReturned(long expectedCount) {
        bookingAssessment.verifyResourceRecordsReturned(expectedCount);
    }

    @When("^offender identifiers are requested for Booking Id \"([^\"]*)\"$")
    public void offenderIdentifiersAreRequestedForBookingId(String bookingId) {
        bookingDetail.getOffenderIdentifiers(Long.valueOf(bookingId));
    }

    @When("^profile information is requested for Booking Id \"([^\"]*)\"$")
    public void profileInformationIsRequestedForBookingId(String bookingId) {
        bookingDetail.getProfileInformation(Long.valueOf(bookingId));
    }

    @When("^physical characteristic information is requested for Booking Id \"([^\"]*)\"$")
    public void physicalCharacteristicInformationIsRequestedForBookingId(String bookingId) {
        bookingDetail.getPhysicalCharacteristics(Long.valueOf(bookingId));
    }

    @When("^image metadata is requested for Booking Id \"([^\"]*)\"$")
    public void imageMetadataIsRequestedForBookingId(String bookingId) {
        bookingDetail.getImageMetadata(Long.valueOf(bookingId));
    }

    @When("^an physical attributes request is made with booking id \"([^\"]*)\"$")
    public void anPhysicalAttributesRequestIsMadeWithBookingId(String bookingId) {
        bookingDetail.getPhysicalAttributes(Long.valueOf(bookingId));
    }

    @Then("^\"(\\d+)\" row of offender identifiers is returned$")
    public void rowOfOffenderIdentifiersIsReturned(long expectedCount) {
        bookingDetail.verifyResourceRecordsReturned(expectedCount);
    }

    @Then("^correct profile information is returned$")
    public void correctProfileInformationIsReturned() {
        bookingDetail.verifyProfileInformation();
    }

    @Then("^\"(\\d+)\" row of physical characteristics is returned$")
    public void rowOfPhysicalCharacteristicsIsReturned(long expectedCount) {
        bookingDetail.verifyResourceRecordsReturned(expectedCount);
    }

    @Then("^image data is returned$")
    public void imageDataIsReturned() {
        bookingDetail.verifyImageMetadataExists();
    }

    @When("^sentence details are requested for offenders who are candidates for Home Detention Curfew$")
    public void sentenceDetailsAreRequestedForHomeDetentionCurfewCandidates() {
        bookingSentenceDetail.requestSentenceDetailsForHomeDetentionCurfewCandidates();
    }
}
