package net.syscon.elite.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@SuppressWarnings("serial")
public class UserDetailsImpl implements UserDetails {
    private final String username;
    private final String password;
    private final Set<GrantedAuthority> authorities = new HashSet<>();
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public UserDetailsImpl(final String username, final String password, final Collection<GrantedAuthority> authorities,
                           final Map<String, Object> additionalProperties) {
        this.username = username;
        this.password = password;
        this.authorities.addAll(authorities);

        if (additionalProperties != null) {
            this.additionalProperties.putAll(additionalProperties);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
}
