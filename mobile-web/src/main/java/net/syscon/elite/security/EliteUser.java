package net.syscon.elite.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;


@SuppressWarnings("serial")
public class EliteUser implements UserDetails {
	
    private String username;
    private String token;
    private List<GrantedAuthority> authorities = new ArrayList<>();
    
    public EliteUser() {}
    
    public EliteUser( final String username, final String token, final List<? extends GrantedAuthority> authorities) {
    	this(username, token, authorities.toArray(new GrantedAuthority[0]));
    }
    
    public EliteUser( final String username, final String token, final GrantedAuthority ... authorities) {
        this.username = username;
        this.authorities.clear();
        for (final GrantedAuthority authority: authorities) {
        	this.authorities.add(authority);
        }
        this.token = token;
    }
    
    public void setUsername(final String username) {
		this.username = username;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public void setAuthorities(final List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@Override
    public String getUsername() {
        return username;
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
    public String getToken() {
        return token;
    }
    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    @Override
    public String getPassword() {
        return null;
    }
}