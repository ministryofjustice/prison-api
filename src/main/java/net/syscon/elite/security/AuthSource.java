package net.syscon.elite.security;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum AuthSource {
    NOMIS("nomis"),
    DELIUS("delius"),
    AUTH("auth"),
    NONE("none");

    private final String name;

    private static final Map<String, AuthSource> byName = new HashMap<>();

    static {
        Arrays.stream(AuthSource.values()).forEach(authSource -> byName.put(authSource.name, authSource));
    }

    AuthSource(final String name) {
        this.name = name;
    }

    public static AuthSource fromName(final String name) {
        return byName.getOrDefault(name, NONE);
    }

}
