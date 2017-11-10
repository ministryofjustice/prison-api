package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking Sentence Detail feature.
 */
public class BookingSentenceDetailSteps extends CommonSteps {
    private static final String BOOKING_SENTENCE_DETAIL_API_URL = API_PREFIX + "bookings/{bookingId}/sentenceDetail";

    private SentenceDetail sentenceDetail;

    @Step("Get booking sentence detail")
    public void getBookingSentenceDetail(Long bookingId) {
        dispatchRequest(bookingId);
    }

    @Step("Verify sentence start date")
    public void verifySentenceStartDate(String sentenceStartDate) {
        verifyLocalDate(sentenceDetail.getSentenceStartDate(), sentenceStartDate);
    }

    @Step("Verify sentence expiry date")
    public void verifySentenceExpiryDate(String sentenceEndDate) {
        verifyLocalDate(sentenceDetail.getSentenceExpiryDate(), sentenceEndDate);
    }

    @Step("Verify early term date")
    public void verifyEarlyTermDate(String earlyTermDate) {
        verifyLocalDate(sentenceDetail.getEarlyTermDate(), earlyTermDate);
    }

    @Step("Verify mid term date")
    public void verifyMidTermDate(String midTermDate) {
        verifyLocalDate(sentenceDetail.getMidTermDate(), midTermDate);
    }

    @Step("Verify late term date")
    public void verifyLateTermDate(String lateTermDate) {
        verifyLocalDate(sentenceDetail.getLateTermDate(), lateTermDate);
    }

    @Step("Verify automatic release date")
    public void verifyAutomaticReleaseDate(String automaticReleaseDate) {
        verifyLocalDate(sentenceDetail.getAutomaticReleaseDate(), automaticReleaseDate);
    }

    @Step("Verify override automatic release date")
    public void verifyOverrideAutomaticReleaseDate(String overrideAutomaticReleaseDate) {
        verifyLocalDate(sentenceDetail.getAutomaticReleaseOverrideDate(), overrideAutomaticReleaseDate);
    }

    @Step("Verify conditional release date")
    public void verifyConditionalReleaseDate(String conditionalReleaseDate) {
        verifyLocalDate(sentenceDetail.getConditionalReleaseDate(), conditionalReleaseDate);
    }

    @Step("Verify override conditional release date")
    public void verifyOverrideConditionalReleaseDate(String overrideConditionalReleaseDate) {
        verifyLocalDate(sentenceDetail.getConditionalReleaseOverrideDate(), overrideConditionalReleaseDate);
    }

    @Step("Verify non-parole date")
    public void verifyNonParoleDate(String nonParoleDate) {
        verifyLocalDate(sentenceDetail.getNonParoleDate(), nonParoleDate);
    }

    @Step("Verify override non-parole date")
    public void verifyOverrideNonParoleDate(String overrideNonParoleDate) {
        verifyLocalDate(sentenceDetail.getNonParoleOverrideDate(), overrideNonParoleDate);
    }

    @Step("Verify post-recall release date")
    public void verifyPostRecallReleaseDate(String postRecallReleaseDate) {
        verifyLocalDate(sentenceDetail.getPostRecallReleaseDate(), postRecallReleaseDate);
    }

    @Step("Verify override post-recall release date")
    public void verifyOverridePostRecallReleaseDate(String overridePostRecallReleaseDate) {
        verifyLocalDate(sentenceDetail.getPostRecallReleaseOverrideDate(), overridePostRecallReleaseDate);
    }

    @Step("Verify home detention curfew eligibility date")
    public void verifyHomeDetentionCurfewEligibilityDate(String homeDetentionCurfewEligibilityDate) {
        verifyLocalDate(sentenceDetail.getHomeDetentionCurfewEligibilityDate(), homeDetentionCurfewEligibilityDate);
    }

    @Step("Verify parole eligibility date")
    public void verifyParoleEligibilityDate(String paroleEligibilityDate) {
        verifyLocalDate(sentenceDetail.getParoleEligibilityDate(), paroleEligibilityDate);
    }

    @Step("Verify home detention curfew approved date")
    public void verifyHomeDetentionCurfewActualDate(String homeDetentionCurfewActualDate) {
        verifyLocalDate(sentenceDetail.getHomeDetentionCurfewActualDate(), homeDetentionCurfewActualDate);
    }

    @Step("Verify parole approved date")
    public void verifyApprovedParoleDate(String approvedParoleDate) {
        verifyLocalDate(sentenceDetail.getApprovedParoleDate(), approvedParoleDate);
    }

    @Step("Verify release on temporary licence date")
    public void verifyReleaseOnTemporaryLicenceDate(String releaseOnTemporaryLicenceDate) {
        verifyLocalDate(sentenceDetail.getReleaseOnTemporaryLicenceDate(), releaseOnTemporaryLicenceDate);
    }

    @Step("Verify early release scheme eligibility date")
    public void verifyEarlyReleaseSchemeEligibilityDate(String earlyReleaseSchemeEligilityDate) {
        verifyLocalDate(sentenceDetail.getEarlyReleaseSchemeEligibilityDate(), earlyReleaseSchemeEligilityDate);
    }

    @Step("Verify licence expiry date")
    public void verifyLicenceExpiryDate(String licenceExpiryDate) {
        verifyLocalDate(sentenceDetail.getLicenceExpiryDate(), licenceExpiryDate);
    }

    @Step("Verify non-DTO release date")
    public void verifyNonDtoReleaseDate(String releaseDate) {
        verifyLocalDate(sentenceDetail.getNonDtoReleaseDate(), releaseDate);
    }

    @Step("verify non-DTO release date type")
    public void verifyNonDtoReleaseDateType(String releaseDateType) {
        verifyEnum(sentenceDetail.getNonDtoReleaseDateType(), releaseDateType);
    }

    @Step("Verify additional days awarded")
    public void verifyAdditionalDaysAwarded(Integer additionalDaysAwarded) {
        assertThat(sentenceDetail.getAdditionalDaysAwarded()).isEqualTo(additionalDaysAwarded);
    }

    @Step("Verify confirmed release date")
    public void verifyConfirmedReleaseDate(String confirmedReleaseDate) {
        verifyLocalDate(sentenceDetail.getConfirmedReleaseDate(), confirmedReleaseDate);
    }

    @Step("Verify release date")
    public void verifyReleaseDate(String releaseDate) {
        verifyLocalDate(sentenceDetail.getReleaseDate(), releaseDate);
    }

    @Step("Verify topup supervision eligibility date")
    public void verifyTopupSupervisionEndDate(String topupSupervisionEndDate) {
        verifyLocalDate(sentenceDetail.getTopupSupervisionEndDate(), topupSupervisionEndDate);
    }

    protected void init() {
        super.init();

        sentenceDetail = null;
    }

    private void dispatchRequest(Long bookingId) {
        init();

        ResponseEntity<SentenceDetail> response;

        try {
            response = restTemplate.exchange(BOOKING_SENTENCE_DETAIL_API_URL, HttpMethod.GET, createEntity(),
                    SentenceDetail.class, bookingId);

            sentenceDetail = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}
