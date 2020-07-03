package uk.gov.justice.hmpps.prison.test;

import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;

/**
 * A client-side exception which includes {@link ErrorResponse} payload.
 */
public class PrisonApiClientException extends RuntimeException {
    private ErrorResponse errorResponse;

    public PrisonApiClientException(final ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
