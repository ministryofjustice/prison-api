package net.syscon.elite.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.syscon.elite.exception.EliteRuntimeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {


	@Value("${jwt.schema}")
	private String authorizationSchema;


	@Value("${jwt.header}")
	private String tokenHeader;

	@Inject
	private TokenManager tokenManager;

	@Inject
	private UserDetailsService userDetailsService;
	


	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final String header = httpRequest.getHeader(this.tokenHeader);
		String authToken = null;
		String username = null;
		if (header != null) {
			final int index = header.indexOf(authorizationSchema);
			if (index < 0) {
				throw new EliteRuntimeException("Authorization Schema not supported");
			}
			authToken = header.substring(index + authorizationSchema.length()).trim();
			username = this.tokenManager.getUsernameFromToken(authToken);
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
			if (this.tokenManager.validateToken(authToken, userDetails)) {
				final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}

		chain.doFilter(request, response);
	}

}
