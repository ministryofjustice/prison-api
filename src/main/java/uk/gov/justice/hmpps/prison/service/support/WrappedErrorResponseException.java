package uk.gov.justice.hmpps.prison.service.support;

import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;

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
