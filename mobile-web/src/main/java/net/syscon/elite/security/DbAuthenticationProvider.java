package net.syscon.elite.security;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.persistence.impl.UserRepositoryImpl;
import net.syscon.util.SQLProvider;

@Configurable
@Service
public class DbAuthenticationProvider implements AuthenticationProvider, UserDetailsService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Map<String, UserDetailsImpl> userDetailsMap = new ConcurrentHashMap<>();
	private final SQLProvider sqlProvider = new SQLProvider();
	
	@Value("${spring.datasource.hikari.driver-class-name}")
	private String jdbcDriver;

	@Value("${spring.datasource.hikari.jdbc-url}")
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
			conn.close();
			final Set<GrantedAuthority> authorities = getUserAuthorities(conn, username);
			userDetailsMap.put(username, new UserDetailsImpl(username, password, authorities));
			return new UsernamePasswordAuthenticationToken(username, password, authorities);
		} catch (final SQLException ex) {
			logger.error(ex.getMessage(), ex);
			throw new BadCredentialsException(ex.getMessage(), ex);
		}
	}
	
	private Set<GrantedAuthority> getUserAuthorities(final Connection conn, final String username) {
		final Set<GrantedAuthority> authorities = new TreeSet<>();
		try (PreparedStatement stmt = conn.prepareStatement(sqlProvider.get("FIND_ROLES_BY_USERNAME")))  {
			stmt.setString(1, username);
			final ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				final String roleName = defaultIfEmpty(rs.getString("ROLE_CODE"), "").replace('-', '_');
				authorities.add(new SimpleGrantedAuthority(roleName));
			}
			rs.close();
		} catch (final SQLException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return authorities;
	}

	@Override
	public boolean supports(final Class<?> authentication) {
		return true;
	}

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		final UserDetails userDetails = userDetailsMap.get(username);
		if (userDetails == null) {
			throw new UsernameNotFoundException("User not found");
		}
		return userDetails;
	}

}