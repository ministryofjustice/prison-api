package net.syscon.elite.service.support;

import net.syscon.elite.api.model.ErrorResponse;

public class WrappedErrorResponseException extends RuntimeException {
    private final ErrorResponse errorResponse;

    public WrappedErrorResponseException(final ErrorResponse errorResponse) {
        super(errorResponse.getUserMessage());

        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
