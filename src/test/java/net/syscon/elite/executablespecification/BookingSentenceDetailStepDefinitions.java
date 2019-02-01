package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.BookingSentenceDetailSteps;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * BDD step definitions for the /offender-sentences API endpoints
 */
public class BookingSentenceDetailStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BookingSentenceDetailSteps bookingSentenceDetail;


    // Sentence Detail Step Definitions (for testing of /bookings/{bookingId}/sentenceDetail endpoint)
    @When("^sentence details are requested for an offender with booking id \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForAnOffenderWithBookingId(String bookingId) {
        bookingSentenceDetail.getBookingSentenceDetail(Long.valueOf(bookingId));
    }

    @When("^I look at row \"([^\"]*)\"$")
    public void iLookAtRow(String index) {
        bookingSentenceDetail.putARowFromListInContext(Integer.valueOf(index) - 1);
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

    @When("^sentence details are requested of offenders for agency \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForAnAgency(String agencyId) {
        bookingSentenceDetail.getOffenderSentenceDetails(agencyId);
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

    @When("^sentence details are requested for offenders who are candidates for Home Detention Curfew$")
    public void sentenceDetailsAreRequestedForHomeDetentionCurfewCandidates() {
        bookingSentenceDetail.requestSentenceDetailsForHomeDetentionCurfewCandidates();
    }

    @When("^sentence terms are requested for booking id \"([^\"]*)\"$")
    public void sentenceTermsAreRequested(String bookingId) {
        bookingSentenceDetail.requestSentenceTerms(bookingId);
    }
    @Then("^correct sentence terms data is returned$")
    public void verifySentenceTerms() {
        bookingSentenceDetail.verifySentenceTerms();
    }
}
