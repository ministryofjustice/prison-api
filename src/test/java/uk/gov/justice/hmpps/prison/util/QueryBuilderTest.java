package uk.gov.justice.hmpps.prison.util;


import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.justice.hmpps.prison.util.DatabaseDialect.HSQLDB;
import static uk.gov.justice.hmpps.prison.util.DatabaseDialect.ORACLE_11;
import static uk.gov.justice.hmpps.prison.util.DatabaseDialect.ORACLE_12;
import static uk.gov.justice.hmpps.prison.util.DatabaseDialect.POSTGRES;

@RunWith(DataProviderRunner.class)
public class QueryBuilderTest {

    private final Map<String, FieldMapper> agencyMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("AGENCY_CODE", new FieldMapper("agencyId"))
            .put("DESCRIPTION", new FieldMapper("description")
            ).build();

    private static final String FROM_AGENCY_LOCATIONS = "FROM AGENCY_LOCATIONS A";
    private static final String INITIAL_SQL = "SELECT A.AGY_LOC_ID AGENCY_CODE, A.DESCRIPTION " + FROM_AGENCY_LOCATIONS;

    private static final QueryBuilderFactory oracle11Builder = new QueryBuilderFactory(ORACLE_11);
    private static final QueryBuilderFactory oracle12Builder = new QueryBuilderFactory(ORACLE_12);
    private static final QueryBuilderFactory postgresBuilder = new QueryBuilderFactory(POSTGRES);
    private static final QueryBuilderFactory hsqldbBuilder = new QueryBuilderFactory(HSQLDB);

    @DataProvider
    public static Object[][] data() {
        return new Object[][]{
                {"eq", "=", "'ITAG'", "'ITAG'"},
                {"neq", "!=", "'ITAG'", "'ITAG'"},
                {"is", "IS", "NULL", "NULL"},
                {"gt", ">", "'ITAG'", "'ITAG'"},
                {"gteq", ">=", "'ITAG'", "'ITAG'"},
                {"lt", "<", "'ITAG'", "'ITAG'"},
                {"lteq", "<=", "'ITAG'", "'ITAG'"},
                {"like", "LIKE", "'ITA%'", "'ITA%'"},
                {"in", "IN", "'X'|'B'", "('X','B')"}
        };
    }


    @Test
    public void buildSqlPreOracle12WithoutPaginationShouldReturnTheSameSQL() {
        final var sql = oracle11Builder.getQueryBuilder(INITIAL_SQL, agencyMapping).removeSpecialChars().build();
        assertThat(sql, equalTo(INITIAL_SQL));
    }

    @Test
    public void buildSqlOracle12WithoutPaginationShouldReturnTheSameSql() {
        final var sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping).removeSpecialChars().build();
        assertThat(sql, equalTo(INITIAL_SQL));
    }

    @Test
    public void buildSqlOracle12AndRowCountShouldReturnOracleNativePaginatedSql() {
        final var sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addRowCount()
                .build();
        final var expectedSql = "SELECT COUNT(*) OVER() RECORD_COUNT, QRY_ALIAS.* FROM ( " + INITIAL_SQL + " ) QRY_ALIAS";
        assertThat(sql, equalTo(expectedSql));
    }

    @Test
    public void buildSqlPostgresAndRowCountShouldReturnOracleNativePaginatedSql() {
        final var sql = postgresBuilder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addRowCount()
                .build();
        final var expectedSql = "SELECT COUNT(*) OVER() RECORD_COUNT, QRY_ALIAS.* FROM ( " + INITIAL_SQL + " ) QRY_ALIAS";
        assertThat(sql, equalTo(expectedSql));
    }

    @Test
    public void buildSqlHsqlDBAndRowCountShouldReturnOracleNativePaginatedSql() {
        final var sql = hsqldbBuilder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addRowCount()
                .build();
        final var expectedSql = "WITH TOTAL_COUNT AS ( SELECT COUNT(*) AS RECORD_COUNT FROM (" + INITIAL_SQL + ") QRY_ALIAS ) SELECT * FROM TOTAL_COUNT, (SELECT QRY_ALIAS.* FROM (" + INITIAL_SQL + ") QRY_ALIAS";
        assertThat(sql, equalTo(expectedSql));
    }

    @Test
    public void buildSqlOracle12AndPaginationShouldReturnOracleNativePaginatedSql() {
        final var sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addPagination()
                .build();
        final var expectedSql = INITIAL_SQL + " OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY";
        assertThat(sql, equalTo(expectedSql));
    }

    @Test
    public void buildSqlPostgresAndPaginationShouldReturnOracleNativePaginatedSql() {
        final var sql = postgresBuilder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addPagination()
                .build();
        final var expectedSql = INITIAL_SQL + " OFFSET (:offset) ROWS FETCH NEXT (:limit) ROWS ONLY";
        assertThat(sql, equalTo(expectedSql));
    }

    @Test
    public void buildSqlHsqlDBAndPaginationShouldReturnOracleNativePaginatedSql() {
        final var sql = hsqldbBuilder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addPagination()
                .build();
        final var expectedSql = INITIAL_SQL + "  OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY";
        assertThat(sql, equalTo(expectedSql));
    }

    @Test
    public void buildSqlPreOracle12AndPaginationShouldReturnSubQueryPaginatedSql() {
        final var sql = oracle11Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addPagination()
                .build();
        final var expectedSql = String.format("SELECT * FROM (SELECT QRY_PAG.*, ROWNUM rnum FROM ( %s  ) QRY_PAG WHERE ROWNUM <= :offset+:limit) WHERE rnum >= :offset+1", INITIAL_SQL);
        assertThat(sql, equalTo(expectedSql));
    }

    @Test
    public void buildSqlUsingAliasColumnWithConditionShouldReturnQueryWithSubQuery() {
        final var sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addQuery("agencyId:eq:'ITAG'")
                .build();
        final var expectedSql = String.format("SELECT QRY_ALIAS.* FROM ( %s ) QRY_ALIAS WHERE AGENCY_CODE = 'ITAG'", INITIAL_SQL);
        assertThat(sql, equalTo(expectedSql));
    }

    @Test
    public void buildSqlUsingAliasColumnWithConditionAndRowCountingShouldReturnQueryWithSubQuery() {
        final var sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addQuery("agencyId:eq:'ITAG'")
                .addRowCount()
                .build();
        final var expectedSql = String.format("SELECT COUNT(*) OVER() RECORD_COUNT, QRY_ALIAS.* FROM ( %s ) QRY_ALIAS WHERE AGENCY_CODE = 'ITAG'", INITIAL_SQL);
        assertThat(sql, equalTo(expectedSql));
    }

    @Test
    @UseDataProvider("data")
    public void buildSqlUsingQueryOperatorsShouldReturnExpectedParam(final String jsonOperator, final String sqlOperator, final String paramValue, final String paramExpected) {
        final var sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
                .removeSpecialChars()
                .addQuery("agencyId:" + jsonOperator + ":" + paramValue)
                .build();
        final var expectedSql = String.format("SELECT QRY_ALIAS.* FROM ( %s ) QRY_ALIAS WHERE AGENCY_CODE %s %s", INITIAL_SQL, sqlOperator, paramExpected);
        assertThat(sql, equalTo(expectedSql));
    }
}
