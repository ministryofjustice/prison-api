package net.syscon.elite.exception;

@SuppressWarnings("serial")
public class EliteRuntimeException extends RuntimeException {

    public EliteRuntimeException() {
    }

    public EliteRuntimeException(final String s) {
        super(s);
    }

    public EliteRuntimeException(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public EliteRuntimeException(final Throwable throwable) {
        super(throwable);
    }

    public EliteRuntimeException(final String s, final Throwable throwable, final boolean b, final boolean b1) {
        super(s, throwable, b, b1);
    }
}
