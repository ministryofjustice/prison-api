package net.syscon.elite.security;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("serial")
public class UserDetailsImpl implements UserDetails {
	private final String username;
	private final String password;
	private final Set<GrantedAuthority> authorities = new TreeSet<>();
	public UserDetailsImpl(final String username, final String password, final Collection<GrantedAuthority> authorities) {
		this.username = username;
		this.password = password;
		this.authorities.addAll(authorities);
	}
	@Override public boolean isEnabled() { return true; }
	@Override public boolean isCredentialsNonExpired() { return true; }
	@Override public boolean isAccountNonLocked() { return true; }
	@Override public boolean isAccountNonExpired() { return true; }
	@Override public String getUsername() { return username; }
	@Override public String getPassword() { return password; }
	@Override public Set<GrantedAuthority> getAuthorities() { return authorities; }
}
