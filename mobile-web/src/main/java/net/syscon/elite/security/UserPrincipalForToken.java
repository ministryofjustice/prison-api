package net.syscon.elite.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserPrincipalForToken {

    private final String username;
    private final List<String> roles;

    @JsonCreator
    public UserPrincipalForToken(@JsonProperty("username") String username, @JsonProperty("roles") List<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }
}
