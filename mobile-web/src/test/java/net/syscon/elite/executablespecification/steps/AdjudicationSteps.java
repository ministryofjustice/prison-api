package net.syscon.elite.executablespecification.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.syscon.elite.api.model.AdjudicationDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class AdjudicationSteps extends CommonSteps {
    private static final String BOOKING_ADJUDICATIONS_API_URL = API_PREFIX + "bookings/{bookingId}/adjudications";
    private static final String FROM_DATE_PREFIX = "?fromDate=";
    private AdjudicationDetail details;
    private int index;

    @Step("Get offender adjudication details")
    public void getAwards(Long bookingId, String fromDate) {
        doSingleResultApiCall(bookingId, fromDate);
    }

    @Step("Verify value of field in details")
    public void verifyAwardField(String field, String value) throws ReflectiveOperationException {
        verifyField(details.getAwards().get(index), field, value);
    }

    private void doSingleResultApiCall(long bookingId, String fromDate) {
        init();
        try {
            String url = BOOKING_ADJUDICATIONS_API_URL;
            if (StringUtils.isNotBlank(fromDate)) {
                url += FROM_DATE_PREFIX + fromDate;
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
}
