package uk.gov.justice.hmpps.prison.service;

import uk.gov.justice.hmpps.prison.exception.ApplicationSpecificException;

import java.util.function.Supplier;

public class ConflictingRequestException extends ApplicationSpecificException implements Supplier<ConflictingRequestException> {


    public static ConflictingRequestException withMessage(final String message) {
        return new ConflictingRequestException(message);
    }
    public static ConflictingRequestException withMessage(final String message, Integer customErrorCode) {
        ConflictingRequestException ex = new ConflictingRequestException(message);
        ex.setErrorCode(customErrorCode);
        return ex;
    }

    public ConflictingRequestException(final String message) {
        super(message);
    }

    @Override
    public ConflictingRequestException get() {
        return new ConflictingRequestException(getMessage());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
