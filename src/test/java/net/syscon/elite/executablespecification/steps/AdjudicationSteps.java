package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.AdjudicationSummary;
import net.syscon.elite.api.model.Award;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

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
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        summary = null;
    }

    public void verifyNoAwards() {
        assertTrue("There are " + summary.getAwards().size() + " awards", summary.getAwards().isEmpty());
    }

    public void setIndex(final int i) {
        index = i;
    }

    public void verifyAwardsNumber(final int n) {
        assertEquals(n, summary.getAwards().size());
    }

    public void verifyAdjudicationCount(final Integer n) {
        assertEquals(n, summary.getAdjudicationCount());
    }

    public void verifyAwards(final List<Award> expected) {
        final var expectedIterator = expected.iterator();
        final var awardsIterator = summary.getAwards().iterator();
        while (expectedIterator.hasNext()) {
            final var expectedThis = expectedIterator.next();
            final var actualThis = awardsIterator.next();
            assertEquals(expectedThis.getSanctionCode(), actualThis.getSanctionCode());
            assertEquals(expectedThis.getSanctionCodeDescription(), actualThis.getSanctionCodeDescription());
            assertEquals(expectedThis.getMonths(), actualThis.getMonths());
            assertEquals(expectedThis.getDays(), actualThis.getDays());
            if (expectedThis.getLimit() == null) {
                assertNull(actualThis.getLimit());
            } else {
                assertThat(actualThis.getLimit()).isEqualByComparingTo(expectedThis.getLimit());
            }
            assertEqualsBlankIsNull(expectedThis.getComment(), actualThis.getComment());
            assertEquals(expectedThis.getEffectiveDate(), actualThis.getEffectiveDate());
        }
        assertFalse("Too many actual awards", awardsIterator.hasNext());
    }
}
