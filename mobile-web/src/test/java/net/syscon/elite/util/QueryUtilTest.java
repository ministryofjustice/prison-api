package net.syscon.elite.util;

import net.syscon.util.QueryUtil;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class QueryUtilTest {

    @Test
    public void testExtractCriteriaFromSql() {
        final String criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1 FROM DUAL");
        assertThat(criteriaResult).isEqualTo("FROM DUAL");
    }

    @Test
    public void testExtractCriteriaFromSqlWithSubQuery() {
        final String criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM TEMP) FROM DUAL WHERE 1 = 1");
        assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE 1 = 1");
    }

    @Test
    public void testExtractCriteriaFromSqlWithlowercase() {
        final String criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM TEMP) from   DUAL WHERE 1 = 1");
        assertThat(criteriaResult).isEqualTo("from DUAL WHERE 1 = 1");
    }

    @Test
    public void testExtractCriteriaFromSqlWithlowercaseNewLines() {
        final String criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM\n" +
                "TEMP) from\n" +
                "DUAL WHERE 1 = 1");
        assertThat(criteriaResult).isEqualTo("from DUAL WHERE 1 = 1");
    }

    @Test
    public void testExtractCriteriaFromSqlWithlowercaseNewLinesAndPlaceholders() {
        final String criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM\n" +
                "TEMP) From\n" +
                "DUAL WHERE caseLoadId = :myId");
        assertThat(criteriaResult).isEqualTo("From DUAL WHERE caseLoadId = :myId");
    }
}
