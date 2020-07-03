package net.syscon.prison.test;

import net.syscon.prison.api.model.ErrorResponse;

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
