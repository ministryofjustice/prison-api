package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingAlertSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingAliasSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingAssessmentSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingDetailSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingIEPSteps;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * BDD step definitions for the following Booking API endpoints:
 * <ul>
 *     <li>/booking/{bookingId}</li>
 *     <li>/booking/{bookingId}/alerts</li>
 *     <li>/booking/{bookingId}/alerts/{alertId}</li>
 *     <li>/booking/{bookingId}/aliases</li>
 *     <li>/bookings/{bookingId}/sentenceDetail</li>
 *     <li>/bookings/{bookingId}/balances</li>
 *     <li>/bookings/{bookingId}/mainSentence</li>
 * </ul>
 * <p>
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class BookingStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BookingAliasSteps bookingAlias;

    @Autowired
    private BookingDetailSteps bookingDetail;

    @Autowired
    private BookingIEPSteps bookingIEP;

    @Autowired
    private BookingAlertSteps bookingAlerts;

    @Autowired
    private BookingAssessmentSteps bookingAssessment;


    @When("^aliases are requested for an offender booking \"([^\"]*)\"$")
    public void aliasesAreRequestedForAnOffenderBooking(final String bookingId) {
        bookingAlias.getAliasesForBooking(Long.valueOf(bookingId));
    }

    @Then("^\"([^\"]*)\" aliases are returned$")
    public void aliasesAreReturned(final String expectedCount) {
        bookingAlias.verifyResourceRecordsReturned(Long.parseLong(expectedCount));
    }

    @And("^alias first names match \"([^\"]*)\"$")
    public void aliasFirstNamesMatch(final String firstNames) {
        bookingAlias.verifyAliasFirstNames(firstNames);
    }

    @And("^alias last names match \"([^\"]*)\"$")
    public void aliasLastNamesMatch(final String lastNames) {
        bookingAlias.verifyAliasLastNames(lastNames);
    }

    @And("^alias ethnicities match \"([^\"]*)\"$")
    public void aliasEthnicitiesMatch(final String ethnicities) {
        bookingAlias.verifyAliasEthnicities(ethnicities);
    }

    @Then("^resource not found response is received from offender aliases API$")
    public void resourceNotFoundResponseIsReceivedFromOffenderAliasesAPI() {
        bookingAlias.verifyResourceNotFound();
    }

    @When("^an offender booking request is made with booking id \"([^\"]*)\"$")
    public void anOffenderBookingRequestIsMadeWithBookingId(final String bookingId) {
        bookingDetail.findBookingDetails(Long.valueOf(bookingId), false);
    }

    @When("^a basic offender booking request is made with booking id \"([^\"]*)\"$")
    public void aBasicOffenderBookingRequestIsMadeWithBookingId(final String bookingId) {
        bookingDetail.findBookingDetails(Long.valueOf(bookingId), true);
    }

    @Then("^resource not found response is received from bookings API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsAPI() {
        bookingDetail.verifyResourceNotFound();
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

    @When("^an IEP summary only is requested for an offender with booking id \"([^\"]*)\"$")
    public void anIEPSummaryOnlyIsRequestedForAnOffenderWithBookingId(final String bookingId) {
        bookingIEP.getBookingIEPSummary(Long.valueOf(bookingId), false);
    }

    @When("^an IEP summary, with details, is requested for an offender with booking id \"([^\"]*)\"$")
    public void anIEPSummaryWithDetailsIsRequestedForAnOffenderWithBookingId(final String bookingId) {
        bookingIEP.getBookingIEPSummary(Long.valueOf(bookingId), true);
    }

    @Then("^IEP summary is returned with IEP level of \"([^\"]*)\"$")
    public void iepSummaryIsReturnedWithIEPLevelOf(final String iepLevel) {
        bookingIEP.verifyCurrentIEPLevel(iepLevel);
    }

    @And("^IEP summary contains \"([^\"]*)\" detail records$")
    public void iepSummaryContainsDetailRecords(final String detailRecordCount) {
        bookingIEP.verifyIEPDetailRecordCount(Integer.parseInt(detailRecordCount));
    }

    @And("^IEP days since review is correct for IEP date of \"([^\"]*)\"$")
    public void iepDaysSinceReviewIsCorrectForIEPDateOf(final String iepDate) {
        bookingIEP.verifyDaysSinceReview(iepDate);
    }

    @Then("^resource not found response is received from bookings IEP summary API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsIEPSummaryAPI() {
        bookingIEP.verifyResourceNotFound();
    }

    @And("^user message in resource not found response from bookings IEP summary API is \"([^\"]*)\"$")
    public void userMessageInResourceNotFoundResponseFromBookingsIEPSummaryAPIIs(final String expectedUserMessage) {
        bookingIEP.verifyErrorUserMessage(expectedUserMessage);
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

    // ----------------------------- Alerts --------------------------

    @When("^alerts are requested for an offender booking \"([^\"]*)\"$")
    public void alertsAreRequestedForOffenderBooking(final Long bookingId) {
        bookingAlerts.getAlerts(bookingId);
    }

    @Then("^\"([^\"]*)\" alerts are returned$")
    public void numberAlertsAreReturned(final String expectedCount) {
        bookingAlerts.verifyResourceRecordsReturned(Long.parseLong(expectedCount));
    }

    @And("alerts codes match \"([^\"]*)\"$")
    public void alertsCodesMatch(final String codes) {
        bookingAlerts.verifyCodeList(codes);
    }

    @When("^alert is requested for an offender booking \"([^\"]*)\" and alert id \"([^\"]*)\"$")
    public void alertIsRequestedForOffenderBooking(final Long bookingId, final Long alertId) {
        bookingAlerts.getAlert(bookingId, alertId);
    }

    @Then("^alert (\\w+) is \"([^\"]*)\"$")
    public void alertValueIs(final String field, final String value) throws Throwable {
        bookingAlerts.verifyAlertField(field, value);
    }

    @When("^alerts are requested for offender nos \"([^\"]*)\" and agency \"([^\"]*)\"$")
    public void alertsRequestedForOffenderBooking(final String offenderNos, final String agencyId) {
        bookingAlerts.getAlerts(agencyId, Arrays.asList(StringUtils.split(offenderNos, ",")));
    }

    @When("^alerts are requested for offender nos \"([^\"]*)\" and no agency$")
    public void alertsRequestedForOffenderBookingNoAgency(final String offenderNos) {
        bookingAlerts.getAlerts(null, Arrays.asList(StringUtils.split(offenderNos, ",")));
    }

    @Then("^alert details are returned as follows:$")
    public void alertsAreReturnedAsFollows(final List<Alert> expected) {
        bookingAlerts.verifyAlerts(expected);
    }

    @Then("^resource not found response is received from alert API$")
    public void resourceNotFoundResponseIsReceivedFromAlertAPI() {
        bookingAlerts.verifyResourceNotFound();
    }

    @Then("^access denied response is received from alert API$")
    public void accessDeniedResponseIsReceivedFromAlertAPI() {
        bookingAlerts.verifyAccessDenied();
    }

    // ----------------------------- Assessments --------------------------
    @When("^an offender booking assessment information request is made with booking id ([0-9-]+) and \"([^\"]*)\"$")
    public void anOffenderBookingAssessmentInformationRequestIsMadeWithBookingIdAnd(final Long bookingId, final String assessmentCode) {
        bookingAssessment.getAssessmentByCode(bookingId, assessmentCode);
    }

    @Then("^the classification is \"([^\"]*)\"$")
    public void theClassificationIsCorrect(final String classification) throws Throwable {
        bookingAssessment.verifyField("classification", classification);
    }

    @And("^the Cell Sharing Alert is (true|false)$")
    public void theCellSharingAlertIs(final boolean csra) {
        bookingAssessment.verifyCsra(csra);
    }

    @And("^the Next Review Date is \"([^\"]*)\"$")
    public void theNextReviewDateIs(final String nextReviewDate) {
        bookingAssessment.verifyNextReviewDate(nextReviewDate);
    }

    @And("^the CSRA is \"([^\"]*)\"$")
    public void theCsraIs(final String csra) throws ReflectiveOperationException {
        bookingDetail.verifyField("csra", csra);
    }

    @And("^the category is \"([^\"]*)\"$")
    public void theCategoryIs(final String category) throws ReflectiveOperationException {
        bookingDetail.verifyField("category", category);
    }

    @When("^an offender booking assessment information request is made with offender numbers \"([^\"]*)\" and \"([^\"]*)\" and latest=\"(true|false)\" and active=\"(true|false)\"$")
    public void anOffenderBookingAssessmentInformationRequestIsMadeWithBookingIdAnd(final String offenderNoList, final String assessmentCode, final boolean latestOnly, final boolean activeOnly) {
        bookingAssessment.getAssessmentsByCode(offenderNoList, assessmentCode, latestOnly, activeOnly);
    }

    @When("^an offender booking assessment information POST request is made with offender numbers \"([^\"]*)\" and \"([^\"]*)\"$")
    public void anOffenderBookingAssessmentInformationRequestIsMadeUsingPost(final String offenderNoList, final String assessmentCode) {
        bookingAssessment.getAssessmentsByCodeUsingPost(offenderNoList, assessmentCode);
    }

    @When("^an offender booking CSRA information POST request is made with offender numbers \"([^\"]*)\"$")
    public void anOffenderBookingCSRAInformationRequestIsMadeUsingPost(final String offenderNoList) {
        bookingAssessment.getCsrasUsingPost(offenderNoList);
    }

    @Then("^bad request response is received from booking assessments API with message \"([^\"]*)\"$")
    public void badRequestResponseIsReceivedFromBookingAssessmentsAPI(final String message) {
        bookingAssessment.verifyBadRequest(message);
    }

    @Then("^access denied response is received from booking assessments API$")
    public void accessDeniedResponseIsReceivedFromAssessmentsAPI() {
        bookingAssessment.verifyAccessDenied();
    }

    @Then("^correct results are returned as for single assessment$")
    public void multipleIsCorrect() {
        bookingAssessment.verifyMultipleAssessments();
    }

    @Then("^full category history is returned$")
    public void multipleCategoriesCorrect() {
        bookingAssessment.verifyMultipleCategoryAssessments();
    }

    @Then("^resource not found response is received from booking assessments API$")
    public void resourceNotFoundResponseIsReceivedFromBookingAssessmentsAPI() {
        bookingAssessment.verifyResourceNotFound();
    }

    @And("^user message in resource not found response from booking assessments API is \"([^\"]*)\"$")
    public void userMessageInResourceNotFoundResponseFromBookingAssessmentsAPIIs(final String expectedUserMessage) {
        bookingAssessment.verifyErrorUserMessage(expectedUserMessage);
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

    @Then("^([0-9]+) uncategorised offenders are returned$")
    public void returnedUncategorisedOffenders(final int size) {
        bookingAssessment.verifyOffenderCategoryListSize(size);
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

    @When("^assessment information is requested for Booking Id \"([^\"]*)\"$")
    public void assessmentInformationIsRequestedForBookingId(final String bookingId) {
        bookingAssessment.getAssessments(Long.valueOf(bookingId));
    }

    @Then("^\"(\\d+)\" row of assessment data is returned$")
    public void rowOfDataIsReturned(final long expectedCount) {
        bookingAssessment.verifyResourceRecordsReturned(expectedCount);
    }

    @When("^offender identifiers are requested for Booking Id \"([^\"]*)\"$")
    public void offenderIdentifiersAreRequestedForBookingId(final String bookingId) {
        bookingDetail.getOffenderIdentifiers(Long.valueOf(bookingId));
    }

    @When("^profile information is requested for Booking Id \"([^\"]*)\"$")
    public void profileInformationIsRequestedForBookingId(final String bookingId) {
        bookingDetail.getProfileInformation(Long.valueOf(bookingId));
    }

    @When("^physical characteristic information is requested for Booking Id \"([^\"]*)\"$")
    public void physicalCharacteristicInformationIsRequestedForBookingId(final String bookingId) {
        bookingDetail.getPhysicalCharacteristics(Long.valueOf(bookingId));
    }

    @When("^image metadata is requested for Booking Id \"([^\"]*)\"$")
    public void imageMetadataIsRequestedForBookingId(final String bookingId) {
        bookingDetail.getImageMetadata(Long.valueOf(bookingId));
    }

    @When("^image data is requested by booking Id \"([^\"]*)\"$")
    public void imageDataIsRequestedByBookingId(final String bookingId) {
        bookingDetail.getImageData(Long.valueOf(bookingId), false);
    }

    @When("^full size image is requested by booking Id \"([^\"]*)\"$")
    public void fullSizeImageDataIsRequestedByBookingId(final String bookingId) {
        bookingDetail.getImageData(Long.valueOf(bookingId), true);
    }

    @When("^full size image is requested by Noms Id \"([^\"]*)\"$")
    public void fullSizeImageDataIsRequestedByNomsId(final String nomsId) {
        bookingDetail.getImageData(nomsId, true);
    }

    @When("^an physical attributes request is made with booking id \"([^\"]*)\"$")
    public void anPhysicalAttributesRequestIsMadeWithBookingId(final String bookingId) {
        bookingDetail.getPhysicalAttributes(Long.valueOf(bookingId));
    }

    @Then("^\"(\\d+)\" row of offender identifiers is returned$")
    public void rowOfOffenderIdentifiersIsReturned(final long expectedCount) {
        bookingDetail.verifyResourceRecordsReturned(expectedCount);
    }

    @Then("^correct profile information is returned$")
    public void correctProfileInformationIsReturned() {
        bookingDetail.verifyProfileInformation();
    }

    @Then("^\"(\\d+)\" row of physical characteristics is returned$")
    public void rowOfPhysicalCharacteristicsIsReturned(final long expectedCount) {
        bookingDetail.verifyResourceRecordsReturned(expectedCount);
    }

    @Then("^image metadata is returned$")
    public void imageMetaDataIsReturned() {
        bookingDetail.verifyImageMetadataExists();
    }

    @Then("^image bytes are returned$")
    public void imageDataIsReturned() {
        bookingDetail.verifyImageBytesExists();
    }

    @When("^a request for IEP summaries are made for the following booking ids \"([^\"]*)\"$")
    public void aRequestForIEPSummariesAreMadeForTheFollowingBookingIds(final String bookings) {
        final var bookingIds = Arrays.asList(bookings.split(","));
        bookingIEP.getBookingIEPSummaryForOffenders(bookingIds, false);
    }

    @Then("^the response should contain an entry with \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theResponseShouldContainAnEntryWith(final String bookingId, final String iepLevel, final String iepDetailCount, final String iepDate) {
        bookingIEP.verifyIepEntry(Long.parseLong(bookingId), iepLevel, Integer.parseInt(iepDetailCount), LocalDate.parse(iepDate));
    }

    @When("^a request for IEP summaries are made for the following booking ids \"([^\"]*)\" including extra details$")
    public void aRequestForIEPSummariesAreMadeForTheFollowingBookingIdsIncludingExtraDetails(final String bookings) {
        final var bookingIds = Arrays.asList(bookings.split(","));
        bookingIEP.getBookingIEPSummaryForOffenders(bookingIds, true);
    }

    @When("^a request for IEP summaries are made for the following booking ids \"([^\"]*)\" with POST$")
    public void aRequestForIEPSummariesAreMadeForTheFollowingBookingIdsWithPost(final String bookings) {
        final var bookingIds = Arrays.asList(bookings.split(","));
        bookingIEP.getBookingIEPSummaryForBookingIds(bookingIds);
    }

    @When("^a categorisation request is made for booking \"([^\"]*)\" with category \"([^\"]*)\" for committee \"([^\"]*)\"$")
    public void aCategorisationRequestIsMadeForBookingWithCategoryForCommitteeAt(final String bookingId, final String category, final String committee) {
        bookingAssessment.createCategorisation(Long.parseLong(bookingId), category, committee);
    }

    @When("^a categorisation is approved for booking \"([^\"]*)\" with category \"([^\"]*)\" date \"([^\"]*)\" and comment \"([^\"]*)\"$")
    public void aCategorisationApprovalForBookingWithCategory(final String bookingId, final String category, final String date, final String comment) {
        final var localDate = StringUtils.isBlank(date) ? null : LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        final var id = StringUtils.isBlank(bookingId) ? null : Long.parseLong(bookingId);
        bookingAssessment.approveCategorisation(id, StringUtils.trimToNull(category), localDate, StringUtils.trimToNull(comment));
    }

    @Then("^offender with booking \"([^\"]*)\" has a categorised status of AWAITING_APROVAL$")
    public void offenderWithBookingHasACategorisedStatusOfAWAITINGAPROVAL(final String bookingId) {
        bookingAssessment.verifyCategorisedPendingApproval(Long.parseLong(bookingId));
    }

    @Then("^offender with booking \"([^\"]*)\" is not present$")
    public void offenderWithBookingNotPresent(final String bookingId) {
        bookingAssessment.verifyCategorisedNotPresent(Long.parseLong(bookingId));
    }

    @When("^a request is made for \"([^\"]*)\"$")
    public void aRequestIsMadeFor(final String offenders) {
        bookingDetail.findBookingDetails(List.of(offenders.split(",")));
    }

    @When("^a request is made with booking Ids \"([^\"]*)\" for prison \"([^\"]*)\"$")
    public void aRequestIsMadeForBookingIds(final String bookingsIds, final String agency) {
        bookingDetail.findInmateDetailsNyBookingIds(agency, Arrays.stream(bookingsIds.split(",")).map(Long::valueOf).collect(Collectors.toList()));
    }

    @Then("^data is returned that includes \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void dataIsReturnedThatIncludes(final String firstName, final String lastName, final String middleName, final String offenderNo, final String bookingId, final String agencyId) {
        bookingDetail.verifyOffenders(firstName, lastName, middleName, offenderNo, bookingId, agencyId);
    }

    @Then("^the total records returned are \"([^\"]*)\"$")
    public void theTotalRecordsReturnedAre(final int size) {
        bookingDetail.verifyOffenderCount(size);
    }

    @When("^a request is made for offender categorisation details at \"([^\"]*)\" with booking id \"([^\"]*)\", latest cat only$")
    public void aRequestIsMadeForOffenderCategorisationDetailsAtWithBookingIdLatest(final String agency, final String bookingId) {
        bookingAssessment.getOffendersCategorisations(agency, Collections.singletonList(Long.valueOf(bookingId)), true);
    }

    @When("^a request is made for offender categorisation details at \"([^\"]*)\" with booking id \"([^\"]*)\", all cats$")
    public void aRequestIsMadeForOffenderCategorisationDetailsAtWithBookingIdAll(final String agency, final String bookingId) {
        bookingAssessment.getOffendersCategorisations(agency, Collections.singletonList(Long.valueOf(bookingId)), false);
    }

    @Then("^\"([^\"]*)\" rows of basic inmate details are returned$")
    public void rowsOfBasicInmateDetailsAreReturned(final String count) {
        bookingDetail.verifyOffendersBasicCount(Integer.parseInt(count));
    }

    @DataTableType
    public Alert alertEntry(Map<String, String> entry) {
        return Alert.builder()
            .offenderNo(entry.get("offenderNo"))
            .bookingId(Long.valueOf(entry.get("bookingId")))
            .alertId(Long.valueOf(entry.get("alertId")))
            .alertType(entry.get("alertType"))
            .alertTypeDescription(entry.get("alertTypeDescription"))
            .alertCode(entry.get("alertCode"))
            .alertCodeDescription(entry.get("alertCodeDescription"))
            .comment(entry.get("comment"))
            .dateCreated(convertTodayAndParse(entry.get("dateCreated")))
            .dateExpires(convertTodayAndParse(entry.get("dateExpires")))
            .expired(Boolean.parseBoolean(entry.get("expired")))
            .active(Boolean.parseBoolean(entry.get("active")))
            .build();
    }

    private LocalDate convertTodayAndParse(final String dateCreated) {
        if (StringUtils.isBlank(dateCreated)) return null;
        if (dateCreated.equals("today")) return LocalDate.now();
        return LocalDate.parse(dateCreated);
    }
}
