package net.syscon.elite.security.jwt;

import net.syscon.elite.security.DeviceFingerprint;
import net.syscon.elite.security.UserDetailsImpl;
import net.syscon.elite.security.UserSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private TokenSettings tokenSettings;

	@Autowired
	private TokenManagement tokenManagement;

	@Value("${security.authentication.header:Authorization}")
	private String authenticationHeader;

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final DeviceFingerprint deviceFingerprint = DeviceFingerprint.setAndGet(httpRequest);
		final String header = httpRequest.getHeader(authenticationHeader);

		String token = null;
		Object userPrincipal = null;
		final String uri = httpRequest.getRequestURI();

		if (header != null) {
			final int index = header.indexOf(tokenSettings.getSchema());

			if (index > -1) {
				token = header.substring(index + tokenSettings.getSchema().length()).trim();
				userPrincipal = tokenManagement.getUserPrincipalFromToken(token);
			}
		}

		UserDetailsImpl userDetails = UserSecurityUtils.toUserDetails(userPrincipal);

		if ((userDetails != null) && (SecurityContextHolder.getContext().getAuthentication() == null)) {
			if (tokenManagement.validateToken(token, userDetails, deviceFingerprint, uri.endsWith("/users/token"))) {

				if (log.isDebugEnabled()) {
					log.debug("--passing control to filterChain for \"{}\" from \"{}\"--", httpRequest.getRequestURL(), request.getRemoteAddr());
					log.debug("User {} has roles {}", userDetails.getUsername(), userDetails.getAuthorities());
				}

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		
		chain.doFilter(request, response);
	}
}
