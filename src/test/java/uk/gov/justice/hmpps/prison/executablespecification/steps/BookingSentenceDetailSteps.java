package uk.gov.justice.hmpps.prison.executablespecification.steps;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.serenitybdd.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking Sentence Detail feature.
 */
public class BookingSentenceDetailSteps extends CommonSteps {
    private static final String BOOKING_SENTENCE_DETAIL_API_URL = API_PREFIX + "bookings/{bookingId}/sentenceDetail";

    private static final String OFFENDER_SENTENCE_DETAIL_API_URL = API_PREFIX + "offender-sentences";
    private static final String HOME_DETENTION_CURFEW_CANDIDATES = OFFENDER_SENTENCE_DETAIL_API_URL + "/home-detention-curfew-candidates?minimumChecksPassedDateForAssessedCurfews={iso8601Date}";

    private static final ParameterizedTypeReference<List<OffenderSentenceDetail>> LIST_OF_OFFENDER_SENTENCE_DETAIL_TYPE = new ParameterizedTypeReference<>() {
    };

    private SentenceCalcDates sentenceDetail;
    private OffenderSentenceTerms offenderSentenceTerms;
    private List<OffenderSentenceTerms> offenderSentenceTermsList;
    private List<OffenderSentenceDetail> offenderSentenceDetails;

    @Step("Get booking sentence detail")
    public void getBookingSentenceDetail(final Long bookingId, final String version) {
        dispatchSentenceDetail(bookingId, version);
    }

    @Step("Get offender sentence details by offender nos and agency id")
    public void getOffenderSentenceDetails(final String offenderNos, final String agencyId) {
        dispatchOffenderSentences(offenderNos, agencyId);
    }

    @Step("Get offender sentence details by offender nos (using post request)")
    public void getOffenderSentenceDetailsUsingPostRequest(final String offenderNos) {
        final List<String> offenderList = StringUtils.isNotBlank(offenderNos) ? ImmutableList.copyOf(offenderNos.split(",")) : Collections.emptyList();
        dispatchOffenderSentencesForPostRequest(OFFENDER_SENTENCE_DETAIL_API_URL, offenderList);
    }

    @Step("Get offender sentence details")
    public void getOffenderSentenceDetails() {
        dispatchOffenderSentences(null, null);
    }

    @Step("Get offender sentence details for agency")
    public void getOffenderSentenceDetails(final String agencyId) {
        dispatchOffenderSentences(null, agencyId);
    }

    @Step("Set row from list in context")
    public void putARowFromListInContext(final int index) {
        sentenceDetail = offenderSentenceDetails.get(index).getSentenceDetail();
    }

    @Step("Verify sentence start date")
    public void verifySentenceStartDate(final String sentenceStartDate) {
        verifyLocalDate(sentenceDetail.getSentenceStartDate(), sentenceStartDate);
    }

    @Step("Verify sentence expiry date")
    public void verifySentenceExpiryDate(final String sentenceEndDate) {
        verifyLocalDate(sentenceDetail.getSentenceExpiryDate(), sentenceEndDate);
    }

    @Step("Verify sentence expiry override date")
    public void verifySentenceExpiryOverrideDate(final String sentenceExpiryOverrideDate) {
        verifyLocalDate(sentenceDetail.getSentenceExpiryOverrideDate(), sentenceExpiryOverrideDate);
    }

    @Step("Verify sentence expiry calculated date")
    public void verifySentenceExpiryCalculatedDate(final String sentenceExpiryCalculatedDate) {
        verifyLocalDate(sentenceDetail.getSentenceExpiryCalculatedDate(), sentenceExpiryCalculatedDate);
    }

    @Step("Verify early term date")
    public void verifyEarlyTermDate(final String earlyTermDate) {
        verifyLocalDate(sentenceDetail.getEarlyTermDate(), earlyTermDate);
    }

    @Step("Verify mid term date")
    public void verifyMidTermDate(final String midTermDate) {
        verifyLocalDate(sentenceDetail.getMidTermDate(), midTermDate);
    }

    @Step("Verify late term date")
    public void verifyLateTermDate(final String lateTermDate) {
        verifyLocalDate(sentenceDetail.getLateTermDate(), lateTermDate);
    }

    @Step("Verify automatic release date")
    public void verifyAutomaticReleaseDate(final String automaticReleaseDate) {
        verifyLocalDate(sentenceDetail.getAutomaticReleaseDate(), automaticReleaseDate);
    }

