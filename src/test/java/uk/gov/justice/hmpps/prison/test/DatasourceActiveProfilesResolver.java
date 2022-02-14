package uk.gov.justice.hmpps.prison.test;

import org.apache.commons.lang3.StringUtils;
import org.springframework.test.context.ActiveProfilesResolver;

import java.util.Objects;

/**
 * Sets active profiles for integration tests based on an environment property.
 */
public class DatasourceActiveProfilesResolver implements ActiveProfilesResolver {

    @Override
    public String[] resolve(final Class<?> testClass) {
        final var datasourceProfile = System.getenv("api.db.target");
        final var datasourceDialect = System.getenv("api.db.dialect");

        Objects.requireNonNull(datasourceProfile, "'api.db.target' environment variable must be specified.");

        return new String[]{
                datasourceProfile + "-" + StringUtils.defaultIfBlank(datasourceDialect, "h2")
        };
    }
}
