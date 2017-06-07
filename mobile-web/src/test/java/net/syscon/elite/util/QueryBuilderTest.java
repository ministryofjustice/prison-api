package net.syscon.elite.util;


import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.util.QueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(DataProviderRunner.class)
public class QueryBuilderTest {
	
	private final Map<String, FieldMapper> agencyMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("AGENCY_CODE", new FieldMapper("agencyId"))
		.put("DESCRIPTION", new FieldMapper("description")
	).build();
	
	private static final String INITIAL_SQL = "SELECT A.AGY_LOC_ID AGENCY_CODE, A.DESCRIPTION FROM AGENCY_LOCATIONS A";
	
	private static final boolean ORACLE_11 = true;
	private static final boolean ORACLE_12 = false;

	@DataProvider
	public static Object[][] data() {
		return new Object[][] {
			{ "eq"  ,  "="   , "'ITAG'" , "'ITAG'"    },
			{ "neq" ,  "!="  , "'ITAG'" , "'ITAG'"    },
			{ "is"  ,  "IS"  , "NULL"   , "NULL"      },
			{ "gt"  ,  ">"   , "'ITAG'" , "'ITAG'"    },
			{ "gteq",  ">="  , "'ITAG'" , "'ITAG'"    },
			{ "lt"  ,  "<"   , "'ITAG'" , "'ITAG'"    },
			{ "lteq",  "<="  , "'ITAG'" , "'ITAG'"    },
			{ "like",  "LIKE", "'ITA%'" , "'ITA%'"    },
			{ "in"  ,  "IN"  , "'X'|'B'", "('X','B')" }
		};
	}


	@Test
	public void buildSQL_PreOracle12WithoutPagination_ShouldReturnTheSameSQL() {
		final String sql = new QueryBuilder.Builder(INITIAL_SQL, agencyMapping, ORACLE_11).build();
		assertThat(sql, equalTo(INITIAL_SQL));
	}
	
	@Test
	public void buildSQL_Oracle12WithoutPagination_ShouldReturnTheSameSql() {
		final String sql = new QueryBuilder.Builder(INITIAL_SQL, agencyMapping, ORACLE_12).build();
		assertThat(sql, equalTo(INITIAL_SQL));
	}
	
	@Test
	public void buildSQL_Oracle12AndPagination_ShouldReturnOracleNativePaginatedSql() {
		final String sql = new QueryBuilder.Builder(INITIAL_SQL, agencyMapping, ORACLE_12)
				.setRemoveSpecialChars(true)
				.addPagedQuery()
				.build();
		final String expectedSql = INITIAL_SQL + " OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY";
		assertThat(sql, equalTo(expectedSql));
	}
	
	@Test
	public void buildSQL_PreOracle12AndPagination_ShouldReturnSubQueryPaginatedSql() {
		final String sql = new QueryBuilder.Builder(INITIAL_SQL, agencyMapping, ORACLE_11)
				.setRemoveSpecialChars(true)
				.addPagedQuery()
				.build();
		final String expectedSql = String.format("SELECT * FROM (SELECT QRY_PAG.*, ROWNUM rnum FROM ( %s ) QRY_PAG WHERE ROWNUM <= :limit) WHERE rnum >= :offset", INITIAL_SQL);
		assertThat(sql, equalTo(expectedSql));
	}
		
	@Test
	public void buildSQL_UsingAliasColumnWithCondition_ShouldReturnQueryWithSubQuery() {
		final String sql = new QueryBuilder.Builder(INITIAL_SQL, agencyMapping, ORACLE_12)
				.setRemoveSpecialChars(true)
				.addQuery("agencyId:eq:'ITAG'")
				.build();
		final String expectedSql = String.format("SELECT QRY_ALIAS.* FROM ( %s ) QRY_ALIAS WHERE AGENCY_CODE = 'ITAG'", INITIAL_SQL);
		assertThat(sql, equalTo(expectedSql));
	}
		
	@Test
	public void buildSQL_UsingAliasColumnWithConditionAndRowCounting_ShouldReturnQueryWithSubQuery() {
		final String sql = new QueryBuilder.Builder(INITIAL_SQL, agencyMapping, ORACLE_12)
			.setRemoveSpecialChars(true)
			.addQuery("agencyId:eq:'ITAG'")
			.addRowCount()
			.build();
		final String expectedSql = String.format("SELECT COUNT(*) OVER() RECORD_COUNT, QRY_ALIAS.* FROM ( %s ) QRY_ALIAS WHERE AGENCY_CODE = 'ITAG'", INITIAL_SQL);
		assertThat(sql, equalTo(expectedSql));
	}

	@Test
	@UseDataProvider("data")
	public void buildSql_UsingQueryOperators_ShouldReturnExpectedParam(final String jsonOperator, final String sqlOperator, final String paramValue, String paramExpected) {
		final String sql = new QueryBuilder.Builder(INITIAL_SQL, agencyMapping, ORACLE_12)
				.setRemoveSpecialChars(true)
				.addQuery("agencyId:" + jsonOperator + ":" + paramValue)
				.build();
		final String expectedSql = String.format("SELECT QRY_ALIAS.* FROM ( %s ) QRY_ALIAS WHERE AGENCY_CODE %s %s", INITIAL_SQL, sqlOperator, paramExpected);
		assertThat(sql, equalTo(expectedSql));
	}



}