package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.FreeTextMatch;

import java.util.List;
import java.util.Set;

@Repository
public interface FreeTextRepository extends org.springframework.data.repository.Repository<FreeTextMatch, String> {

    // TODO GDPR-57 Extend query to cover all tables with relevant free text fields
    // TODO GDPR-137 Investigate Oracle Text, Lucene as alternative solutions.

    @Query(value =

            "SELECT 'ADDRESSES' AS table_name " +
            "FROM addresses " +
            "WHERE OWNER_ID IN (:offenderBookIds) " +
            "AND OWNER_CLASS IN ('OFF_EMP', 'OFF_EDU') " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'AGENCY_INCIDENT_CHARGES' AS table_name " +
            "FROM agency_incident_charges aic " +
            "INNER JOIN agency_incident_parties aip " +
            "ON aic.agency_incident_id = aip.agency_incident_id " +
            "AND aic.party_seq = aip.party_seq " +
            "WHERE aip.offender_book_id IN (:offenderBookIds) " +
            "AND (" +
            "REGEXP_LIKE(aic.guilty_evidence_text, :regex, 'i') " +
            "OR REGEXP_LIKE(aic.report_text, :regex, 'i') " +
            ") " +

            "UNION " +
            "SELECT 'AGENCY_INCIDENT_PARTIES' AS table_name " +
            "FROM agency_incident_parties " +
            "WHERE offender_book_id IN (:offenderBookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_ALERTS' AS table_name " +
            "FROM offender_alerts " +
            "WHERE offender_book_id IN (:offenderBookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_ASSESSMENTS' AS table_name " +
            "FROM offender_assessments " +
            "WHERE offender_book_id IN (:offenderBookIds) " +
            "AND REGEXP_LIKE(assess_comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CASE_NOTES' AS table_name " +
            "FROM offender_case_notes " +
            "WHERE offender_book_id IN (:offenderBookIds) " +
            "AND REGEXP_LIKE(case_note_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_COURSE_ATTENDANCES' AS table_name " +
            "FROM offender_course_attendances " +
            "WHERE offender_book_id IN (:offenderBookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_VISIT_VISITORS' AS table_name " +
            "FROM offender_visit_visitors ovv " +
            "INNER JOIN offender_visits ov " +
            "ON ovv.offender_visit_id = ov.offender_visit_id " +
            "WHERE ov.offender_book_id IN (:offenderBookIds) " +
            "AND REGEXP_LIKE(ovv.comment_text, :regex, 'i')",

            nativeQuery = true)
    List<FreeTextMatch> findMatch(Set<Long> offenderBookIds, String regex);
}
