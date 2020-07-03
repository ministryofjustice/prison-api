package net.syscon.prison.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserPrincipalForToken {

    private final String username;

    @JsonCreator
    public UserPrincipalForToken(@JsonProperty("username") final String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
