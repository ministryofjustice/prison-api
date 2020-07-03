package net.syscon.prison.service;

import java.util.function.Supplier;

public class ConfigException extends RuntimeException implements Supplier<ConfigException> {

    public static ConfigException withMessage(final String message) {
        return new ConfigException(message);
    }

    public static ConfigException withMessage(final String message, final Object... args) {
        return new ConfigException(String.format(message, args));
    }

    public ConfigException(final String message) {
        super(message);
    }

    @Override
    public ConfigException get() {
        return new ConfigException(getMessage());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
