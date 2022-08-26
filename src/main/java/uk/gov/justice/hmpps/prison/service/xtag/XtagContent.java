package uk.gov.justice.hmpps.prison.service.xtag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XtagContent {
    private String xtag;

    private String p_nomis_timestamp;

    private String p_offender_id;
    private String p_root_offender_id;
    private String p_offender_book_id;
    private String p_offender_id_display;

    private String p_address_usage;
    private String p_address_end_date;
    private String p_address_deleted;

    private String p_owner_class;

    private String p_primary_addr_flag;
    private String p_mail_addr_flag;
    private String p_next_of_kin_flag;
    private String p_delete_flag;

    private String p_old_prison_num;
    private String p_old_prision_num;
    private String p_old_prison_number;
    private String p_new_prison_num;

    private String p_identifier_value;

    private String p_alert_seq;
    private String p_assessment_seq;
    private String p_imprison_status_seq;
    private String p_sentence_seq;
    private String p_result_seq;
    private String p_charge_seq;
    private String p_sanction_seq;
    private String p_movement_seq;

    private String p_owner_id;
    private String p_person_id;
    private String p_alias_offender_id;
    private String p_address_id;
    private String p_old_offender_id;
    private String p_offender_sent_calculation_id;
    private String p_oic_hearing_id;
    private String p_agency_incident_id;
    private String p_oic_offence_id;
    private String p_offender_risk_predictor_id;
    private String p_offender_sent_condition_id;
    private String p_hdc_status_tracking_id;
    private String p_from_agy_loc_id;
    private String p_to_agy_loc_id;

    private String p_plea_finding_code;
    private String p_finding_code;
    private String p_condition_code;
    private String p_reason_code;
    private String p_status_code;
    private String p_alert_code;
    private String p_movement_reason_code;
    private String p_direction_code;
    private String p_escort_code;

    private String p_event_date;
    private String p_event_time;
    private String p_old_alert_date;
    private String p_old_alert_time;
    private String p_alert_date;
    private String p_alert_time;
    private String p_expiry_date;
    private String p_expiry_time;
    private String p_movement_date;
    private String p_movement_time;

    private String p_incident_case_id;
    private String p_party_seq;
    private String p_requirement_seq;
    private String p_question_seq;
    private String p_response_seq;
    private String p_table_name;

    private String p_alert_type;
    private String p_movement_type;
    private String p_identifier_type;

    private String p_bed_assign_seq;
    private String p_living_unit_id;

    private String p_new_record;
    private String p_record_deleted;
    private String p_timestamp;

    private String p_event_id;
    private String p_start_time;
    private String p_end_time;
    private String p_event_class;
    private String p_event_type;
    private String p_event_sub_type;
    private String p_event_status;
    private String p_agy_loc_id;

    private String p_iep_level_seq;
    private String p_iep_level;
    private String p_audit_module_name;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (p_nomis_timestamp != null) sb.append("p_nomis_timestamp=").append(p_nomis_timestamp);
        if (xtag != null) sb.append(", xtag=").append(xtag);
        if (p_offender_id != null) sb.append(", p_offender_id=").append(p_offender_id);
        if (p_root_offender_id != null) sb.append(", p_root_offender_id=").append(p_root_offender_id);
        if (p_offender_book_id != null) sb.append(", p_offender_book_id=").append(p_offender_book_id);
        if (p_offender_id_display != null) sb.append(", p_offender_id_display=").append(p_offender_id_display);
        if (p_address_usage != null) sb.append(", p_address_usage=").append(p_address_usage);
        if (p_address_end_date != null) sb.append(", p_address_end_date=").append(p_address_end_date);
        if (p_address_deleted != null) sb.append(", p_address_deleted=").append(p_address_deleted);
        if (p_owner_class != null) sb.append(", p_owner_class=").append(p_owner_class);
        if (p_primary_addr_flag != null) sb.append(", p_primary_addr_flag=").append(p_primary_addr_flag);
        if (p_mail_addr_flag != null) sb.append(", p_mail_addr_flag=").append(p_mail_addr_flag);
        if (p_next_of_kin_flag != null) sb.append(", p_next_of_kin_flag=").append(p_next_of_kin_flag);
        if (p_delete_flag != null) sb.append(", p_delete_flag=").append(p_delete_flag);
        if (p_old_prison_num != null) sb.append(", p_old_prison_num=").append(p_old_prison_num);
        if (p_old_prision_num != null) sb.append(", p_old_prision_num=").append(p_old_prision_num);
        if (p_old_prison_number != null) sb.append(", p_old_prison_number=").append(p_old_prison_number);
        if (p_new_prison_num != null) sb.append(", p_new_prison_num=").append(p_new_prison_num);
        if (p_identifier_value != null) sb.append(", p_identifier_value=").append(p_identifier_value);
        if (p_alert_seq != null) sb.append(", p_alert_seq=").append(p_alert_seq);
        if (p_assessment_seq != null) sb.append(", p_assessment_seq=").append(p_assessment_seq);
        if (p_imprison_status_seq != null) sb.append(", p_imprison_status_seq=").append(p_imprison_status_seq);
        if (p_sentence_seq != null) sb.append(", p_sentence_seq=").append(p_sentence_seq);
        if (p_result_seq != null) sb.append(", p_result_seq=").append(p_result_seq);
        if (p_charge_seq != null) sb.append(", p_charge_seq=").append(p_charge_seq);
        if (p_sanction_seq != null) sb.append(", p_sanction_seq=").append(p_sanction_seq);
        if (p_movement_seq != null) sb.append(", p_movement_seq=").append(p_movement_seq);
        if (p_owner_id != null) sb.append(", p_owner_id=").append(p_owner_id);
        if (p_person_id != null) sb.append(", p_person_id=").append(p_person_id);
        if (p_alias_offender_id != null) sb.append(", p_alias_offender_id=").append(p_alias_offender_id);
        if (p_address_id != null) sb.append(", p_address_id=").append(p_address_id);
        if (p_old_offender_id != null) sb.append(", p_old_offender_id=").append(p_old_offender_id);
        if (p_offender_sent_calculation_id != null) sb.append(", p_offender_sent_calculation_id=").append(p_offender_sent_calculation_id);
        if (p_oic_hearing_id != null) sb.append(", p_oic_hearing_id=").append(p_oic_hearing_id);
        if (p_agency_incident_id != null) sb.append(", p_agency_incident_id=").append(p_agency_incident_id);
        if (p_oic_offence_id != null) sb.append(", p_oic_offence_id=").append(p_oic_offence_id);
        if (p_offender_risk_predictor_id != null) sb.append(", p_offender_risk_predictor_id=").append(p_offender_risk_predictor_id);
        if (p_offender_sent_condition_id != null) sb.append(", p_offender_sent_condition_id=").append(p_offender_sent_condition_id);
        if (p_hdc_status_tracking_id != null) sb.append(", p_hdc_status_tracking_id=").append(p_hdc_status_tracking_id);
        if (p_from_agy_loc_id != null) sb.append(", p_from_agy_loc_id=").append(p_from_agy_loc_id);
        if (p_to_agy_loc_id != null) sb.append(", p_to_agy_loc_id=").append(p_to_agy_loc_id);
        if (p_plea_finding_code != null) sb.append(", p_plea_finding_code=").append(p_plea_finding_code);
        if (p_finding_code != null) sb.append(", p_finding_code=").append(p_finding_code);
        if (p_condition_code != null) sb.append(", p_condition_code=").append(p_condition_code);
        if (p_reason_code != null) sb.append(", p_reason_code=").append(p_reason_code);
        if (p_status_code != null) sb.append(", p_status_code=").append(p_status_code);
        if (p_alert_code != null) sb.append(", p_alert_code=").append(p_alert_code);
        if (p_movement_reason_code != null) sb.append(", p_movement_reason_code=").append(p_movement_reason_code);
        if (p_direction_code != null) sb.append(", p_direction_code=").append(p_direction_code);
        if (p_escort_code != null) sb.append(", p_escort_code=").append(p_escort_code);
        if (p_event_date != null) sb.append(", p_event_date=").append(p_event_date);
        if (p_event_time != null) sb.append(", p_event_time=").append(p_event_time);
        if (p_old_alert_date != null) sb.append(", p_old_alert_date=").append(p_old_alert_date);
        if (p_old_alert_time != null) sb.append(", p_old_alert_time=").append(p_old_alert_time);
        if (p_alert_date != null) sb.append(", p_alert_date=").append(p_alert_date);
        if (p_alert_time != null) sb.append(", p_alert_time=").append(p_alert_time);
        if (p_expiry_date != null) sb.append(", p_expiry_date=").append(p_expiry_date);
        if (p_expiry_time != null) sb.append(", p_expiry_time=").append(p_expiry_time);
        if (p_movement_date != null) sb.append(", p_movement_date=").append(p_movement_date);
        if (p_movement_time != null) sb.append(", p_movement_time=").append(p_movement_time);
        if (p_incident_case_id != null) sb.append(", p_incident_case_id=").append(p_incident_case_id);
        if (p_party_seq != null) sb.append(", p_party_seq=").append(p_party_seq);
        if (p_requirement_seq != null) sb.append(", p_requirement_seq=").append(p_requirement_seq);
        if (p_question_seq != null) sb.append(", p_question_seq=").append(p_question_seq);
        if (p_response_seq != null) sb.append(", p_response_seq=").append(p_response_seq);
        if (p_table_name != null) sb.append(", p_table_name=").append(p_table_name);
        if (p_alert_type != null) sb.append(", p_alert_type=").append(p_alert_type);
        if (p_movement_type != null) sb.append(", p_movement_type=").append(p_movement_type);
        if (p_identifier_type != null) sb.append(", p_identifier_type=").append(p_identifier_type);
        if (p_bed_assign_seq != null) sb.append(", p_bed_assign_seq=").append(p_bed_assign_seq);
        if (p_living_unit_id != null) sb.append(", p_living_unit_id=").append(p_living_unit_id);
        if (p_new_record != null) sb.append(", p_new_record=").append(p_new_record);
        if (p_record_deleted != null) sb.append(", p_record_deleted=").append(p_record_deleted);
        if (p_timestamp != null) sb.append(", p_timestamp=").append(p_timestamp);
        if (p_event_id != null) sb.append(", p_event_id=").append(p_event_id);
        if (p_start_time != null) sb.append(", p_start_time=").append(p_start_time);
        if (p_end_time != null) sb.append(", p_end_time=").append(p_end_time);
        if (p_event_class != null) sb.append(", p_event_class=").append(p_event_class);
        if (p_event_type != null) sb.append(", p_event_type=").append(p_event_type);
        if (p_event_sub_type != null) sb.append(", p_event_sub_type=").append(p_event_sub_type);
        if (p_event_status != null) sb.append(", p_event_status=").append(p_event_status);
        if (p_agy_loc_id != null) sb.append(", p_agy_loc_id=").append(p_agy_loc_id);
        if (p_iep_level_seq != null) sb.append(", p_iep_level_seq=").append(p_iep_level_seq);
        if (p_iep_level != null) sb.append(", p_iep_level=").append(p_iep_level);
        if (p_audit_module_name != null) sb.append(", p_audit_module_name=").append(p_audit_module_name);
        return "{" + chop(sb.toString()) + "}";
    }

    private String chop(String s) {
        if (s.startsWith(", ")) return s.substring(2);
        return s;
    }
}
