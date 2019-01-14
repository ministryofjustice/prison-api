package net.syscon.elite.executablespecification.steps;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking Sentence Detail feature.
 */
public class BookingSentenceDetailSteps extends CommonSteps {
    private static final String BOOKING_SENTENCE_DETAIL_API_URL = API_PREFIX + "bookings/{bookingId}/sentenceDetail";

    private static final String OFFENDER_SENTENCE_DETAIL_API_URL = API_PREFIX + "offender-sentences";
    private static final String OFFENDER_BOOKING_SENTENCE_DETAIL_API_URL = OFFENDER_SENTENCE_DETAIL_API_URL + "/bookings";

    private static final String HOME_DETENTION_CURFEW_CANDIDATES = OFFENDER_SENTENCE_DETAIL_API_URL + "/home-detention-curfew-candidates";
    private static final ParameterizedTypeReference<List<OffenderSentenceDetail>> LIST_OF_OFFENDER_SENTENCE_DETAIL_TYPE = new ParameterizedTypeReference<>() {
    };

    private SentenceDetail sentenceDetail;

    private List<OffenderSentenceDetail> offenderSentenceDetails;

    @Step("Get booking sentence detail")
    public void getBookingSentenceDetail(Long bookingId) {
        dispatchRequest(bookingId);
    }

    @Step("Get offender sentence details by offender nos and agency id")
    public void getOffenderSentenceDetails(String offenderNos, String agencyId) {
        dispatchOffenderSentences(offenderNos, agencyId);
    }

    @Step("Get offender sentence details by offender nos (using post request)")
    public void getOffenderSentenceDetailsUsingPostRequest(String offenderNos) {
        List<String> offenderList = StringUtils.isNotBlank(offenderNos) ? ImmutableList.copyOf(offenderNos.split(",")) : Collections.emptyList();
        dispatchOffenderSentencesForPostRequest(OFFENDER_SENTENCE_DETAIL_API_URL, offenderList);
    }

    @Step("Get offender sentence details by booking ids (using post request)")
    public void getBookingSentenceDetailsUsingPostRequest(String bookingIds) {
        List<String> list = StringUtils.isNotBlank(bookingIds) ? ImmutableList.copyOf(bookingIds.split(",")) : Collections.emptyList();
        dispatchOffenderSentencesForPostRequest(OFFENDER_BOOKING_SENTENCE_DETAIL_API_URL, list);
    }

    @Step("Get offender sentence details")
    public void getOffenderSentenceDetails() {
        dispatchOffenderSentences(null, null);
    }

    @Step("Get offender sentence details for agency")
    public void getOffenderSentenceDetails(String agencyId) {
        dispatchOffenderSentences(null, agencyId);
    }

    @Step("Set row from list in context")
    public void putARowFromListInContext(int index) {
        sentenceDetail = offenderSentenceDetails.get(index).getSentenceDetail();
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

    @Step("Verify home detention curfew actual date")
    public void verifyHomeDetentionCurfewActualDate(String homeDetentionCurfewActualDate) {
        verifyLocalDate(sentenceDetail.getHomeDetentionCurfewActualDate(), homeDetentionCurfewActualDate);
    }

    @Step("Verify actual parole date")
    public void verifyActualParoleDate(String actualParoleDate) {
        verifyLocalDate(sentenceDetail.getActualParoleDate(), actualParoleDate);
    }

    @Step("Verify release on temporary licence date")
    public void verifyReleaseOnTemporaryLicenceDate(String releaseOnTemporaryLicenceDate) {
        verifyLocalDate(sentenceDetail.getReleaseOnTemporaryLicenceDate(), releaseOnTemporaryLicenceDate);
    }

    @Step("Verify early removal scheme eligibility date")
    public void verifyEarlyRemovalSchemeEligibilityDate(String earlyRemovalSchemeEligilityDate) {
        verifyLocalDate(sentenceDetail.getEarlyRemovalSchemeEligibilityDate(), earlyRemovalSchemeEligilityDate);
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

    @Step("Verify tariff date")
    public void verifyTariffDate(String tariffDate) {
        verifyLocalDate(sentenceDetail.getTariffDate(), tariffDate);
    }

    @Step("Verify topup supervision expiry date")
    public void verifyTopupSupervisionExpiryDate(String topupSupervisionExpiryDate) {
        verifyLocalDate(sentenceDetail.getTopupSupervisionExpiryDate(), topupSupervisionExpiryDate);
    }

    @Step("Request sentence details for Home Detention Curfew Candidates")
    public void requestSentenceDetailsForHomeDetentionCurfewCandidates(boolean newVersion) {
        dispatchOffenderSentencesForHomeDetentionCurfewCandidates(newVersion);
    }

    @Step("Verify some resource records returned")
    public void verifySomeResourceRecordsReturned() {
        assertThat(offenderSentenceDetails).isNotEmpty();
    }

    protected void init() {
        super.init();
        offenderSentenceDetails = null;
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

    private void dispatchOffenderSentences(String offenderNos, String agencyId) {
        init();

        StringBuilder urlModifier = new StringBuilder();
        if (StringUtils.isNotBlank(offenderNos)) {
            Arrays.asList(offenderNos.split(",")).forEach(offenderNo -> urlModifier.append(initialiseUrlModifier(urlModifier)).append("offenderNo=").append(offenderNo));
        }

        if (StringUtils.isNotBlank(agencyId)) {
            urlModifier.append(initialiseUrlModifier(urlModifier)).append("agencyId=").append(agencyId);
        }

        Map<String, String> headers = new HashMap<>();
        try {
            ResponseEntity<List<OffenderSentenceDetail>> response = restTemplate.exchange(OFFENDER_SENTENCE_DETAIL_API_URL + urlModifier,
                    HttpMethod.GET,
                    createEntity(null, headers),
                    LIST_OF_OFFENDER_SENTENCE_DETAIL_TYPE);
            buildResourceData(response);

            offenderSentenceDetails = response.getBody();
            if (offenderSentenceDetails != null &&
                    !offenderSentenceDetails.isEmpty() &&
                    offenderSentenceDetails.size() == 1) {
                sentenceDetail = offenderSentenceDetails.get(0).getSentenceDetail();
            }
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchOffenderSentencesForHomeDetentionCurfewCandidates(boolean newVersion) {
        init();

        try {
            ResponseEntity<List<OffenderSentenceDetail>> response = restTemplate.exchange(HOME_DETENTION_CURFEW_CANDIDATES + (newVersion ? "/v2" : ""),
                    HttpMethod.GET,
                    createEntity(null, Collections.emptyMap()),
                    LIST_OF_OFFENDER_SENTENCE_DETAIL_TYPE);
            buildResourceData(response);

            offenderSentenceDetails = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchOffenderSentencesForPostRequest(String url, List<String> idList) {
        init();

        try {
            ResponseEntity<List<OffenderSentenceDetail>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    createEntity(idList),
                    LIST_OF_OFFENDER_SENTENCE_DETAIL_TYPE);
            buildResourceData(response);

            offenderSentenceDetails = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private String initialiseUrlModifier(StringBuilder urlModifier) {
        return urlModifier.length() > 0 ? "&" : "?";
    }
}
