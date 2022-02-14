package uk.gov.justice.hmpps.prison.util;

import org.springframework.core.env.Environment;

import java.util.Arrays;

public class ProfileUtil {

    public static boolean isNomisProfile(Environment env) {
        return Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.contains("nomis"));
    }

    public static boolean isInMemoryDb(Environment env) {
        return Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.contains("h2"));
    }
}
