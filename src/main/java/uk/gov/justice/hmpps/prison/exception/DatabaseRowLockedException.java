package uk.gov.justice.hmpps.prison.exception;

import java.util.function.Supplier;

public class DatabaseRowLockedException extends RuntimeException implements Supplier<DatabaseRowLockedException> {
    private static final String DEFAULT_MESSAGE_FOR_ID_FORMAT = "Resource locked, possibly in use in P-Nomis.";

    private String developerMessage;

    public static DatabaseRowLockedException withMessage(final String message) {
        return new DatabaseRowLockedException(message);
    }

    public DatabaseRowLockedException(final String message) {
        super(DEFAULT_MESSAGE_FOR_ID_FORMAT);
        developerMessage = message;
    }

    @Override
    public DatabaseRowLockedException get() {
        return this;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }
}
