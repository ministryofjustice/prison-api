package net.syscon.elite.web.api.resource.impl;

import java.util.Base64;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import net.syscon.elite.model.EliteUser;
import net.syscon.elite.security.jwt.TokenManagement;
import net.syscon.elite.security.jwt.TokenSettings;
import net.syscon.elite.web.api.model.AuthLogin;
import net.syscon.elite.web.api.model.CaseLoad;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.Token;
import net.syscon.elite.web.api.resource.UsersResource;

@Component
public class UsersResourceImpl implements UsersResource {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private TokenManagement tokenManagement;

	@Inject
	private TokenSettings tokenSettings;

	@Inject
	private AuthenticationManager authenticationManager;
	
	@Inject
	private UserDetailsService userDetailsService;
	

	@Override
	public GetUsersMeResponse getUsersMe() throws Exception {
		final EliteUser userDetails = (EliteUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return GetUsersMeResponse.withJsonOK(userDetails);
	}

	@Override
	public PostUsersLoginResponse postUsersLogin(final String credentials, final AuthLogin authLogin) throws Exception {
		Token token = null;
		try {
			String username = null;
			String password = null;
			if (authLogin != null) {
				username = authLogin.getUsername();
				password = authLogin.getPassword();
			} else if (credentials != null) {
				final int index = credentials.indexOf(TokenSettings.BASIC_AUTHENTICATION);
				if (index > -1) {
					final String ss = credentials.substring(index + TokenSettings.BASIC_AUTHENTICATION.length()).trim();
					final String usrPwd = new String(Base64.getDecoder().decode(ss));
					final String[] items = usrPwd.split(":");
					if (items.length == 2) {
						username = items[0];
						password = items[1];
					}
				}
			}
			if (username != null && password != null) {
				final Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
				SecurityContextHolder.getContext().setAuthentication(authentication);
				final EliteUser userDetails = (EliteUser) userDetailsService.loadUserByUsername(username);
				token = tokenManagement.createToken(userDetails);
			}
		} catch (final AuthenticationException ex) {
			log.error(ex.getMessage(), ex);
		}
		if (token != null) {
			return PostUsersLoginResponse.withJsonCreated(token.getToken(), token);
		} else {
			final String message = "Authentication Error";
			final HttpStatus httpStatus = new HttpStatus("401", "401", message, message, "");
			return PostUsersLoginResponse.withJsonUnauthorized(httpStatus);
		}
	}

	@Override
	public PostUsersTokenResponse postUsersToken(final String header) throws Exception {
		Token token = null;
		try {
			final int index = header.indexOf(tokenSettings.getSchema());
			if (index > -1) {
				final String encodedToken = header.substring(index + tokenSettings.getSchema().length()).trim();
				final String username = tokenManagement.getUsernameFromToken(encodedToken);
				final EliteUser userDetails = (EliteUser) this.userDetailsService.loadUserByUsername(username);
				token = tokenManagement.createToken(userDetails);
			}

		} catch (final AuthenticationException ex) {
			log.error(ex.getMessage(), ex);
		}

		if (token != null) {
			return PostUsersTokenResponse.withJsonCreated(token.getToken(), token);
		} else {
			final String message = "Authentication Error";
			final HttpStatus httpStatus = new HttpStatus("401", "401", message, message, "");
			return PostUsersTokenResponse.withJsonUnauthorized(httpStatus);
		}
	}

	@Override
	public GetUsersMeCaseLoadsResponse getUsersMeCaseLoads(final int offset, final int limit) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PutUsersMeActiveCaseLoadResponse putUsersMeActiveCaseLoad(final CaseLoad entity) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetUsersMeActiveCaseLoadResponse getUsersMeActiveCaseLoad() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}




}
