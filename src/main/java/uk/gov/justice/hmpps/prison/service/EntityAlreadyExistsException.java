package uk.gov.justice.hmpps.prison.service;

import java.util.function.Supplier;

public class EntityAlreadyExistsException extends RuntimeException implements Supplier<EntityAlreadyExistsException> {
    private static final String DEFAULT_MESSAGE_FOR_ID_FORMAT = "Resource with id [%s] already exists.";

    public static EntityAlreadyExistsException withId(final long id) {
        return withId(String.valueOf(id));
    }

    public static EntityAlreadyExistsException withId(final String id) {
        return new EntityAlreadyExistsException(String.format(DEFAULT_MESSAGE_FOR_ID_FORMAT, id));
    }

    public static EntityAlreadyExistsException withMessage(final String message) {
        return new EntityAlreadyExistsException(message);
    }

    public static EntityAlreadyExistsException withMessage(final String message, final Object... args) {
        return new EntityAlreadyExistsException(String.format(message, args));
    }

    public EntityAlreadyExistsException(final String message) {
        super(message);
    }

    @Override
    public EntityAlreadyExistsException get() {
        return new EntityAlreadyExistsException(getMessage());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