    @Step("Verify override automatic release date")
    public void verifyOverrideAutomaticReleaseDate(final String overrideAutomaticReleaseDate) {
        verifyLocalDate(sentenceDetail.getAutomaticReleaseOverrideDate(), overrideAutomaticReleaseDate);
    }

    @Step("Verify conditional release date")
    public void verifyConditionalReleaseDate(final String conditionalReleaseDate) {
        verifyLocalDate(sentenceDetail.getConditionalReleaseDate(), conditionalReleaseDate);
    }

    @Step("Verify override conditional release date")
    public void verifyOverrideConditionalReleaseDate(final String overrideConditionalReleaseDate) {
        verifyLocalDate(sentenceDetail.getConditionalReleaseOverrideDate(), overrideConditionalReleaseDate);
    }

    @Step("Verify non-parole date")
    public void verifyNonParoleDate(final String nonParoleDate) {
        verifyLocalDate(sentenceDetail.getNonParoleDate(), nonParoleDate);
    }

    @Step("Verify override non-parole date")
    public void verifyOverrideNonParoleDate(final String overrideNonParoleDate) {
        verifyLocalDate(sentenceDetail.getNonParoleOverrideDate(), overrideNonParoleDate);
    }

    @Step("Verify post-recall release date")
    public void verifyPostRecallReleaseDate(final String postRecallReleaseDate) {
        verifyLocalDate(sentenceDetail.getPostRecallReleaseDate(), postRecallReleaseDate);
    }

    @Step("Verify override post-recall release date")
    public void verifyOverridePostRecallReleaseDate(final String overridePostRecallReleaseDate) {
        verifyLocalDate(sentenceDetail.getPostRecallReleaseOverrideDate(), overridePostRecallReleaseDate);
    }

    @Step("Verify home detention curfew eligibility date")
    public void verifyHomeDetentionCurfewEligibilityDate(final String homeDetentionCurfewEligibilityDate) {
        verifyLocalDate(sentenceDetail.getHomeDetentionCurfewEligibilityDate(), homeDetentionCurfewEligibilityDate);
    }
    @Step("Verify home detention curfew eligibility calculated date")
    public void verifyHomeDetentionCurfewEligibilityCalculatedDate(final String homeDetentionCurfewEligibilityCalculatedDate) {
        verifyLocalDate(sentenceDetail.getHomeDetentionCurfewEligibilityCalculatedDate(), homeDetentionCurfewEligibilityCalculatedDate);
    }
    @Step("Verify home detention curfew eligibility override date")
    public void verifyHomeDetentionCurfewEligibilityOverrideDate(final String homeDetentionCurfewEligibilityOverrideDate) {
        verifyLocalDate(sentenceDetail.getHomeDetentionCurfewEligibilityOverrideDate(), homeDetentionCurfewEligibilityOverrideDate);
    }

    @Step("Verify parole eligibility date")
    public void verifyParoleEligibilityDate(final String paroleEligibilityDate) {
        verifyLocalDate(sentenceDetail.getParoleEligibilityDate(), paroleEligibilityDate);
    }

    @Step("Verify parole eligibility override date")
    public void verifyParoleEligibilityOverrideDate(final String paroleEligibilityOverrideDate) {
        verifyLocalDate(sentenceDetail.getParoleEligibilityOverrideDate(), paroleEligibilityOverrideDate);
    }

    @Step("Verify parole eligibility calculated date")
    public void verifyParoleEligibilityCalculatedDate(final String paroleEligibilityCalculatedDate) {
        verifyLocalDate(sentenceDetail.getParoleEligibilityCalculatedDate(), paroleEligibilityCalculatedDate);
    }

    @Step("Verify home detention curfew actual date")
    public void verifyHomeDetentionCurfewActualDate(final String homeDetentionCurfewActualDate) {
        verifyLocalDate(sentenceDetail.getHomeDetentionCurfewActualDate(), homeDetentionCurfewActualDate);
    }

    @Step("Verify actual parole date")
    public void verifyActualParoleDate(final String actualParoleDate) {
        verifyLocalDate(sentenceDetail.getActualParoleDate(), actualParoleDate);
    }

    @Step("Verify release on temporary licence date")
    public void verifyReleaseOnTemporaryLicenceDate(final String releaseOnTemporaryLicenceDate) {
        verifyLocalDate(sentenceDetail.getReleaseOnTemporaryLicenceDate(), releaseOnTemporaryLicenceDate);
    }

