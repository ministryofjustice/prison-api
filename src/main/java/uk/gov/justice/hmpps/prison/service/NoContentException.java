package uk.gov.justice.hmpps.prison.service;

import java.util.function.Supplier;

public class NoContentException extends RuntimeException implements Supplier<uk.gov.justice.hmpps.prison.service.NoContentException> {
    private static final String DEFAULT_MESSAGE_FOR_ID_FORMAT = "Resource has no content.";

    public static uk.gov.justice.hmpps.prison.service.NoContentException withId(final long id) {
        return withId(String.valueOf(id));
    }

    public static uk.gov.justice.hmpps.prison.service.NoContentException withId(final String id) {
        return new uk.gov.justice.hmpps.prison.service.NoContentException(String.format(DEFAULT_MESSAGE_FOR_ID_FORMAT, id));
    }

    public static uk.gov.justice.hmpps.prison.service.NoContentException withMessage(final String message) {
        return new uk.gov.justice.hmpps.prison.service.NoContentException(message);
    }

    public static uk.gov.justice.hmpps.prison.service.NoContentException withMessage(final String message, final Object... args) {
        return new uk.gov.justice.hmpps.prison.service.NoContentException(String.format(message, args));
    }

    public NoContentException(final String message) {
        super(message);
    }

    @Override
    public uk.gov.justice.hmpps.prison.service.NoContentException get() {
        return new uk.gov.justice.hmpps.prison.service.NoContentException(getMessage());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
