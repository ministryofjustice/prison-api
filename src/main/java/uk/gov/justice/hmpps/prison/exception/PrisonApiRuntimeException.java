package uk.gov.justice.hmpps.prison.exception;

@SuppressWarnings("serial")
public class PrisonApiRuntimeException extends RuntimeException {

    public PrisonApiRuntimeException() {
    }

    public PrisonApiRuntimeException(final String s) {
        super(s);
    }

    public PrisonApiRuntimeException(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public PrisonApiRuntimeException(final Throwable throwable) {
        super(throwable);
    }

    public PrisonApiRuntimeException(final String s, final Throwable throwable, final boolean b, final boolean b1) {
        super(s, throwable, b, b1);
    }
}
