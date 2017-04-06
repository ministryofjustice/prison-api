package net.syscon.elite.web.api.resource.impl;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import net.syscon.elite.security.TokenUtils;
import net.syscon.elite.web.api.model.AuthLogin;
import net.syscon.elite.web.api.model.AuthToken;
import net.syscon.elite.web.api.model.Token;
import net.syscon.elite.web.api.resource.UsersResource;

@Component
public class UsersResourceImpl implements UsersResource {

	@Value("${jwt.header}")
	private String tokenHeader;

	@Inject
	private AuthenticationManager authenticationManager;
	
	@Inject
	private UserDetailsService userDetailsService;
	
	@Inject
	private TokenUtils tokenUtils;

	
	@Override
	public GetUsersMeResponse getUsersMe() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public PostUsersLoginResponse postUsersLogin(final AuthLogin authLogin) throws Exception {

		// Perform the authentication
		final Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authLogin.getUsername(), authLogin.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		// Reload password post-authentication so we can generate token
		final UserDetails userDetails = userDetailsService.loadUserByUsername(authLogin.getUsername());
		
		final String base64Token = this.tokenUtils.generateToken(userDetails);
		final Token token = new Token(base64Token);
		
		return PostUsersLoginResponse.withJsonCreated(token);
		
	}

	@Override
	public PostUsersTokenResponse postUsersToken(final AuthToken entity) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
