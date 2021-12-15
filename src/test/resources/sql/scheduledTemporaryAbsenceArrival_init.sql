INSERT INTO offenders (offender_id, id_source_code, last_name, middle_name, first_name, sex_code, create_date,
                       last_name_key, offender_id_display, root_offender_id, race_code, alias_name_type, birth_date,
                       birth_place, birth_country_code)
VALUES (2554472, 'SEQ', 'AIMONIS', 'EF''LIAICO', 'URUA', 'M', SYSDATE, 'ANDERSON', 'G6942UN', 2554472, 'W1', 'CN',
        TO_DATE('1969-12-30', 'YYYY-MM-DD'), 'WALES', 'UK');

INSERT INTO offender_bookings (offender_book_id, booking_begin_date, booking_end_date, booking_no, offender_id, agy_loc_id,
                               living_unit_id, disclosure_flag, in_out_status, active_flag, booking_status, youth_adult_code,
                               assigned_staff_id, booking_type, root_offender_id, service_fee_flag,
                               community_active_flag, comm_status, status_reason, booking_seq)
VALUES (1176156, TO_DATE('2017-02-17', 'YYYY-MM-DD'), NULL, '26972A', 2554472, 'LEI', -9, 'N', 'OUT', 'Y', 'O', 'N',
        6991, 'INST', 2554472, 'N', 'N', null, 'TAP-C3', 1);

INSERT INTO offender_external_movements (offender_book_id, movement_seq, movement_date, movement_time,
                                         movement_type, movement_reason_code, direction_code, escort_code, from_agy_loc_id,
                                         to_agy_loc_id, active_flag, event_id)
VALUES (1176156, 1, TO_DATE('2017-04-17', 'YYYY-MM-DD'), TO_DATE('2017-04-17', 'YYYY-MM-DD'), 'ADM', 'I', 'IN', 'GEOAME', 'ABDRCT', 'BXI', 'N', NULL) ,
       (1176156, 2, TO_DATE('2021-12-14', 'YYYY-MM-DD'), TO_DATE('2021-12-14', 'YYYY-MM-DD'), 'TAP', 'C3', 'OUT', 'A', 'BXI', 'ABDRCT', 'Y', 456944514);


INSERT INTO offender_ind_schedules (event_id, offender_book_id, event_date, start_time, end_time, event_class,
                                    event_type, event_sub_type, event_status, comment_text,
                                    hidden_comment_text, application_date, parent_event_id, agy_loc_id,
                                    to_agy_loc_id, to_internal_location_id, from_city, to_city, crs_sch_id,
                                    order_id, sentence_seq, outcome_reason_code, judge_name, check_box_1,
                                    check_box_2, in_charge_staff_id, credited_hours, report_in_date,
                                    piece_work, engagement_code, understanding_code, details,
                                    credited_work_hour, agreed_travel_hour, unpaid_work_supervisor,
                                    unpaid_work_behaviour, unpaid_work_action, sick_note_received_date,
                                    sick_note_expiry_date, court_event_result, unexcused_absence_flag,
                                    create_user_id, modify_user_id, create_datetime, modify_datetime,
                                    escort_code, confirm_flag, direction_code, to_city_code, from_city_code,
                                    off_prgref_id, in_time, out_time, performance_code, temp_abs_sch_id,
                                    reference_id, transport_code, application_time, to_country_code,
                                    oj_location_code, contact_person_name, to_address_owner_class,
                                    to_address_id, return_date, return_time)
VALUES (456944515, 1176156, TO_DATE('2021-12-14', 'YYYY-MM-DD'),
        TO_DATE('2021-12-14', 'YYYY-MM-DD'), null, 'EXT_MOV', 'TAP', 'C4', 'SCH', null, null, null,
        456944514, null, 'LEI', null, null, null, null, null, null, null, null, 'N', 'N', null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, 'N', 'MNAWROCKI_GEN', null,
        TO_DATE('2021-12-14', 'YYYY-MM-DD'), null, 'A', 'N', 'IN', null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
       (456944514, 1176156, TO_DATE('2021-12-14', 'YYYY-MM-DD'),
        TO_DATE('2021-12-14', 'YYYY-MM-DD'), null, 'EXT_MOV', 'TAP', 'C4', 'COMP', null, null,
        TO_DATE('2021-12-14', 'YYYY-MM-DD'), null, 'LEI', 'ABDRCT', null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, 'MNAWROCKI_GEN', 'MNAWROCKI_GEN',
        TO_DATE('2021-12-14', 'YYYY-MM-DD'),
        TO_DATE('2021-12-14', 'YYYY-MM-DD'), 'A', null, 'OUT', null, null, null,
        null, null, null, null, null, 'TAX', TO_DATE('2021-12-14', 'YYYY-MM-DD'), null, null, null, 'AGY',
        5623140, TO_DATE('2021-12-14', 'YYYY-MM-DD'),
        TO_DATE('2021-12-14', 'YYYY-MM-DD'));