    @Step("Verify early removal scheme eligibility date")
    public void verifyEarlyRemovalSchemeEligibilityDate(final String earlyRemovalSchemeEligilityDate) {
        verifyLocalDate(sentenceDetail.getEarlyRemovalSchemeEligibilityDate(), earlyRemovalSchemeEligilityDate);
    }

    @Step("Verify licence expiry date")
    public void verifyLicenceExpiryDate(final String licenceExpiryDate) {
        verifyLocalDate(sentenceDetail.getLicenceExpiryDate(), licenceExpiryDate);
    }

    @Step("Verify licence expiry override date")
    public void verifyLicenceExpiryOverrideDate(final String licenseExpiryOverrideDate) {
        verifyLocalDate(sentenceDetail.getLicenceExpiryOverrideDate(), licenseExpiryOverrideDate);
    }

    @Step("Verify licence expiry calculated date")
    public void verifyLicenceExpiryCalculatedDate(final String licenseExpiryCalculatedDate) {
        verifyLocalDate(sentenceDetail.getLicenceExpiryCalculatedDate(), licenseExpiryCalculatedDate);
    }

    @Step("Verify non-DTO release date")
    public void verifyNonDtoReleaseDate(final String releaseDate) {
        verifyLocalDate(sentenceDetail.getNonDtoReleaseDate(), releaseDate);
    }

    @Step("verify non-DTO release date type")
    public void verifyNonDtoReleaseDateType(final String releaseDateType) {
        verifyEnum(sentenceDetail.getNonDtoReleaseDateType(), releaseDateType);
    }

    @Step("Verify additional days awarded")
    public void verifyAdditionalDaysAwarded(final Integer additionalDaysAwarded) {
        assertThat(sentenceDetail.getAdditionalDaysAwarded()).isEqualTo(additionalDaysAwarded);
    }

    @Step("Verify confirmed release date")
    public void verifyConfirmedReleaseDate(final String confirmedReleaseDate) {
        verifyLocalDate(sentenceDetail.getConfirmedReleaseDate(), confirmedReleaseDate);
    }

    @Step("Verify release date")
    public void verifyReleaseDate(final String releaseDate) {
        verifyLocalDate(sentenceDetail.getReleaseDate(), releaseDate);
    }

    @Step("Verify tariff date")
    public void verifyTariffDate(final String tariffDate) {
        verifyLocalDate(sentenceDetail.getTariffDate(), tariffDate);
    }

    @Step("Verify topup supervision expiry date")
    public void verifyTopupSupervisionExpiryDate(final String topupSupervisionExpiryDate) {
        verifyLocalDate(sentenceDetail.getTopupSupervisionExpiryDate(), topupSupervisionExpiryDate);
    }
    @Step("Verify topup supervision expiry calculated date")
    public void verifyTopupSupervisionExpiryCalculatedDate(final String topupSupervisionExpiryCalculatedDate) {
        verifyLocalDate(sentenceDetail.getTopupSupervisionExpiryCalculatedDate(), topupSupervisionExpiryCalculatedDate);
    }
    @Step("Verify topup supervision expiry override date")
    public void verifyTopupSupervisionExpiryOverrideDate(final String topupSupervisionExpiryOverrideDate) {
        verifyLocalDate(sentenceDetail.getTopupSupervisionExpiryOverrideDate(), topupSupervisionExpiryOverrideDate);
    }

    @Step("Verify detention training order post-recall release date")
    public void verifyDtoPostRecallReleaseDateOverride(final String dtoPostRecallReleaseDateOverride) {
        verifyLocalDate(sentenceDetail.getDtoPostRecallReleaseDateOverride(), dtoPostRecallReleaseDateOverride);
    }

    @Step("Verify tariff early removal scheme eligibility date")
    public void verifyTariffEarlyRemovalSchemeEligibilityDate(final String tariffEarlyRemovalSchemeEligibilityDate) {
        verifyLocalDate(sentenceDetail.getTariffEarlyRemovalSchemeEligibilityDate(), tariffEarlyRemovalSchemeEligibilityDate);
    }

    @Step("Verify effective sentence end date")
    public void verifyEffectiveSentenceEndDate(final String effectiveSentenceEndDate) {
        verifyLocalDate(sentenceDetail.getEffectiveSentenceEndDate(), effectiveSentenceEndDate);
    }

    @Step("Request sentence details for Home Detention Curfew Candidates")
    public void requestSentenceDetailsForHomeDetentionCurfewCandidates() {
        dispatchOffenderSentencesForHomeDetentionCurfewCandidates();
    }

    @Step("Verify calculated ETD date")
    public void verifyEtdCalculated(String etdCalculatedDate) {
        verifyLocalDate(sentenceDetail.getEtdCalculatedDate(), etdCalculatedDate);
    }

