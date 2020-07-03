package net.syscon.prison.test;

import net.syscon.prison.api.model.ErrorResponse;

/**
 * A client-side exception which includes {@link ErrorResponse} payload.
 */
public class EliteClientException extends RuntimeException {
    private ErrorResponse errorResponse;

    public EliteClientException(final ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
