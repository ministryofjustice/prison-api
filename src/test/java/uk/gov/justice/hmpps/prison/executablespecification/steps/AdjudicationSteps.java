package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSummary;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Award;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AdjudicationSteps extends CommonSteps {
    private static final String BOOKING_ADJUDICATIONS_API_URL = API_PREFIX + "bookings/{bookingId}/adjudications?";
    private static final String AWARD_CUTOFF_DATE_DATE_PREFIX = "&awardCutoffDate=";
    private static final String ADJUDICATION_CUTOFF_DATE_PREFIX = "&adjudicationCutoffDate=";
    private AdjudicationSummary summary;
    private int index;

    @Step("Get offender adjudication summary")
    public void getAdjudicationSummary(final Long bookingId, final String awardCutoffDate, final String adjudicationCutoffDate) {
        doSingleResultApiCall(bookingId, awardCutoffDate, adjudicationCutoffDate);
    }

    @Step("Verify value of field in summary")
    public void verifyAwardField(final String field, final String value) throws ReflectiveOperationException {
        verifyField(summary.getAwards().get(index), field, value);
    }

    private void doSingleResultApiCall(final long bookingId, final String awardCutoffDate, final String adjudicationCutoffDate) {
        init();
        try {
            var url = BOOKING_ADJUDICATIONS_API_URL;
            if (StringUtils.isNotBlank(awardCutoffDate)) {
                url += AWARD_CUTOFF_DATE_DATE_PREFIX + awardCutoffDate;
            }
            if (StringUtils.isNotBlank(adjudicationCutoffDate)) {
                url += ADJUDICATION_CUTOFF_DATE_PREFIX + adjudicationCutoffDate;
            }
            final var response = restTemplate.exchange(url, HttpMethod.GET, createEntity(),
                    AdjudicationSummary.class, bookingId);
            summary = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        summary = null;
    }

    public void verifyNoAwards() {
        assertThat(summary.getAwards().isEmpty()).as("There are " + summary.getAwards().size() + " awards").isTrue();
    }

    public void setIndex(final int i) {
        index = i;
    }

    public void verifyAwardsNumber(final int n) {
        assertThat(summary.getAwards()).hasSize(n);
    }

    public void verifyAdjudicationCount(final Integer n) {
        assertThat(summary.getAdjudicationCount()).isEqualTo(n);
    }

    public void verifyAwards(final List<Award> expected) {
        final var expectedIterator = expected.iterator();
        final var awardsIterator = summary.getAwards().iterator();
        while (expectedIterator.hasNext()) {
            final var expectedThis = expectedIterator.next();
            final var actualThis = awardsIterator.next();
            assertThat(actualThis.getSanctionCode()).isEqualTo(expectedThis.getSanctionCode());
            assertThat(actualThis.getSanctionCodeDescription()).isEqualTo(expectedThis.getSanctionCodeDescription());
            assertThat(actualThis.getMonths()).isEqualTo(expectedThis.getMonths());
            assertThat(actualThis.getDays()).isEqualTo(expectedThis.getDays());
            if (expectedThis.getLimit() == null) {
                assertThat(actualThis.getLimit()).isNull();
            } else {
                assertThat(actualThis.getLimit()).isEqualByComparingTo(expectedThis.getLimit());
            }
            assertEqualsBlankIsNull(expectedThis.getComment(), actualThis.getComment());
            assertThat(actualThis.getEffectiveDate()).isEqualTo(expectedThis.getEffectiveDate());
        }
        assertThat(awardsIterator.hasNext()).as("Too many actual awards").isFalse();
    }
}
