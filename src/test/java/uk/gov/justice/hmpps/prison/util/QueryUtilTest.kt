package uk.gov.justice.hmpps.prison.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class QueryUtilTest {
  @Test
  fun testExtractCriteriaFromSql() {
    val criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1 FROM DUAL")
    assertThat(criteriaResult).isEqualTo("FROM DUAL")
  }

  @Test
  fun testExtractCriteriaFromSqlWithSubQuery() {
    val criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM TEMP) FROM DUAL WHERE 1 = 1")
    assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE 1 = 1")
  }

  @Test
  fun testExtractCriteriaFromSqlWithlowercase() {
    val criteriaResult = QueryUtil.getCriteriaFromQuery("SELECT 1, (SELECT COUNT(*) FROM TEMP) from   DUAL WHERE 1 = 1")
    assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE 1 = 1")
  }

  @Test
  fun testExtractCriteriaFromSqlWithlowercaseNewLines() {
    val criteriaResult = QueryUtil.getCriteriaFromQuery(
      """
  SELECT 1, (SELECT COUNT(*) FROM
  TEMP) from
  DUAL WHERE 1 = 1
      """.trimIndent(),
    )
    assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE 1 = 1")
  }

  @Test
  fun testExtractCriteriaFromSqlWithlowercaseNewLinesAndPlaceholders() {
    val criteriaResult = QueryUtil.getCriteriaFromQuery(
      """
  SELECT 1, (SELECT COUNT(*) FROM
  TEMP) From
  DUAL WHERE caseLoadId = :myId
      """.trimIndent(),
    )
    assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE caseLoadId = :myId")
  }

  @Test
  fun testExtractCriteriaFromSqlEmbeddedFrom() {
    val criteriaResult = QueryUtil.getCriteriaFromQuery(
      """
  SELECT 1, (SELECT COUNT(*) FROM
  TEMP) From
  DUAL WHERE exists (select 1 from DUAL)
      """.trimIndent(),
    )
    assertThat(criteriaResult).isEqualTo("FROM DUAL WHERE EXISTS (SELECT 1 FROM DUAL)")
  }

  @Test
  fun testExtractCriteriaFromInnerJoins() {
    val criteriaResult =
      QueryUtil.getCriteriaFromQuery("SELECT t1.ID FROM TMP1 t1 INNER JOIN TMP2 t2 ON t1.ID = t2.ID LEFT JOIN TMP3 t3 on t3.ID = t2.ID")
    assertThat(criteriaResult)
      .isEqualTo("FROM TMP1 t1 INNER JOIN TMP2 t2 ON t1.ID = t2.ID LEFT JOIN TMP3 t3 ON t3.ID = t2.ID")
  }

  @Test
  fun testConvertToDateNullDateValue() {
    assertThatThrownBy { QueryUtil.convertToDate(null) }
      .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun testConvertToDate() {
    val convertedDate = QueryUtil.convertToDate("2017-04-15")
    assertThat(convertedDate).isEqualTo("TO_DATE('2017-04-15', 'YYYY-MM-DD')")
  }
}