    @Step("Verify override ETD date")
    public void verifyEtdOverride(String etdOverrideDate) {
        verifyLocalDate(sentenceDetail.getEtdOverrideDate(), etdOverrideDate);
    }
    @Step("Verify calculated LTD date")
    public void verifyLtdCalculated(String ltdCalculatedDate) {
        verifyLocalDate(sentenceDetail.getLtdCalculatedDate(), ltdCalculatedDate);
    }

    @Step("Verify override LTD date")
    public void verifyLtdOverride(String ltdOverrideDate) {
        verifyLocalDate(sentenceDetail.getLtdOverrideDate(), ltdOverrideDate);
    }

    @Step("Verify calculated MTD date")
    public void verifyMtdCalculated(String mtdCalculatedDate) {
        verifyLocalDate(sentenceDetail.getMtdCalculatedDate(), mtdCalculatedDate);
    }

    @Step("Verify override MTD date")
    public void verifyMtdOverride(String mtdOverrideDate) {
        verifyLocalDate(sentenceDetail.getMtdOverrideDate(), mtdOverrideDate);
    }

    @Step("Verify some resource records returned")
    public void verifySomeResourceRecordsReturned() {
        assertThat(offenderSentenceDetails).isNotEmpty();
    }

    protected void init() {
        super.init();
        offenderSentenceDetails = null;
        offenderSentenceTerms = null;
        offenderSentenceTermsList = null;
        sentenceDetail = null;
    }

    private void dispatchSentenceDetail(final Long bookingId, final String version) {
        init();

        final ResponseEntity<SentenceCalcDates> response;

        try {
            response = restTemplate.exchange(
                BOOKING_SENTENCE_DETAIL_API_URL,
                HttpMethod.GET,
                createEntity(null, ImmutableMap.of("version", version)),
                SentenceCalcDates.class,
                bookingId);

            sentenceDetail = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchOffenderSentences(final String offenderNos, final String agencyId) {
        init();

        final var urlModifier = new StringBuilder();
        if (StringUtils.isNotBlank(offenderNos)) {
            Arrays.asList(offenderNos.split(",")).forEach(offenderNo -> urlModifier.append(initialiseUrlModifier(urlModifier)).append("offenderNo=").append(offenderNo));
        }

        if (StringUtils.isNotBlank(agencyId)) {
            urlModifier.append(initialiseUrlModifier(urlModifier)).append("agencyId=").append(agencyId);
        }

        final Map<String, String> headers = new HashMap<>();
        try {
            final var response = restTemplate.exchange(OFFENDER_SENTENCE_DETAIL_API_URL + urlModifier,
                    HttpMethod.GET,
                    createEntity(null, headers),
                    LIST_OF_OFFENDER_SENTENCE_DETAIL_TYPE);
            buildResourceData(response);

            offenderSentenceDetails = response.getBody();
            if (offenderSentenceDetails != null && offenderSentenceDetails.size() == 1) {
                sentenceDetail = offenderSentenceDetails.get(0).getSentenceDetail();
            }
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchOffenderSentencesForHomeDetentionCurfewCandidates() {
        init();

        try {
            final var response = restTemplate.exchange(
                    HOME_DETENTION_CURFEW_CANDIDATES,
                    HttpMethod.GET,
                    createEntity(null, Collections.emptyMap()),
                    LIST_OF_OFFENDER_SENTENCE_DETAIL_TYPE,
                    LocalDate.EPOCH);
            buildResourceData(response);

            offenderSentenceDetails = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchOffenderSentencesForPostRequest(final String url, final List<String> idList) {
        init();

        try {
            final var response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    createEntity(idList),
                    LIST_OF_OFFENDER_SENTENCE_DETAIL_TYPE);
            buildResourceData(response);

            offenderSentenceDetails = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private String initialiseUrlModifier(final StringBuilder urlModifier) {
        return urlModifier.length() > 0 ? "&" : "?";
    }

    @Deprecated
    public void verifySentenceTermsOld() {
        assertThat(offenderSentenceTerms).isEqualTo(new OffenderSentenceTerms(
                -3L, 2, 1, null, "R", "Prohibited Activity", LocalDate.of(2015, 3, 16), 5, null, null, null, false, null ,0.0 , null, null, null));
    }

    public void verifySentenceTerms(List<OffenderSentenceTerms> expected) {
        assertThat(offenderSentenceTermsList).asList().containsAll(expected);
    }


}
