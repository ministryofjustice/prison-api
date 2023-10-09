package uk.gov.justice.hmpps.prison.exception;

@SuppressWarnings("serial")
public class MissingRoleCheckException extends RuntimeException {

    public MissingRoleCheckException(final String s) {
        super(s);
    }
}
