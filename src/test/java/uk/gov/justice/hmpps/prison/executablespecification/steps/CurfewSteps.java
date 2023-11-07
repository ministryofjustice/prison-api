package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;
import uk.gov.justice.hmpps.prison.repository.OffenderCurfewRepositoryTest;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CurfewSteps extends CommonSteps {
    private static final String CURFEW_CHECKS_PASSED_URI = API_PREFIX + "/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/checks-passed";
    private static final String CURFEW_APPROVAL_STATUS_URI = API_PREFIX + "/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/approval-status";
    private static final String LATEST_HOME_DETENTION_CURFEW_URI = API_PREFIX + "/offender-sentences/booking/{bookingId}/home-detention-curfews/latest";

    private static final BiConsumer<StringBuilder, String> valueAppender = (b, value) -> b.append('"').append(value).append('"');
    private static final BiConsumer<StringBuilder, String> bareValueAppender = StringBuilder::append;
    private static final Map<String, String> EXTRA_HEADERS = Collections.singletonMap("Content-Type", MediaType.APPLICATION_JSON_VALUE);

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

    private HttpStatusCode httpStatus;
    private ErrorResponse errorResponse;

    private void resetErrors() {
        httpStatus = null;
        errorResponse = null;
    }

    private void reset(String bookingId) {
        resetErrors();
        try {
            OffenderCurfewRepositoryTest.createNewCurfewForBookingId(Long.parseLong(bookingId), jdbcTemplate);
        } catch (RuntimeException e) {
            // Some tests make API requests involving bookings that don't exist.  This is deliberate. Their purpose is
            // to confirm that the API behaves appropriately in these circumstances. When a booking doesn't exist it
            // isn't possible to add an OFFENDER_CURFEWS record for that booking and the method call above will throw an
            // exception. This is to be expected and should be ignored.
        }
    }

    @Step("Update HDC status")
    public void updateHdcStatus(final String bookingId, final String checksPassed, final String dateString) {
        reset(bookingId);
        putRequest(CURFEW_CHECKS_PASSED_URI, hdcChecksBody(checksPassed, dateString), bookingId);
    }

    @Step("Update HDC approval status")
    public void updateHdcApprovalStatus(final String bookingId, final String checksPassed, final String approvalStatus, final String refusedReason, final String dateString) {
        reset(bookingId);
        if (StringUtils.isNotEmpty(checksPassed)) {
            putRequest(CURFEW_CHECKS_PASSED_URI, hdcChecksBody(checksPassed, "2019-01-01"), bookingId);
            verifyHttpStatusCode(204);
            resetErrors();
        }
        putRequest(CURFEW_APPROVAL_STATUS_URI, approvalStatusBody(approvalStatus, refusedReason, dateString), bookingId);
    }

    @Step("verify HTTP status code")
    public void verifyHttpStatusCode(final int expected) {
        if (httpStatus != null) {
            assertThat(httpStatus.value()).isEqualTo(expected);
        } else if (errorResponse != null) {
            assertThat(errorResponse.getStatus()).isEqualTo(expected);
        } else {
            fail("No Http status code to verify");
        }
    }

    @Step("Verify Latest Home Detention Curfew")
    public void verifyLatestHomeDetentionCurfew(Long bookingId, Boolean checksPassed, LocalDate checksPassedDate) {
        HomeDetentionCurfew hdc = getHomeDetentionCurfew(bookingId);

        assertThat(hdc)
                .extracting("passed", "checksPassedDate")
                .containsExactly(checksPassed, checksPassedDate);
    }

    @Step("Verify Latest Home Detention Curfew")
    public void verifyLatestHomeDetentionCurfew(Long bookingId, String approvalStatus, String refusedReason, LocalDate approvalStatusDate) {
        HomeDetentionCurfew hdc = getHomeDetentionCurfew(bookingId);

        assertThat(hdc)
                .extracting("approvalStatus", "refusedReason", "approvalStatusDate")
                .containsExactly(approvalStatus, refusedReason, approvalStatusDate);
    }

    private HomeDetentionCurfew getHomeDetentionCurfew(Long bookingId) {
        return restTemplate
                .exchange(
                        LATEST_HOME_DETENTION_CURFEW_URI,
                        HttpMethod.GET,
                        createEntity(null, EXTRA_HEADERS),
                        HomeDetentionCurfew.class,
                        bookingId)
                .getBody();
    }

    private void putRequest(final String uri, final String json, final Object... uriVariables) {
        try {
            final var response = restTemplate.exchange(
                    uri,
                    HttpMethod.PUT,
                    createEntity(
                            json,
                            EXTRA_HEADERS),
                    Void.class,
                    uriVariables
            );
            httpStatus = response.getStatusCode();
        } catch (final RestClientException e) {
            extractHttpStatusCode(e);
        } catch (final PrisonApiClientException e) {
            errorResponse = e.getErrorResponse();
        } catch (final Throwable t) {
            fail("Unexpected outcome from Http PUT request", t);
        }
    }

    private void extractHttpStatusCode(final RestClientException e) {
        if (e instanceof HttpStatusCodeException) {
            httpStatus = ((HttpStatusCodeException) e).getStatusCode();
        }
    }


    private void appendField(final StringBuilder b, final String fieldName, final String fieldValue, final BiConsumer<StringBuilder, String> appender) {
        if (fieldValue == null) return;
        if (fieldValue.length() < 1) return;
        if (b.length() > 0) b.append(',');
        b.append('"').append(fieldName).append("\":");
        appender.accept(b, fieldValue);
    }

    private void appendField(final StringBuilder b, final String fieldName, final String fieldValue) {
        appendField(b, fieldName, fieldValue, valueAppender);
    }

    private void appendBareField(final StringBuilder b, final String fieldName, final String fieldValue) {
        appendField(b, fieldName, fieldValue, bareValueAppender);
    }

    private String wrap(final StringBuilder fields) {
        return '{' + fields.append('}').toString();
    }

    private String hdcChecksBody(final String checksPassed, final String dateString) {
        final var b = new StringBuilder();
        appendField(b, "date", dateString);
        appendBareField(b, "passed", checksPassed);
        return wrap(b);
    }

    private String approvalStatusBody(final String approvalStatus, final String refusedReason, final String dateString) {
        final var b = new StringBuilder();
        appendField(b, "date", dateString);
        appendField(b, "approvalStatus", approvalStatus);
        appendField(b, "refusedReason", refusedReason);
        return wrap(b);
    }
}
