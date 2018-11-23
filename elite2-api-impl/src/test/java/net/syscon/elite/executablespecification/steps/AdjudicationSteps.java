package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.AdjudicationDetail;
import net.syscon.elite.api.model.Award;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class AdjudicationSteps extends CommonSteps {
    private static final String BOOKING_ADJUDICATIONS_API_URL = API_PREFIX + "bookings/{bookingId}/adjudications?";
    private static final String AWARD_CUTOFF_DATE_DATE_PREFIX = "&awardCutoffDate=";
    private static final String ADJUDICATION_CUTOFF_DATE_PREFIX = "&adjudicationCutoffDate=";
    private AdjudicationDetail details;
    private int index;

    @Step("Get offender adjudication details")
    public void getAwards(Long bookingId, String awardCutoffDate, String adjudicationCutoffDate) {
        doSingleResultApiCall(bookingId, awardCutoffDate, adjudicationCutoffDate);
    }

    @Step("Verify value of field in details")
    public void verifyAwardField(String field, String value) throws ReflectiveOperationException {
        verifyField(details.getAwards().get(index), field, value);
    }

    private void doSingleResultApiCall(long bookingId, String awardCutoffDate, String adjudicationCutoffDate) {
        init();
        try {
            String url = BOOKING_ADJUDICATIONS_API_URL;
            if (StringUtils.isNotBlank(awardCutoffDate)) {
                url += AWARD_CUTOFF_DATE_DATE_PREFIX + awardCutoffDate;
            }
            if (StringUtils.isNotBlank(adjudicationCutoffDate)) {
                url += ADJUDICATION_CUTOFF_DATE_PREFIX + adjudicationCutoffDate;
            }
            ResponseEntity<AdjudicationDetail> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(),
                    AdjudicationDetail.class, bookingId);
            details = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        details = null;
    }

    public void verifyNoAwards() {
        assertTrue("There are " + details.getAwards().size() + " awards", details.getAwards().isEmpty());
    }

    public void setIndex(int i) {
        index = i;
    }

    public void verifyAwardsNumber(int n) {
        assertEquals(n, details.getAwards().size());
    }

    public void verifyAdjudicationCount(Integer n) {
        assertEquals(n, details.getAdjudicationCount());
    }

    public void verifyAwards(List<Award> expected) {
        final Iterator<Award> expectedIterator = expected.iterator();
        final Iterator<Award> awardsIterator = details.getAwards().iterator();
        while (expectedIterator.hasNext()) {
            final Award expectedThis = expectedIterator.next();
            final Award actualThis = awardsIterator.next();
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
