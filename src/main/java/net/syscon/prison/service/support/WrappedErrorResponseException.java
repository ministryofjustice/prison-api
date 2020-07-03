package net.syscon.prison.service.support;

import net.syscon.prison.api.model.ErrorResponse;

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
