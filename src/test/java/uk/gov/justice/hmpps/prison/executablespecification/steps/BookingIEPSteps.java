package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

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
    private static final String BOOKING_IEP_SUMMARY_API_POST = API_PREFIX + "bookings/iepSummary";

    private static final String BOOKING_IEP_SUMMARY_WITH_DETAILS_QUERY = "?withDetails=true";

    private PrivilegeSummary privilegeSummary;
    private List<PrivilegeSummary> privilegeSummaries;

    protected void init() {
        super.init();

        privilegeSummary = null;
        privilegeSummaries = null;
    }

    @Step("Get booking IEP summary")
    public void getBookingIEPSummary(final Long bookingId, final boolean withDetails) {
        dispatchRequest(bookingId, withDetails);
    }

    @Step("Verify current IEP level")
    public void verifyCurrentIEPLevel(final String expectedIEPLevel) {
        assertThat(privilegeSummary.getIepLevel()).isEqualTo(expectedIEPLevel);
    }

    @Step("Verify IEP detail record count")
    public void verifyIEPDetailRecordCount(final int expectedDetailCount) {
        assertThat(privilegeSummary.getIepDetails()).hasSize(expectedDetailCount);
    }

    @Step("Verify days since IEP review")
    public void verifyDaysSinceReview(final String iepDate) {
        if (StringUtils.isNotBlank(iepDate)) {
            // When there is an IEP date, days since review is expected to be logical days between current system date
            // and provided IEP date.
            final var ldIEPDate = LocalDate.parse(iepDate, DateTimeFormatter.ISO_LOCAL_DATE);
            final var expectedDaysSinceReview = DAYS.between(ldIEPDate, now());

            assertThat(privilegeSummary.getDaysSinceReview()).isEqualTo(Long.valueOf(expectedDaysSinceReview).intValue());
        } else {
            // When there is no release date, days remaining is expected to be null.
            assertThat(privilegeSummary.getDaysSinceReview()).isNull();
        }
    }

    public void verifyIepEntry(final Long bookingId, final String iepLevel, final Integer iepDetailCount, final LocalDate iepDate) {
        final var count = privilegeSummaries
                .stream()
                .filter(
                        privilegeSummary -> privilegeSummary.getBookingId().equals(bookingId) &&
                                privilegeSummary.getIepLevel().equals(iepLevel) &&
                                privilegeSummary.getIepDetails().size() == iepDetailCount &&
                                privilegeSummary.getIepDate().equals(iepDate))
                .count();

        assertThat(count).isEqualTo(1);
    }

    public void getBookingIEPSummaryForOffenders(final List<String> bookings, final boolean withDetails) {
        init();

        final var bookingIdParameters = bookings
                .stream()
                .map(booking -> String.format("bookings=%s", booking))
                .collect(Collectors.joining("&"));

        final var queryParameters = (withDetails ? BOOKING_IEP_SUMMARY_WITH_DETAILS_QUERY + "&" : "?") + bookingIdParameters;

        try {
            final var response =
                    restTemplate.exchange(
                            BOOKING_IEP_SUMMARY_API_FOR_OFFENDERS_URL + queryParameters,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<PrivilegeSummary>>() {
                            },
                            bookings);

            privilegeSummaries = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getBookingIEPSummaryForBookingIds(final List<String> bookingIds) {
        init();

        try {
            final var response =
                restTemplate.exchange(
                    BOOKING_IEP_SUMMARY_API_POST,
                    HttpMethod.POST,
                    createEntity(bookingIds),
                    new ParameterizedTypeReference<List<PrivilegeSummary>>() {
                    });

            privilegeSummaries = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchRequest(final Long bookingId, final boolean withDetails) {
        init();

        try {
            final var response =
                    restTemplate.exchange(
                            BOOKING_IEP_SUMMARY_API_URL + (withDetails ? BOOKING_IEP_SUMMARY_WITH_DETAILS_QUERY : ""),
                            HttpMethod.GET,
                            createEntity(),
                            PrivilegeSummary.class,
                            bookingId);

            privilegeSummary = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}
