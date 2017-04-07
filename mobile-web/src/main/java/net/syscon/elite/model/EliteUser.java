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
	public boolean isAccountNonExpired() {
		return super.getAccountNonExpired() != null? super.getAccountNonExpired().booleanValue(): false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return super.getAccountNonLocked() != null? super.getAccountNonLocked().booleanValue(): false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return super.getCredentialsNonExpired() != null? super.getCredentialsNonExpired().booleanValue(): false;
	}

	@Override
	public boolean isEnabled() {
		return super.getEnabled() != null? super.getEnabled().booleanValue(): false;
	}

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
	
	@Override
	@JsonIgnore
	public String getPassword() {
		return null;
	}



}

