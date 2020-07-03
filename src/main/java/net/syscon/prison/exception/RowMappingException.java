package net.syscon.prison.exception;


@SuppressWarnings("serial")
public class RowMappingException extends RuntimeException {

    public RowMappingException() {
    }

    public RowMappingException(final String s) {
        super(s);
    }

    public RowMappingException(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public RowMappingException(final Throwable throwable) {
        super(throwable);
    }

    public RowMappingException(final String s, final Throwable throwable, final boolean b, final boolean b1) {
        super(s, throwable, b, b1);
    }
}
