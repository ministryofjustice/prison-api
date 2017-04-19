package net.syscon.elite.security.jwt;

import net.syscon.elite.security.DeviceFingerprint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private TokenSettings tokenSettings;


	@Inject
	private UserDetailsService userDetailsService;

	@Inject
	private TokenManagement tokenManagement;
	

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final DeviceFingerprint deviceFingerprint = DeviceFingerprint.setAndGet(httpRequest);
		final String header = httpRequest.getHeader(TokenSettings.AUTHORIZATION_HEADER);

		String token = null;
		String username = null;

		if (header != null) {
			final int index = header.indexOf(tokenSettings.getSchema());
			if (index > -1) {
				token = header.substring(index + tokenSettings.getSchema().length()).trim();
				username = tokenManagement.getUsernameFromToken(token);
			}
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			final String uri = httpRequest.getRequestURI();
			final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
			if (tokenManagement.validateToken(token, userDetails, deviceFingerprint, uri.endsWith("/users/token"))) {
				if (log.isDebugEnabled()) {
					log.debug("--passing control to filterChain for \"" + httpRequest.getRequestURL().toString() + "\" from \"" + request.getRemoteAddr() + "\"--");
				}

				final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
				SecurityContextHolder.getContext().setAuthentication(authentication);

			}
		}
		chain.doFilter(request, response);
	}


}
