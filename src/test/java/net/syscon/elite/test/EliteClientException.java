package net.syscon.elite.test;

import net.syscon.elite.api.model.ErrorResponse;

/**
 * A client-side exception which includes {@link ErrorResponse} payload.
 */
public class EliteClientException extends RuntimeException {
    private ErrorResponse errorResponse;

    public EliteClientException(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
