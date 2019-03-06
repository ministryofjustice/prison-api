package net.syscon.elite.service;

import java.util.function.Supplier;

public class AllocationException extends RuntimeException implements Supplier<AllocationException> {

    public static AllocationException withMessage(final String message) {
        return new AllocationException(message);
    }

    public static AllocationException withMessage(final String message, final Object... args) {
        return new AllocationException(String.format(message, args));
    }

    public AllocationException(final String message) {
        super(message);
    }

    @Override
    public AllocationException get() {
        return new AllocationException(getMessage());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
