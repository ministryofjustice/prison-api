package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CurfewSteps extends CommonSteps {
    private static final String CURFEW_CHECKS_PASSED_URI = API_PREFIX + "/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/checks-passed";
    private static final String CURFEW_APPROVAL_STATUS_URI = API_PREFIX + "/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/approval-status";

    private static final BiConsumer<StringBuilder, String> valueAppender = (b, value) -> b.append('"').append(value).append('"');
    private static final BiConsumer<StringBuilder, String> bareValueAppender = StringBuilder::append;
    private static final Map<String, String> EXTRA_HEADERS = Collections.singletonMap("Content-Type", MediaType.APPLICATION_JSON_VALUE);

    private HttpStatus httpStatus;
    private ErrorResponse errorResponse;

    private void reset() {
        httpStatus = null;
        errorResponse = null;
    }

    @Step("Update HDC status")
    public void updateHdcStatus(String bookingId, String checksPassed, String dateString) {
        reset();
        putRequest(CURFEW_CHECKS_PASSED_URI, hdcChecksBody(checksPassed, dateString), bookingId);
    }

    @Step("Update HDC approval status")
    public void updateHdcApprovalStatus(String bookingId, String approvalStatus, String dateString) {
        reset();
        putRequest(CURFEW_APPROVAL_STATUS_URI, approvalStatusBody(approvalStatus, dateString), bookingId);
    }

    @Step("verify HTTP status code")
    public void verifyHttpStatusCode(int expected) {
        if (httpStatus != null) {
            assertThat(httpStatus.value()).isEqualTo(expected);
        } else if (errorResponse != null) {
            assertThat(errorResponse.getStatus()).isEqualTo(expected);
        } else {
            fail("No Http status code to verify");
        }
    }

    private void putRequest(String uri, String json, Object... uriVariables) {
        System.out.println("json: " + json);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    uri,
                    HttpMethod.PUT,
                    createEntity(
                            json,
                            EXTRA_HEADERS),
                    Void.class,
                    uriVariables
            );
            httpStatus = response.getStatusCode();
        } catch (RestClientException e) {
            extractHttpStatusCode(e);
        } catch (EliteClientException e) {
            errorResponse = e.getErrorResponse();
        } catch (Throwable t) {
            fail("Unexpected outcome from Http PUT request", t);
        }
    }

    private void extractHttpStatusCode(RestClientException e) {
        if (e instanceof HttpStatusCodeException) {
            httpStatus = ((HttpStatusCodeException) e).getStatusCode();
        }
    }


    private void appendField(StringBuilder b, String fieldName, String fieldValue, BiConsumer<StringBuilder, String> appender) {
        if (fieldValue == null) return;
        if (b.length() > 0) b.append(',');
        b.append('"').append(fieldName).append("\":");
        appender.accept(b, fieldValue);
    }

    private void appendField(StringBuilder b, String fieldName, String fieldValue) {
        appendField(b, fieldName, fieldValue, valueAppender);
    }

    private void appendBareField(StringBuilder b, String fieldName, String fieldValue) {
        appendField(b, fieldName, fieldValue, bareValueAppender);
    }

    private String wrap(StringBuilder fields) {
        return '{' + fields.append('}').toString();
    }

    private String hdcChecksBody(String checksPassed, String dateString) {
        StringBuilder b = new StringBuilder();
        appendField(b, "date", dateString);
        appendBareField(b, "passed", checksPassed);
        return wrap(b);
    }

    private String approvalStatusBody(String approvalStatus, String dateString) {
        StringBuilder b = new StringBuilder();
        appendField(b, "date", dateString);
        appendField(b, "approvalStatus", approvalStatus);
        return wrap(b);
    }
}
