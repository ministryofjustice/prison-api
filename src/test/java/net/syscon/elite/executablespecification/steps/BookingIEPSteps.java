package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking IEP Summary feature.
 */
public class BookingIEPSteps extends CommonSteps {
    private static final String BOOKING_IEP_SUMMARY_API_URL = API_PREFIX + "bookings/{bookingId}/iepSummary";
    private static final String BOOKING_IEP_SUMMARY_API_FOR_OFFENDERS_URL = API_PREFIX + "bookings/offenders/iepSummary";

    private static final String BOOKING_IEP_SUMMARY_WITH_DETAILS_QUERY = "?withDetails=true";

    private PrivilegeSummary privilegeSummary;
    private List<PrivilegeSummary> privilegeSummaries;

    protected void init() {
        super.init();

        privilegeSummary = null;
        privilegeSummaries = null;
    }

    @Step("Get booking IEP summary")
    public void getBookingIEPSummary(Long bookingId, boolean withDetails) {
        dispatchRequest(bookingId, withDetails);
    }

    @Step("Verify current IEP level")
    public void verifyCurrentIEPLevel(String expectedIEPLevel) {
        assertThat(privilegeSummary.getIepLevel()).isEqualTo(expectedIEPLevel);
    }

    @Step("Verify IEP detail record count")
    public void verifyIEPDetailRecordCount(int expectedDetailCount) {
        assertThat(privilegeSummary.getIepDetails().size()).isEqualTo(expectedDetailCount);
    }

    @Step("Verify days since IEP review")
    public void verifyDaysSinceReview(String iepDate) {
        if (StringUtils.isNotBlank(iepDate)) {
            // When there is an IEP date, days since review is expected to be logical days between current system date
            // and provided IEP date.
            var ldIEPDate = LocalDate.parse(iepDate, DateTimeFormatter.ISO_LOCAL_DATE);
            var expectedDaysSinceReview = DAYS.between(ldIEPDate, now());

            assertThat(privilegeSummary.getDaysSinceReview()).isEqualTo(Long.valueOf(expectedDaysSinceReview).intValue());
        } else {
            // When there is no release date, days remaining is expected to be null.
            assertThat(privilegeSummary.getDaysSinceReview()).isNull();
        }
    }

    public void verifyIepEntry(Long bookingId, String iepLevel, Integer iepDetailCount, LocalDate iepDate) {
        var count = privilegeSummaries
                .stream()
                .filter(
                        privilegeSummary -> privilegeSummary.getBookingId().equals(bookingId) &&
                        privilegeSummary.getIepLevel().equals(iepLevel) &&
                        privilegeSummary.getIepDetails().size() == iepDetailCount &&
                        privilegeSummary.getIepDate().equals(iepDate))
                .count();

        assertThat(count).isEqualTo(1);
    }

    public void getBookingIEPSummaryForOffenders(List<String> bookings, boolean withDetails) {
        init();

        var bookingIdParameters =  bookings
                .stream()
                .map(booking -> String.format("bookings=%s", booking))
                .collect(Collectors.joining("&"));

        var queryParameters = (withDetails ?  BOOKING_IEP_SUMMARY_WITH_DETAILS_QUERY + "&" : "?") + bookingIdParameters;

        try {
            var response =
                    restTemplate.exchange(
                            BOOKING_IEP_SUMMARY_API_FOR_OFFENDERS_URL + queryParameters,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<PrivilegeSummary>>() {},
                            bookings);

            privilegeSummaries = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchRequest(Long bookingId, boolean withDetails) {
        init();

        try {
            var response =
                    restTemplate.exchange(
                            BOOKING_IEP_SUMMARY_API_URL + (withDetails ? BOOKING_IEP_SUMMARY_WITH_DETAILS_QUERY : ""),
                            HttpMethod.GET,
                            createEntity(),
                            PrivilegeSummary.class,
                            bookingId);

            privilegeSummary = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}
