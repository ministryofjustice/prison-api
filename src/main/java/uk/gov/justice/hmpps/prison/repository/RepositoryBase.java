package uk.gov.justice.hmpps.prison.repository;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.util.QueryBuilderFactory;
import uk.gov.justice.hmpps.prison.util.SQLProvider;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public abstract class RepositoryBase {
    @Autowired
    protected NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    protected SQLProvider sqlProvider;

    @Autowired
    protected QueryBuilderFactory queryBuilderFactory;

    @Value("${datasource.jdbc.fetch_size:100}")
    protected int fetchSize;

    @PostConstruct
    public void initSql() {
        sqlProvider.loadSql(getClass().getSimpleName().replace('.', '/'));
        getJdbcTemplateBase().setFetchSize(fetchSize);
    }

    public JdbcTemplate getJdbcTemplateBase() {
        return ((JdbcTemplate) jdbcTemplate.getJdbcOperations());
    }

    protected MapSqlParameterSource createParams(final Object... params) {
        return new MapSqlParameterSource(array2map(params));
    }

    protected MapSqlParameterSource createParamSource(final PageRequest pageRequest, final Object... params) {
        Validate.notNull(pageRequest, "Page request must be provided.");


        final var parameterSource = new MapSqlParameterSource();

        parameterSource.addValue("offset", pageRequest.getOffset());
        parameterSource.addValue("limit", pageRequest.getLimit());

        parameterSource.addValues(array2map(params));

        return parameterSource;
    }

    // Converts one-dimensional array of key/value pairs to map
    private Map<String, Object> array2map(final Object... params) {
        Validate.isTrue(params.length % 2 == 0, "Additional parameters must be provided as key/value pairs.");

        final Map<String, Object> theMap = new HashMap<>();

        for (var i = 0; i < params.length / 2; i++) {
            final var j = i * 2;

            theMap.put(params[j].toString(), params[j + 1]);
        }

        return theMap;
    }

    public String getQuery(final String name) {
        return sqlProvider.get(name);
    }
}
