package uk.gov.justice.hmpps.prison.service;

import java.util.function.Supplier;

public class ConflictingRequestException extends RuntimeException implements Supplier<ConflictingRequestException> {

    public static ConflictingRequestException withMessage(final String message) {
        return new ConflictingRequestException(message);
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
