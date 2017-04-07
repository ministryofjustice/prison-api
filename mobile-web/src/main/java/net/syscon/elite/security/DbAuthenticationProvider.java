package net.syscon.elite.security;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.persistence.UserRepository;

@Configurable
@Service
public class DbAuthenticationProvider implements AuthenticationProvider {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${spring.datasource.hikari.driver-class-name}")
	private String jdbcDriver;

	@Value("${spring.datasource.hikari.jdbc-url}")
	private String jdbcUrl;
	
	@Inject
	private UserRepository userRepository;
	
	
	@PostConstruct
	public void postConstruct() {
		try {
			Class.forName(jdbcDriver);
		} catch (final ClassNotFoundException e) {
			throw new EliteRuntimeException(e.getMessage(), e);
		}
	}
	
	@Override
	public Authentication authenticate(final Authentication auth) throws AuthenticationException {
		final String username = auth.getName();
		final String password = auth.getCredentials().toString();
		try (final Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
			conn.close();
			return new UsernamePasswordAuthenticationToken(username, password, userRepository.findAuthorities(username));
		} catch (final SQLException ex) {
			logger.error(ex.getMessage(), ex);
			throw new BadCredentialsException(ex.getMessage(), ex);
		}
	}

	@Override
	public boolean supports(final Class<?> authentication) {
		return true;
	}

}