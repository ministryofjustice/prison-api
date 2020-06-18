package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.FreeTextMatch;

import java.util.List;

@Repository
public interface FreeTextRepository extends org.springframework.data.repository.Repository<FreeTextMatch, String> {

    // TODO GDPR-57 Extend query to cover all tables with relevant free text fields
    // TODO GDPR-137 Investigate Oracle Text, Lucene as alternative solutions.

    @Query(value =
            "SELECT 'OFFENDER_ASSESSMENTS' AS table_name " +
            "FROM offender_assessments " +
            "WHERE offender_book_id = :offenderBookId " +
            "AND REGEXP_LIKE(assess_comment_text, :regex, 'i') " +
            "UNION " +
            "SELECT 'OFFENDER_ALERTS' AS table_name " +
            "FROM offender_alerts oa " +
            "WHERE offender_book_id = :offenderBookId " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')",
            nativeQuery = true)
    List<FreeTextMatch> findMatch(long offenderBookId, String regex);
}
