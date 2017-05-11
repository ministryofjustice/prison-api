package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.security.jwt.TokenManagement;
import net.syscon.elite.security.jwt.TokenSettings;
import net.syscon.elite.service.UserService;
import net.syscon.elite.web.api.model.*;
import net.syscon.elite.web.api.resource.UsersResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Base64;
import java.util.List;

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
	private UserService userService;


	@Override
	public GetUsersByUsernameResponse getUsersByUsername(String username) throws Exception {
		try {
			final UserDetails user = userService.getUserByUsername(username.toUpperCase());
			return GetUsersByUsernameResponse.withJsonOK(user);
		} catch (final EliteRuntimeException ex) {
			log.error(ex.getMessage());
			final HttpStatus httpStatus = new HttpStatus("404", "404", "User Not Found", "User Not Found", "");
			return GetUsersByUsernameResponse.withJsonNotFound(httpStatus);
		}
	}

	@Override
	public GetUsersMeResponse getUsersMe() throws Exception {
		final UserDetails user = getCurrentUser();
		return GetUsersMeResponse.withJsonOK(user);
	}

	@Override
	public PostUsersLoginResponse postUsersLogin(final String credentials, final AuthLogin authLogin) throws Exception {
		Token token = null;
		try {
			String username = null;
			String password = null;
			if (authLogin != null) {
				username = authLogin.getUsername().toUpperCase();
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
				log.debug("Trying to authenticate the user ", username, " ...");
				final Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
				SecurityContextHolder.getContext().setAuthentication(authentication);
				token = tokenManagement.createToken(username);
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
				final String username = tokenManagement.getUsernameFromToken(encodedToken).toUpperCase();
				token = tokenManagement.createToken(username);
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
		final UserDetails user = getCurrentUser();
		final List<CaseLoad> caseLoads = userService.getCaseLoads(user.getStaffId());
		return GetUsersMeCaseLoadsResponse.withJsonOK(caseLoads);
	}

	private UserDetails getCurrentUser() {
		final UserDetails user = userService.getUserByUsername(UserSecurityUtils.getCurrentUsername());
		return user;
	}

	@Override
	public PutUsersMeActiveCaseLoadResponse putUsersMeActiveCaseLoad(final CaseLoad entity) throws Exception {
		try {
			final UserDetails user = getCurrentUser();
			userService.setActiveCaseLoad(user.getStaffId(), entity.getCaseLoadId());
			return PutUsersMeActiveCaseLoadResponse.withOK();
		} catch (final AccessDeniedException ex) {
			final HttpStatus httpStatus = new HttpStatus("403",  "403", "Not Authorized", "The current user does not have acess to this CaseLoad", "");
			return PutUsersMeActiveCaseLoadResponse.withJsonUnauthorized(httpStatus);
		}
	}

	@Override
	public GetUsersMeActiveCaseLoadResponse getUsersMeActiveCaseLoad() throws Exception {
		try {
			final UserDetails user = getCurrentUser();
			final CaseLoad caseLoad = userService.getActiveCaseLoad(user.getStaffId());
			return GetUsersMeActiveCaseLoadResponse.withJsonOK(caseLoad);
		} catch (final DataAccessException ex) {
			final HttpStatus httpStatus = new HttpStatus("500",  "500", "Internal Error", "Internal Error", "");
			return GetUsersMeActiveCaseLoadResponse.withJsonBadRequest(httpStatus);
		}
	}


	

}
