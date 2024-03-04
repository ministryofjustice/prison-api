package uk.gov.justice.hmpps.prison.util

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper
import java.util.stream.Stream

class QueryBuilderTest {
  private val agencyMapping = mapOf(
    "AGENCY_CODE" to FieldMapper("agencyId"),
    "DESCRIPTION" to FieldMapper("description"),
  )

  @Test
  fun buildSqlPreOracle12WithoutPaginationShouldReturnTheSameSQL() {
    val sql = oracle11Builder.getQueryBuilder(INITIAL_SQL, agencyMapping).removeSpecialChars().build()
    assertThat(sql).isEqualTo(INITIAL_SQL)
  }

  @Test
  fun buildSqlOracle12WithoutPaginationShouldReturnTheSameSql() {
    val sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping).removeSpecialChars().build()
    assertThat(sql).isEqualTo(INITIAL_SQL)
  }

  @Test
  fun buildSqlOracle12AndRowCountShouldReturnOracleNativePaginatedSql() {
    val sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
      .removeSpecialChars()
      .addRowCount()
      .build()
    val expectedSql = "SELECT COUNT(*) OVER() RECORD_COUNT, QRY_ALIAS.* FROM ( $INITIAL_SQL ) QRY_ALIAS"
    assertThat(sql).isEqualTo(expectedSql)
  }

  @Test
  fun buildSqlPostgresAndRowCountShouldReturnOracleNativePaginatedSql() {
    val sql = postgresBuilder.getQueryBuilder(INITIAL_SQL, agencyMapping)
      .removeSpecialChars()
      .addRowCount()
      .build()
    val expectedSql = "SELECT COUNT(*) OVER() RECORD_COUNT, QRY_ALIAS.* FROM ( $INITIAL_SQL ) QRY_ALIAS"
    assertThat(sql).isEqualTo(expectedSql)
  }

  @Test
  fun buildSqlHsqlDBAndRowCountShouldReturnOracleNativePaginatedSql() {
    val sql = hsqldbBuilder.getQueryBuilder(INITIAL_SQL, agencyMapping)
      .removeSpecialChars()
      .addRowCount()
      .build()
    val expectedSql =
      "WITH TOTAL_COUNT AS ( SELECT COUNT(*) AS RECORD_COUNT FROM ($INITIAL_SQL) QRY_ALIAS ) SELECT * FROM TOTAL_COUNT, (SELECT QRY_ALIAS.* FROM ($INITIAL_SQL) QRY_ALIAS"
    assertThat(sql).isEqualTo(expectedSql)
  }

  @Test
  fun buildSqlOracle12AndPaginationShouldReturnOracleNativePaginatedSql() {
    val sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
      .removeSpecialChars()
      .addPagination()
      .build()
    val expectedSql = "$INITIAL_SQL OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY"
    assertThat(sql).isEqualTo(expectedSql)
  }

  @Test
  fun buildSqlPostgresAndPaginationShouldReturnOracleNativePaginatedSql() {
    val sql = postgresBuilder.getQueryBuilder(INITIAL_SQL, agencyMapping)
      .removeSpecialChars()
      .addPagination()
      .build()
    val expectedSql = "$INITIAL_SQL OFFSET (:offset) ROWS FETCH NEXT (:limit) ROWS ONLY"
    assertThat(sql).isEqualTo(expectedSql)
  }

  @Test
  fun buildSqlHsqlDBAndPaginationShouldReturnOracleNativePaginatedSql() {
    val sql = hsqldbBuilder.getQueryBuilder(INITIAL_SQL, agencyMapping)
      .removeSpecialChars()
      .addPagination()
      .build()
    val expectedSql = "$INITIAL_SQL  OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY"
    assertThat(sql).isEqualTo(expectedSql)
  }

  @Test
  fun buildSqlPreOracle12AndPaginationShouldReturnSubQueryPaginatedSql() {
    val sql = oracle11Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
      .removeSpecialChars()
      .addPagination()
      .build()
    val expectedSql =
      "SELECT * FROM (SELECT QRY_PAG.*, ROWNUM rnum FROM ( $INITIAL_SQL  ) QRY_PAG WHERE ROWNUM <= :offset+:limit) WHERE rnum >= :offset+1"
    assertThat(sql).isEqualTo(expectedSql)
  }

  @Test
  fun buildSqlUsingAliasColumnWithConditionShouldReturnQueryWithSubQuery() {
    val sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
      .removeSpecialChars()
      .addQuery("agencyId:eq:'ITAG'")
      .build()
    val expectedSql = "SELECT QRY_ALIAS.* FROM ( $INITIAL_SQL ) QRY_ALIAS WHERE AGENCY_CODE = 'ITAG'"
    assertThat(sql).isEqualTo(expectedSql)
  }

  @Test
  fun buildSqlUsingAliasColumnWithConditionAndRowCountingShouldReturnQueryWithSubQuery() {
    val sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
      .removeSpecialChars()
      .addQuery("agencyId:eq:'ITAG'")
      .addRowCount()
      .build()
    val expectedSql = "SELECT COUNT(*) OVER() RECORD_COUNT, QRY_ALIAS.* FROM ( $INITIAL_SQL ) QRY_ALIAS WHERE AGENCY_CODE = 'ITAG'"
    assertThat(sql).isEqualTo(expectedSql)
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  inner class BuildWithData {
    @ParameterizedTest
    @MethodSource("data")
    fun buildSqlUsingQueryOperatorsShouldReturnExpectedParam(
      jsonOperator: String,
      sqlOperator: String?,
      paramValue: String,
      paramExpected: String?,
    ) {
      val sql = oracle12Builder.getQueryBuilder(INITIAL_SQL, agencyMapping)
        .removeSpecialChars()
        .addQuery("agencyId:$jsonOperator:$paramValue")
        .build()
      val expectedSql = "SELECT QRY_ALIAS.* FROM ( $INITIAL_SQL ) QRY_ALIAS WHERE AGENCY_CODE $sqlOperator $paramExpected"
      assertThat(sql).isEqualTo(expectedSql)
    }

    fun data(): Stream<Arguments> = Stream.of(
      arguments("eq", "=", "'ITAG'", "'ITAG'"),
      arguments("neq", "!=", "'ITAG'", "'ITAG'"),
      arguments("is", "IS", "NULL", "NULL"),
      arguments("gt", ">", "'ITAG'", "'ITAG'"),
      arguments("gteq", ">=", "'ITAG'", "'ITAG'"),
      arguments("lt", "<", "'ITAG'", "'ITAG'"),
      arguments("lteq", "<=", "'ITAG'", "'ITAG'"),
      arguments("like", "LIKE", "'ITA%'", "'ITA%'"),
      arguments("in", "IN", "'X'|'B'", "('X','B')"),
    )
  }

  companion object {
    private const val FROM_AGENCY_LOCATIONS = "FROM AGENCY_LOCATIONS A"
    private const val INITIAL_SQL = "SELECT A.AGY_LOC_ID AGENCY_CODE, A.DESCRIPTION $FROM_AGENCY_LOCATIONS"
    private val oracle11Builder = QueryBuilderFactory(DatabaseDialect.ORACLE_11)
    private val oracle12Builder = QueryBuilderFactory(DatabaseDialect.ORACLE_12)
    private val postgresBuilder = QueryBuilderFactory(DatabaseDialect.POSTGRES)
    private val hsqldbBuilder = QueryBuilderFactory(DatabaseDialect.HSQLDB)
  }
}
