package net.syscon.elite.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.syscon.elite.web.api.model.UserDetails;

@SuppressWarnings("serial")
public class EliteUser extends UserDetails implements org.springframework.security.core.userdetails.UserDetails {


	@Override
	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities() {
		final List<GrantedAuthority> authorities = new ArrayList<>();
		if (super.getRoles() != null) {
			for (final String role: super.getRoles()) {
				authorities.add(new SimpleGrantedAuthority(role));
			}
		}
		return authorities;
	}

	public void setAuthorities(final Collection<? extends GrantedAuthority> values) {
		final List<String> roles = new ArrayList<>();
		for (final GrantedAuthority authority: values) {
			roles.add(authority.getAuthority());
		}
		super.setRoles(roles);
	}

	@Override
	@JsonIgnore
	public String getPassword() {
		return null;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return true;
	}


}

