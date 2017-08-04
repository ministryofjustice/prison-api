package net.syscon.elite.test;

import org.springframework.test.context.ActiveProfilesResolver;

import java.util.Objects;

/**
 * Sets active profiles for integration tests based on an environment property.
 */
public class DatasourceActiveProfilesResolver implements ActiveProfilesResolver {

    @Override
    public String[] resolve(Class<?> testClass) {
        String datasourceProfile = System.getenv("api.db.target");

        Objects.requireNonNull(datasourceProfile, "'api.db.target' environment variable must be specified.");

        return new String[] {"noproxy", datasourceProfile, datasourceProfile + "-hsqldb"};
    }
}
