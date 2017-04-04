package net.syscon.elite.security.jwt;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@SuppressWarnings("serial")
public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {
	
    private final String token;
    
    public JwtAuthenticationToken(final String token) {
        super(null, null);
        this.token = token;
    }
    
    public String getToken() {
        return token;
    }
    
    @Override
    public Object getCredentials() {
        return null;
    }
    @Override
    public Object getPrincipal() {
        return null;
    }
}