package net.syscon.elite.security.jwt;
import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("serial")
public class JwtTokenMalformedException extends AuthenticationException {
	
    public JwtTokenMalformedException(final String msg) {
        super(msg);
    }

	public JwtTokenMalformedException(final String message, final Exception e) {
		super(message, e);
	}
}