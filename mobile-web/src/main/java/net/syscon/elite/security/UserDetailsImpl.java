package net.syscon.elite.security;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("serial")
public class UserDetailsImpl implements UserDetails {
	private final String username;
	private final String password;
	private final List<GrantedAuthority> authorities;
	public UserDetailsImpl(final String username, final String password, final List<GrantedAuthority> authorities) {
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}
	@Override public boolean isEnabled() { return true; }
	@Override public boolean isCredentialsNonExpired() { return true; }
	@Override public boolean isAccountNonLocked() { return true; }
	@Override public boolean isAccountNonExpired() { return true; }
	@Override public String getUsername() { return username; }
	@Override public String getPassword() { return password; }
	@Override public List<GrantedAuthority> getAuthorities() { return authorities; }
}
