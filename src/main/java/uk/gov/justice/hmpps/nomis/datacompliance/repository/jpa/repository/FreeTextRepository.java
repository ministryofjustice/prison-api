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

            "WITH incident_case_ids AS (" +
            "SELECT incident_case_id FROM incident_case_parties " +
            "WHERE offender_book_id IN (:bookIds) " +
            "), " +
            "offender_csip_ids AS (" +
            "SELECT csip_id FROM offender_csip_reports " +
            "WHERE offender_book_id IN (:bookIds) " +
            ") " +
            "SELECT 'ADDRESSES' AS table_name " +
            "FROM addresses " +
            "WHERE OWNER_ID IN (:bookIds) " +
            "AND OWNER_CLASS IN ('OFF_EMP', 'OFF_EDU') " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'AGENCY_INCIDENT_CHARGES' AS table_name " +
            "FROM agency_incident_charges aic " +
            "INNER JOIN agency_incident_parties aip " +
            "ON aic.agency_incident_id = aip.agency_incident_id " +
            "AND aic.party_seq = aip.party_seq " +
            "WHERE aip.offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(aic.guilty_evidence_text, :regex, 'i') " +
            "OR REGEXP_LIKE(aic.report_text, :regex, 'i') " +
            ") " +

            "UNION " +
            "SELECT 'AGENCY_INCIDENT_PARTIES' AS table_name " +
            "FROM agency_incident_parties " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'AGENCY_INCIDENTS' AS table_name " +
            "FROM agency_incidents ai " +
            "INNER JOIN agency_incident_parties aip " +
            "ON ai.agency_incident_id = aip.agency_incident_id " +
            "WHERE aip.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(ai.incident_details, :regex, 'i') " +

            "UNION " +
            "SELECT 'AGY_INC_INVESTIGATIONS' AS table_name " +
            "FROM agy_inc_investigations aii " +
            "INNER JOIN agency_incident_parties aip " +
            "ON aip.agency_incident_id = aii.agency_incident_id " +
            "AND aip.party_seq = aii.party_seq " +
            "WHERE aip.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(aii.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'AGY_INC_INV_STATEMENTS' AS table_name " +
            "FROM agy_inc_inv_statements aiis " +
            "INNER JOIN agy_inc_investigations aii " +
            "ON aiis.agy_inc_investigation_id = aii.agy_inc_investigation_id " +
            "INNER JOIN agency_incident_parties aip " +
            "ON aip.agency_incident_id = aii.agency_incident_id " +
            "AND aip.party_seq = aii.party_seq " +
            "WHERE aip.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(aiis.statement_detail, :regex, 'i') " +

            "UNION " +
            "SELECT 'COURT_EVENTS' AS table_name " +
            "FROM court_events " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'CURFEW_ADDRESS_OCCUPANTS' AS table_name " +
            "FROM curfew_address_occupants cao " +
            "INNER JOIN curfew_addresses ca " +
            "ON cao.curfew_address_id = ca.curfew_address_id " +
            "WHERE ca.offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(contact_text, :regex, 'i') " +
            "OR REGEXP_LIKE(comment_text, :regex, 'i') " +
            ") " +

            "UNION " +
            "SELECT 'HDC_BOARD_DECISIONS' AS table_name " +
            "FROM hdc_board_decisions hbd " +
            "INNER JOIN hdc_request_referrals hrr " +
            "ON hbd.hdc_request_referral_id = hrr.hdc_request_referral_id " +
            "WHERE hrr.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(hbd.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'HDC_GOVERNOR_DECISIONS' AS table_name " +
            "FROM hdc_governor_decisions hgd " +
            "INNER JOIN hdc_request_referrals hrr " +
            "ON hgd.hdc_request_referral_id = hrr.hdc_request_referral_id " +
            "WHERE hrr.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(hgd.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'HDC_PRISON_STAFF_COMMENTS' AS table_name " +
            "FROM hdc_prison_staff_comments " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'HDC_PROB_STAFF_COMMENTS' AS table_name " +
            "FROM hdc_prob_staff_comments hpsc " +
            "INNER JOIN hdc_request_referrals hrr " +
            "ON hpsc.hdc_request_referral_id = hrr.hdc_request_referral_id " +
            "WHERE hrr.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(hpsc.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'HDC_REQUEST_REFERRALS' AS table_name " +
            "FROM hdc_request_referrals " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(referral_information, :regex, 'i') " +

            "UNION " +
            "SELECT 'INCIDENT_CASES' AS table_name " +
            "FROM incident_cases ic " +
            "INNER JOIN incident_case_ids ici " +
            "ON ic.incident_case_id = ici.incident_case_id " +
            "WHERE REGEXP_LIKE(incident_title, :regex, 'i') " +
            "OR REGEXP_LIKE(incident_details, :regex, 'i') " +

            "UNION " +
            "SELECT 'INCIDENT_CASE_PARTIES' AS table_name " +
            "FROM incident_case_parties icp " +
            "INNER JOIN incident_case_ids ici " +
            "ON icp.incident_case_id = ici.incident_case_id " +
            "WHERE REGEXP_LIKE(icp.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'INCIDENT_CASE_REQUIREMENTS' AS table_name " +
            "FROM incident_case_requirements icr " +
            "INNER JOIN incident_case_ids ici " +
            "ON icr.incident_case_id = ici.incident_case_id " +
            "WHERE REGEXP_LIKE(icr.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'INCIDENT_CASE_RESPONSES' AS table_name " +
            "FROM incident_case_responses icr " +
            "INNER JOIN incident_case_ids ici " +
            "ON icr.incident_case_id = ici.incident_case_id " +
            "WHERE REGEXP_LIKE(icr.response_comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'INCIDENT_QUE_RESPONSE_HTY' AS table_name " +
            "FROM INCIDENT_QUE_RESPONSE_HTY iqrh " +
            "INNER JOIN incident_questionnaire_hty iqh " +
            "ON iqrh.incident_questionnaire_id = iqh.incident_questionnaire_id " +
            "INNER JOIN incident_case_ids ici " +
            "ON iqh.incident_case_id = ici.incident_case_id " +
            "WHERE REGEXP_LIKE(iqrh.response_comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'IWP_DOCUMENTS' AS table_name " +
            "FROM iwp_documents " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_ALERTS' AS table_name " +
            "FROM offender_alerts " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_ASSESSMENTS' AS table_name " +
            "FROM offender_assessments " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(assess_comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_ASSESSMENT_ITEMS' AS table_name " +
            "FROM offender_assessment_items " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_BELIEFS' AS table_name " +
            "FROM offender_beliefs " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comments, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CASES' AS table_name " +
            "FROM offender_cases " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(status_update_comment, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CASE_NOTES' AS table_name " +
            "FROM offender_case_notes " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(case_note_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CASE_STATUSES' AS table_name " +
            "FROM offender_case_statuses ocs " +
            "INNER JOIN offender_cases oc " +
            "ON ocs.case_id = oc.case_id " +
            "WHERE oc.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(ocs.status_update_comment, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CONTACT_PERSONS' AS table_name " +
            "FROM offender_contact_persons " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_COURSE_ATTENDANCES' AS table_name " +
            "FROM offender_course_attendances " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CSIP_ATTENDEES' AS table_name " +
            "FROM offender_csip_attendees oca " +
            "INNER JOIN offender_csip_reviews ocr " +
            "ON oca.review_id = ocr.review_id " +
            "INNER JOIN offender_csip_ids oci " +
            "ON ocr.csip_id = oci.csip_id " +
            "WHERE REGEXP_LIKE(oca.contribution, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CSIP_FACTORS' AS table_name " +
            "FROM offender_csip_factors ocf " +
            "INNER JOIN offender_csip_ids oci " +
            "ON ocf.csip_id = oci.csip_id " +
            "WHERE REGEXP_LIKE(ocf.comments, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CSIP_INTVW' AS table_name " +
            "FROM offender_csip_intvw oci " +
            "INNER JOIN offender_csip_ids ids " +
            "ON oci.csip_id = ids.csip_id " +
            "WHERE REGEXP_LIKE(oci.comments, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CSIP_PLANS' AS table_name " +
            "FROM offender_csip_plans ocp " +
            "INNER JOIN offender_csip_ids oci " +
            "ON ocp.csip_id = oci.csip_id " +
            "WHERE (" +
            "REGEXP_LIKE(ocp.identified_need, :regex, 'i') " +
            "OR REGEXP_LIKE(ocp.progression, :regex, 'i') " +
            "OR REGEXP_LIKE(ocp.intervention, :regex, 'i') " +
            ") " +

            "UNION " +
            "SELECT 'OFFENDER_CSIP_REPORTS' AS table_name " +
            "FROM offender_csip_reports ocr " +
            "INNER JOIN offender_csip_ids oci " +
            "ON ocr.csip_id = oci.csip_id " +
            "WHERE (" +
            "REGEXP_LIKE(ocr.rfr_comment, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.cdr_other_information, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.cdr_concern_description, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_known_reasons, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_persons_trigger, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_staff_involved, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_evidence_secured, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_conclusion, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_next_steps, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_other, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_occurrence_reason, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_usual_behaviour, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.inv_protective_factors, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.reason, :regex, 'i') " +
            "OR REGEXP_LIKE(ocr.cdr_decision_reason, :regex, 'i') " +
            ") " +

            "UNION " +
            "SELECT 'OFFENDER_CSIP_REVIEWS' AS table_name " +
            "FROM offender_csip_reviews ocr " +
            "INNER JOIN offender_csip_ids oci " +
            "ON ocr.csip_id = oci.csip_id " +
            "WHERE REGEXP_LIKE(ocr.summary, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_CURFEWS' AS table_name " +
            "FROM offender_curfews " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(comment_text, :regex, 'i')" +
            "OR REGEXP_LIKE(alternate_curfew_detail, :regex, 'i')" +
            ")" +

            "UNION " +
            "SELECT 'OFFENDER_VISIT_VISITORS' AS table_name " +
            "FROM offender_visit_visitors ovv " +
            "INNER JOIN offender_visits ov " +
            "ON ovv.offender_visit_id = ov.offender_visit_id " +
            "WHERE ov.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(ovv.comment_text, :regex, 'i')",

            nativeQuery = true)
    List<FreeTextMatch> findMatchUsingBookIds(Set<Long> bookIds, String regex);

    @Query(value =

            "SELECT 'OFFENDER_DAMAGE_OBLIGATIONS' AS table_name " +
            "FROM offender_damage_obligations " +
            "WHERE offender_id IN (:offenderIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') ",

            nativeQuery = true)
    List<FreeTextMatch> findMatchUsingOffenderIds(Set<Long> offenderIds, String regex);
}
