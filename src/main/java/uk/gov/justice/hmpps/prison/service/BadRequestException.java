package uk.gov.justice.hmpps.prison.service;

import uk.gov.justice.hmpps.prison.exception.ApplicationSpecificException;

import java.util.function.Supplier;

public class BadRequestException extends ApplicationSpecificException implements Supplier<BadRequestException> {
    private static final String DEFAULT_MESSAGE_FOR_DATA_FORMAT = "Invalid Data supplied [%s]";

    public static BadRequestException withId(final long data) {
        return withId(String.valueOf(data));
    }

    public static BadRequestException withId(final String data) {
        return new BadRequestException(String.format(DEFAULT_MESSAGE_FOR_DATA_FORMAT, data));
    }

    public static BadRequestException withMessage(final String message) {
        return new BadRequestException(message);
    }

    public static BadRequestException withMessage(final String message, Integer customErrorCode) {
        BadRequestException ex = new BadRequestException(message);
        ex.setErrorCode(customErrorCode);
        return ex;
    }

    public static BadRequestException withMessage(final String message, final Object... args) {
        return new BadRequestException(String.format(message, args));
    }

    public BadRequestException(final String message) {
        super(message);
    }

    @Override
    public BadRequestException get() {
        return new BadRequestException(getMessage());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
