package net.syscon.elite.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Configurable
@Service
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(final Authentication auth) throws AuthenticationException {

		final String username = auth.getName();
		final String password = auth.getCredentials().toString();
		

		

		// to add more logic
		final List<GrantedAuthority> grantedAuths = new ArrayList<>();
		grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
		return new UsernamePasswordAuthenticationToken(username, password, grantedAuths);

	}

	@Override
	public boolean supports(final Class<?> authentication) {
		return true;
	}

}