package net.syscon.elite.security.jwt;

import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("serial")
public class JwtTokenMissingException extends AuthenticationException {
    public JwtTokenMissingException(final String msg) {
        super(msg);
    }
}