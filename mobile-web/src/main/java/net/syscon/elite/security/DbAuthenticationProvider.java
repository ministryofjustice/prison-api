package net.syscon.elite.security;

import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.persistence.impl.UserRepositoryImpl;
import net.syscon.util.SQLProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Configurable
@Service
public class DbAuthenticationProvider implements AuthenticationProvider {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final SQLProvider sqlProvider = new SQLProvider();

	@Value("${driver.class.name}")
	private String jdbcDriver;

	@Value("${app.jdbc.url}")
	private String jdbcUrl;

	@PostConstruct
	public void postConstruct() {
		try {
			Class.forName(jdbcDriver);
			final String filename = String.format("sqls/%s.sql", UserRepositoryImpl.class.getSimpleName());
			sqlProvider.loadFromClassLoader(filename);
		} catch (final ClassNotFoundException e) {
			throw new EliteRuntimeException(e.getMessage(), e);
		}
	}
	
	@Override
	public Authentication authenticate(final Authentication auth) throws AuthenticationException {
		final String username = auth.getName().toUpperCase();
		final String password = auth.getCredentials().toString();
		try (final Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
			logger.debug(String.format("User %s logged with success!", username));
			final Set<GrantedAuthority> authorities = getUserAuthorities(conn, username);
			conn.close();
			return new UsernamePasswordAuthenticationToken(username, password, authorities);
		} catch (final SQLException ex) {
			logger.error(ex.getMessage(), ex);
			throw new BadCredentialsException(ex.getMessage(), ex);
		}
	}
	
	private Set<GrantedAuthority> getUserAuthorities(final Connection conn, final String username) {
		final Set<GrantedAuthority> authorities = new HashSet<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		return authorities;
	}

	@Override
	public boolean supports(final Class<?> authentication) {
		return true;
	}


}