package net.syscon.elite.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserPrincipalForToken {

    private final String username;

    @JsonCreator
    public UserPrincipalForToken(@JsonProperty("username") String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
