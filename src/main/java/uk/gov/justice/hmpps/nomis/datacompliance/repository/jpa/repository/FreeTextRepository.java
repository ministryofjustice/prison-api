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

            "agency_incident_ids AS (" +
            "SELECT agency_incident_id, oic_incident_id, party_seq FROM agency_incident_parties " +
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
            "INNER JOIN agency_incident_ids ids " +
            "ON aic.agency_incident_id = ids.agency_incident_id " +
            "AND aic.party_seq = ids.party_seq " +
            "WHERE (" +
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
            "INNER JOIN agency_incident_ids ids " +
            "ON ai.agency_incident_id = ids.agency_incident_id " +
            "WHERE REGEXP_LIKE(ai.incident_details, :regex, 'i') " +

            "UNION " +
            "SELECT 'AGY_INC_INVESTIGATIONS' AS table_name " +
            "FROM agy_inc_investigations aii " +
            "INNER JOIN agency_incident_ids ids " +
            "ON ids.agency_incident_id = aii.agency_incident_id " +
            "AND ids.party_seq = aii.party_seq " +
            "WHERE REGEXP_LIKE(aii.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'AGY_INC_INV_STATEMENTS' AS table_name " +
            "FROM agy_inc_inv_statements aiis " +
            "INNER JOIN agy_inc_investigations aii " +
            "ON aiis.agy_inc_investigation_id = aii.agy_inc_investigation_id " +
            "INNER JOIN agency_incident_ids ids " +
            "ON ids.agency_incident_id = aii.agency_incident_id " +
            "AND ids.party_seq = aii.party_seq " +
            "WHERE REGEXP_LIKE(aiis.statement_detail, :regex, 'i') " +

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
            "SELECT 'OFFENDER_DATA_CORRECTIONS_HTY' AS table_name " +
            "FROM offender_data_corrections_hty " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_EDUCATIONS ' AS table_name " +
            "FROM offender_educations " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_EMPLOYMENTS ' AS table_name " +
            "FROM offender_employments " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(termination_reason_text, :regex, 'i')" +
            "OR REGEXP_LIKE(comment_text, :regex, 'i')" +
            ") " +

            "UNION " +
            "SELECT 'OFFENDER_EXTERNAL_MOVEMENTS ' AS table_name " +
            "FROM offender_external_movements " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_FINE_PAYMENTS ' AS table_name " +
            "FROM offender_fine_payments " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_FIXED_TERM_RECALLS ' AS table_name " +
            "FROM offender_fixed_term_recalls " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_GANG_AFFILIATIONS ' AS table_name " +
            "FROM offender_gang_affiliations " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_GANG_INVESTS ' AS table_name " +
            "FROM offender_gang_invests " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_HEALTH_PROBLEMS' AS table_name " +
            "FROM offender_health_problems " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(description, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_IDENTIFYING_MARKS' AS table_name " +
            "FROM offender_identifying_marks " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_IEP_LEVELS' AS table_name " +
            "FROM offender_iep_levels " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_IMPRISON_STATUSES' AS table_name " +
            "FROM offender_imprison_statuses " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_IND_SCHEDULES' AS table_name " +
            "FROM offender_ind_schedules " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_KEY_DATE_ADJUSTS' AS table_name " +
            "FROM offender_key_date_adjusts " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_LANGUAGES' AS table_name " +
            "FROM offender_languages " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_LICENCE_RECALLS' AS table_name " +
            "FROM offender_licence_recalls " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_MEDICAL_TREATMENTS' AS table_name " +
            "FROM offender_medical_treatments omt " +
            "INNER JOIN offender_health_problems ohp " +
            "ON omt.offender_health_problem_id = ohp.offender_health_problem_id " +
            "WHERE ohp.offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(omt.comment_text, :regex, 'i') " +
            "OR REGEXP_LIKE(omt.description, :regex, 'i') " +
            ")" +

            "UNION " +
            "SELECT 'OFFENDER_MILITARY_RECORDS' AS table_name " +
            "FROM offender_military_records " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(description, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_MOVEMENT_APPS' AS table_name " +
            "FROM offender_movement_apps " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_NA_DETAILS' AS table_name " +
            "FROM offender_na_details " +
            "WHERE (offender_book_id IN (:bookIds) OR ns_offender_book_id IN (:bookIds))" +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_NO_PAY_PERIODS' AS table_name " +
            "FROM offender_no_pay_periods " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_OIC_SANCTIONS' AS table_name " +
            "FROM offender_oic_sanctions " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_PAY_STATUSES' AS table_name " +
            "FROM offender_pay_statuses " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_PERSON_RESTRICTS' AS table_name " +
            "FROM offender_person_restricts opr " +
            "INNER JOIN offender_contact_persons ocp " +
            "ON opr.offender_contact_person_id = ocp.offender_contact_person_id " +
            "WHERE ocp.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(opr.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_PPTY_CONTAINERS' AS table_name " +
            "FROM offender_ppty_containers " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_PROGRAM_PROFILES' AS table_name " +
            "FROM offender_program_profiles " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(referral_comment_text, :regex, 'i') " +
            "OR REGEXP_LIKE(offender_end_comment_text, :regex, 'i') " +
            ")" +

            "UNION " +
            "SELECT 'OFFENDER_REHAB_DECISIONS' AS table_name " +
            "FROM offender_rehab_decisions " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_REHAB_PROVIDERS' AS table_name " +
            "FROM offender_rehab_providers orp " +
            "INNER JOIN offender_rehab_decisions ord " +
            "ON orp.offender_rehab_decision_id = ord.offender_rehab_decision_id " +
            "WHERE ord.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(orp.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_RELEASE_DETAILS' AS table_name " +
            "FROM offender_release_details " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_RELEASE_DETAILS_HTY' AS table_name " +
            "FROM offender_release_details_hty " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_RESTRICTIONS' AS table_name " +
            "FROM offender_restrictions " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_SENTENCES' AS table_name " +
            "FROM offender_sentences " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(comment_text, :regex, 'i') " +
            "OR REGEXP_LIKE(status_update_comment, :regex, 'i') " +
            ") " +

            "UNION " +
            "SELECT 'OFFENDER_SENTENCE_ADJUSTS' AS table_name " +
            "FROM offender_sentence_adjusts " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_SENTENCE_STATUSES' AS table_name " +
            "FROM offender_sentence_statuses " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(status_update_comment, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_SENT_CALCULATIONS' AS table_name " +
            "FROM offender_sent_calculations " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_SENT_CONDITIONS' AS table_name " +
            "FROM offender_sent_conditions " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(comment_text, :regex, 'i') " +
            "OR REGEXP_LIKE(details_text, :regex, 'i')" +
            "OR REGEXP_LIKE(long_comment_text, :regex, 'i')" +
            "OR REGEXP_LIKE(drug_testing, :regex, 'i')" +
            "OR REGEXP_LIKE(prohibited_contact, :regex, 'i')" +
            "OR REGEXP_LIKE(non_associated_offenders, :regex, 'i')" +
            "OR REGEXP_LIKE(restricted_approval_person, :regex, 'i')" +
            "OR REGEXP_LIKE(other_program, :regex, 'i')" +
            "OR REGEXP_LIKE(status_update_comment, :regex, 'i')" +
            ")" +

            "UNION " +
            "SELECT 'OFFENDER_SENT_COND_STATUSES' AS table_name " +
            "FROM offender_sent_cond_statuses oscs " +
            "INNER JOIN offender_sent_conditions osc " +
            "ON oscs.offender_sent_condition_id = osc.offender_sent_condition_id " +
            "WHERE osc.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(oscs.status_update_comment, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_SUBSTANCE_DETAILS' AS table_name " +
            "FROM offender_substance_details " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(comment_text, :regex, 'i') " +
            "OR REGEXP_LIKE(use_period, :regex, 'i') " +
            ") " +

            "UNION " +
            "SELECT 'OFFENDER_SUBSTANCE_TREATMENTS' AS table_name " +
            "FROM offender_substance_treatments " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_TEST_SELECTIONS' AS table_name " +
            "FROM offender_test_selections " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(notes, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_VISITS' AS table_name " +
            "FROM offender_visits " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(comment_text, :regex, 'i') " +
            "OR REGEXP_LIKE(visitor_concern_text, :regex, 'i') " +
            ") " +

            "UNION " +
            "SELECT 'OFFENDER_VISIT_BALANCE_ADJS' AS table_name " +
            "FROM offender_visit_balance_adjs " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_VISIT_ORDERS' AS table_name " +
            "FROM offender_visit_orders " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_VSC_SENTENCES' AS table_name " +
            "FROM offender_vsc_sentences " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(status_update_comment, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_VSC_SENT_CALCULATIONS' AS table_name " +
            "FROM offender_vsc_sent_calculations " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_VISIT_VISITORS' AS table_name " +
            "FROM offender_visit_visitors ovv " +
            "INNER JOIN offender_visits ov " +
            "ON ovv.offender_visit_id = ov.offender_visit_id " +
            "WHERE ov.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(ovv.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFF_CASE_NOTE_RECIPIENTS' AS table_name " +
            "FROM off_case_note_recipients ocnr " +
            "INNER JOIN offender_case_notes ocn " +
            "ON ocnr.case_note_id = ocn.case_note_id " +
            "WHERE ocn.offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(ocnr.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OIC_HEARINGS' AS table_name " +
            "FROM oic_hearings oh " +
            "INNER JOIN agency_incident_ids ids " +
            "ON ids.oic_incident_id = oh.oic_incident_id " +
            "WHERE REGEXP_LIKE(oh.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OIC_HEARING_NOTICES' AS table_name " +
            "FROM oic_hearing_notices ohn " +
            "INNER JOIN oic_hearings oh " +
            "ON ohn.oic_hearing_id = oh.oic_hearing_id " +
            "INNER JOIN agency_incident_ids ids " +
            "ON ids.oic_incident_id = oh.oic_incident_id " +
            "WHERE REGEXP_LIKE(ohn.comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'ORDERS' AS table_name " +
            "FROM orders " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'TASK_ASSIGNMENT_HTY' AS table_name " +
            "FROM task_assignment_hty " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND (" +
            "REGEXP_LIKE(details, :regex, 'i') " +
            "OR REGEXP_LIKE(complete_comment_text, :regex, 'i') " +
            ")",

            nativeQuery = true)
    List<FreeTextMatch> findMatchUsingBookIds(Set<Long> bookIds, String regex);

    @Query(value =

            "SELECT 'OFFENDER_DAMAGE_OBLIGATIONS' AS table_name " +
            "FROM offender_damage_obligations " +
            "WHERE offender_id IN (:offenderIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i') " +

            "UNION " +
            "SELECT 'OFFENDER_FREEZE_DISBURSEMENTS ' AS table_name " +
            "FROM offender_freeze_disbursements " +
            "WHERE offender_id IN (:offenderIds) " +
            "AND REGEXP_LIKE(comment_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_IDENTIFIERS' AS table_name " +
            "FROM offender_identifiers " +
            "WHERE offender_id IN (:offenderIds) " +
            "AND REGEXP_LIKE(issued_authority_text, :regex, 'i')" +

            "UNION " +
            "SELECT 'OFFENDER_PAYMENT_PROFILES' AS table_name " +
            "FROM offender_payment_profiles " +
            "WHERE offender_id IN (:offenderIds) " +
            "AND (" +
            "REGEXP_LIKE(reference_text, :regex, 'i') " +
            "OR REGEXP_LIKE(comment_text, :regex, 'i')" +
            ")",

            nativeQuery = true)
    List<FreeTextMatch> findMatchUsingOffenderIds(Set<Long> offenderIds, String regex);
}
