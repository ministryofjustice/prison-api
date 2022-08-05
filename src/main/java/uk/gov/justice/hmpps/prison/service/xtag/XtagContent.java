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
}
