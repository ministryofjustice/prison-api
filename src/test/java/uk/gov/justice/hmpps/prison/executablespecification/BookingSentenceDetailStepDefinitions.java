package uk.gov.justice.hmpps.prison.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingSentenceDetailSteps;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * BDD step definitions for the /offender-sentences API endpoints
 */
public class BookingSentenceDetailStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BookingSentenceDetailSteps bookingSentenceDetail;


    // Sentence Detail Step Definitions (for testing of /bookings/{bookingId}/sentenceDetail endpoint)
    @When("^sentence details are requested for an offender with booking id \"([^\"]*)\" and version \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForAnOffenderWithBookingId(final String bookingId, final String version) {
        bookingSentenceDetail.getBookingSentenceDetail(Long.valueOf(bookingId), version);
    }

    @When("^I look at row \"([^\"]*)\"$")
    public void iLookAtRow(final String index) {
        bookingSentenceDetail.putARowFromListInContext(Integer.valueOf(index) - 1);
    }

    @Then("^sentence start date matches \"([^\"]*)\"$")
    public void sentenceStartDateMatches(final String sentenceStartDate) {
        bookingSentenceDetail.verifySentenceStartDate(sentenceStartDate);
    }

    @And("^sentence expiry date matches \"([^\"]*)\"$")
    public void sentenceExpiryDateMatches(final String sentenceExpiryDate) {
        bookingSentenceDetail.verifySentenceExpiryDate(sentenceExpiryDate);
    }

    @And("^early term date matches \"([^\"]*)\"$")
    public void earlyTermDateMatches(final String earlyTermDate) {
        bookingSentenceDetail.verifyEarlyTermDate(earlyTermDate);
    }

    @And("^mid term date matches \"([^\"]*)\"$")
    public void midTermDateMatches(final String midTermDate) {
        bookingSentenceDetail.verifyMidTermDate(midTermDate);
    }

    @And("^late term date matches \"([^\"]*)\"$")
    public void lateTermDateMatches(final String lateTermDate) {
        bookingSentenceDetail.verifyLateTermDate(lateTermDate);
    }

    @And("^automatic release date matches \"([^\"]*)\"$")
    public void automaticReleaseDateMatches(final String automaticReleaseDate) {
        bookingSentenceDetail.verifyAutomaticReleaseDate(automaticReleaseDate);
    }

    @And("^override automatic release date matches \"([^\"]*)\"$")
    public void automaticOverrideReleaseDateMatches(final String overrideAutomaticReleaseDate) {
        bookingSentenceDetail.verifyOverrideAutomaticReleaseDate(overrideAutomaticReleaseDate);
    }

    @And("^conditional release date matches \"([^\"]*)\"$")
    public void conditionalReleaseDateMatches(final String conditionalReleaseDate) {
        bookingSentenceDetail.verifyConditionalReleaseDate(conditionalReleaseDate);
    }

    @And("^override conditional release date matches \"([^\"]*)\"$")
    public void conditionalOverrideReleaseDateMatches(final String overrideConditionalReleaseDate) {
        bookingSentenceDetail.verifyOverrideConditionalReleaseDate(overrideConditionalReleaseDate);
    }

    @And("^non-parole date matches \"([^\"]*)\"$")
    public void nonParoleDateMatches(final String nonParoleDate) {
        bookingSentenceDetail.verifyNonParoleDate(nonParoleDate);
    }

    @And("^override non-parole date matches \"([^\"]*)\"$")
    public void nonParoleOverrideDateMatches(final String overrideNonParoleDate) {
        bookingSentenceDetail.verifyOverrideNonParoleDate(overrideNonParoleDate);
    }

    @And("^post-recall release date matches \"([^\"]*)\"$")
    public void postRecallReleaseDateMatches(final String postRecallReleaseDate) {
        bookingSentenceDetail.verifyPostRecallReleaseDate(postRecallReleaseDate);
    }

    @And("^override post-recall release date matches \"([^\"]*)\"$")
    public void postRecallOverrideReleaseDateMatches(final String overridePostRecallReleaseDate) {
        bookingSentenceDetail.verifyOverridePostRecallReleaseDate(overridePostRecallReleaseDate);
    }

    @And("^home detention curfew eligibility date matches \"([^\"]*)\"$")
    public void homeDetentionCurfewEligibilityDateMatches(final String homeDetentionCurfewEligibilityDate) {
        bookingSentenceDetail.verifyHomeDetentionCurfewEligibilityDate(homeDetentionCurfewEligibilityDate);
    }

    @And("^parole eligibility date matches \"([^\"]*)\"$")
    public void paroleEligibilityDateMatches(final String paroleEligibilityDate) {
        bookingSentenceDetail.verifyParoleEligibilityDate(paroleEligibilityDate);
    }

    @And("^licence expiry date matches \"([^\"]*)\"$")
    public void licenceExpiryDateMatches(final String licenceExpiryDate) {
        bookingSentenceDetail.verifyLicenceExpiryDate(licenceExpiryDate);
    }

    @And("^non-DTO release date matches \"([^\"]*)\"$")
    public void nonDtoReleaseDateMatches(final String releaseDate) {
        bookingSentenceDetail.verifyNonDtoReleaseDate(releaseDate);
    }

    @And("^additional days awarded matches \"([^\"]*)\"$")
    public void additionalDaysAwardedMatches(final String additionalDaysAwarded) {
        bookingSentenceDetail.verifyAdditionalDaysAwarded(
                isBlank(additionalDaysAwarded) ? null : Integer.valueOf(additionalDaysAwarded));
    }

    @And("^non-DTO release date type matches \"([^\"]*)\"$")
    public void nonDtoReleaseDateTypeMatches(final String releaseDateType) {
        bookingSentenceDetail.verifyNonDtoReleaseDateType(releaseDateType);
    }

    @And("^home detention curfew actual date matches \"([^\"]*)\"$")
    public void homeDetentionCurfewActualDateMatches(final String homeDetentionCurfewActualDate) {
        bookingSentenceDetail.verifyHomeDetentionCurfewActualDate(homeDetentionCurfewActualDate);
    }

    @And("^actual parole date matches \"([^\"]*)\"$")
    public void actualParoleDateMatches(final String actualParoleDate) {
        bookingSentenceDetail.verifyActualParoleDate(actualParoleDate);
    }

    @And("^release on temporary licence date matches \"([^\"]*)\"$")
    public void releaseOnTemporaryLicenceDateMatches(final String releaseOnTemporaryLicenceDate) {
        bookingSentenceDetail.verifyReleaseOnTemporaryLicenceDate(releaseOnTemporaryLicenceDate);
    }

    @And("^early removal scheme eligibility date matches \"([^\"]*)\"$")
    public void earlyRemovalSchemeEligibilityDateMatches(final String earlyRemovalSchemeEligibilityDate) {
        bookingSentenceDetail.verifyEarlyRemovalSchemeEligibilityDate(earlyRemovalSchemeEligibilityDate);
    }

    @Then("^resource not found response is received from sentence details API$")
    public void resourceNotFoundResponseIsReceivedFromSentenceDetailsAPI() {
        bookingSentenceDetail.verifyResourceNotFound();
    }

    @And("^confirmed release date matches \"([^\"]*)\"$")
    public void confirmedReleaseDateMatches(final String confirmedReleaseDate) {
        bookingSentenceDetail.verifyConfirmedReleaseDate(confirmedReleaseDate);
    }

    @And("^topup supervision expiry date matches \"([^\"]*)\"$")
    public void topupSupervisionExpiryDateMatches(final String topupSupervisionExpiryDate) {
        bookingSentenceDetail.verifyTopupSupervisionExpiryDate(topupSupervisionExpiryDate);
    }

    @And("^release date matches \"([^\"]*)\"$")
    public void releaseDateMatches(final String releaseDate) {
        bookingSentenceDetail.verifyReleaseDate(releaseDate);
    }

    @And("^tariff date matches \"([^\"]*)\"$")
    public void tariffDateMatches(final String tariffDate) {
        bookingSentenceDetail.verifyTariffDate(tariffDate);
    }

    @And("^detention training order post-recall release date matches \"([^\"]*)\"$")
    public void dtoPostRecallMatches(final String dtoPostRecallReleaseDate) {
        bookingSentenceDetail.verifyDtoPostRecallReleaseDateOverride(dtoPostRecallReleaseDate);
    }

    @And("^tariff early removal scheme eligibility date matches \"([^\"]*)\"$")
    public void tariffEarlyRemovalMatches(final String tariffEarlyRemovalDate) {
        bookingSentenceDetail.verifyTariffEarlyRemovalSchemeEligibilityDate(tariffEarlyRemovalDate);
    }

    @And("^effective sentence end date matches \"([^\"]*)\"$")
    public void effectiveEndDateMatches(final String effectiveEndDate) {
        bookingSentenceDetail.verifyEffectiveSentenceEndDate(effectiveEndDate);
    }

    @When("^sentence details are requested for an offenders in logged in users caseloads with offender No \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForAnOffendersInLoggedInUsersCaseloadsWithBookingId(final String offenderNos) {
        bookingSentenceDetail.getOffenderSentenceDetails(offenderNos, null);
    }

    @When("^sentence details are requested by a POST request for offender Nos \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedByPostForOffenderNos(final String offenderNos) {
        bookingSentenceDetail.getOffenderSentenceDetailsUsingPostRequest(offenderNos);
    }

    @When("^sentence details are requested of offenders for the logged in users caseloads$")
    public void sentenceDetailsAreRequestedForAnOffendersInLoggedInUsersCaseloads() {
        bookingSentenceDetail.getOffenderSentenceDetails();
    }

    @When("^sentence details are requested of offenders for agency \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForAnAgency(final String agencyId) {
        bookingSentenceDetail.getOffenderSentenceDetails(agencyId);
    }

    @Then("^\"([0-9-]+)\" offenders are returned$")
    public void offendersAreReturned(final long total) {
        bookingSentenceDetail.verifyNoError();
        bookingSentenceDetail.verifyResourceRecordsReturned(total);
    }

    @Then("^some offenders are returned$")
    public void offendersAreReturned() {
        bookingSentenceDetail.verifyNoError();
        bookingSentenceDetail.verifyResourceRecordsNotEmpty();
    }

    @And("^\"([0-9-]+)\" offenders in total$")
    public void offendersInTotal(final long total) {
        bookingSentenceDetail.verifyTotalResourceRecordsAvailable(total);
    }

    @Then("some offender sentence details are returned")
    public void someOffenderSentenceDetailsAreReturned() {
        bookingSentenceDetail.verifySomeResourceRecordsReturned();
    }

    @When("^sentence details are requested for offender Nos of \"([^\"]*)\"$")
    public void sentenceDetailsAreRequestedForOffenderNosOf(final String offenderNos) {
        bookingSentenceDetail.getOffenderSentenceDetails(offenderNos, null);
    }

    @When("^sentence details are requested for offenders who are candidates for Home Detention Curfew$")
    public void sentenceDetailsAreRequestedForHomeDetentionCurfewCandidates() {
        bookingSentenceDetail.requestSentenceDetailsForHomeDetentionCurfewCandidates();
    }

    @When("^sentence terms are requested for booking id \"([^\"]*)\"$")
    public void sentenceTermsAreRequested(final String bookingId) {
        bookingSentenceDetail.requestSentenceTerms(bookingId);
    }

    @Then("^correct sentence terms data is returned$")
    public void verifySentenceTermsOld() {
        bookingSentenceDetail.verifySentenceTermsOld();
    }

    @Then("^correct sentence terms data is returned as follows:$")
    public void sentenceTermsAreReturnedAsFollows(final DataTable table) {
        bookingSentenceDetail.verifySentenceTerms(table.asList(OffenderSentenceTerms.class));
    }

    @When("^sentences and offences are requested for booking id \"([^\"]*)\"$")
    public void sentencesAndOffencesAreRequested(final String bookingId) {
        bookingSentenceDetail.requestSentencesAndOffences(bookingId);
    }

    @Then("^\"([0-9-]+)\" sentences are returned")
    public void sentencesAndOffencesAreReturned(final int total) {
        bookingSentenceDetail.verifySentencesAndOffences(total);
    }

    @Then("^the key values for record \"([^\"]*)\" are correct in the sentence and offence details \"([^\"]*)\" sentenced date: \"([^\"]*)\" offence date: \"([^\"]*)\" years: \"([^\"]*)\"$")
    public void sentenceSequenceIs(final int index, final int sentenceSequence, final String sentencedDate, final String offenceDate, final int years) {
        bookingSentenceDetail.verifySentenceDetails(index, sentenceSequence, sentencedDate, offenceDate, years);
    }
}
