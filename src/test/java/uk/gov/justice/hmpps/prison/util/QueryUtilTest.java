package uk.gov.justice.hmpps.prison.util;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class QueryUtilTest {

    @Test
    public void testExtractCriteriaFromSql() {
        final var criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1 FROM DUAL");
        assertThat(criteriaResult).isEqualTo("FROM DUAL");
    }

    @Test
    public void testExtractCriteriaFromSqlWithSubQuery() {
        final var criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM TEMP) FROM DUAL WHERE 1 = 1");
        assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE 1 = 1");
    }

    @Test
    public void testExtractCriteriaFromSqlWithlowercase() {
        final var criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM TEMP) from   DUAL WHERE 1 = 1");
        assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE 1 = 1");
    }

    @Test
    public void testExtractCriteriaFromSqlWithlowercaseNewLines() {
        final var criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM\n" +
                "TEMP) from\n" +
                "DUAL WHERE 1 = 1");
        assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE 1 = 1");
    }

    @Test
    public void testExtractCriteriaFromSqlWithlowercaseNewLinesAndPlaceholders() {
        final var criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM\n" +
                "TEMP) From\n" +
                "DUAL WHERE caseLoadId = :myId");
        assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE caseLoadId = :myId");
    }

    @Test
    public void testExtractCriteriaFromSqlEmbeddedFrom() {
        final var criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM\n" +
                "TEMP) From\n" +
                "DUAL WHERE exists (select 1 from DUAL)");
        assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE EXISTS (SELECT 1 FROM DUAL)");
    }

    @Test
    public void testExtractCriteriaFromInnerJoins() {
        final var criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT t1.ID FROM TMP1 t1 INNER JOIN TMP2 t2 ON t1.ID = t2.ID LEFT JOIN TMP3 t3 on t3.ID = t2.ID");
        assertThat(criteriaResult).isEqualTo("FROM TMP1 t1 INNER JOIN TMP2 t2 ON t1.ID = t2.ID LEFT JOIN TMP3 t3 ON t3.ID = t2.ID");
    }

    @Test
    public void testConvertToDateNullDateValue() {
        assertThatThrownBy(() -> QueryUtil.convertToDate(null))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConvertToDate() {
        final var convertedDate = QueryUtil.convertToDate("2017-04-15");

        assertThat(convertedDate).isEqualTo("TO_DATE('2017-04-15', 'YYYY-MM-DD')");
    }
}
