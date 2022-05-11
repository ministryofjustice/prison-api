INSERT INTO offenders (offender_id, id_source_code, last_name, middle_name, first_name, sex_code, create_date,
                       last_name_key, offender_id_display, root_offender_id, race_code, alias_name_type, birth_date,
                       birth_place, birth_country_code)
VALUES (2554473, 'SEQ', 'AIMONIS', 'EF''LIAICO', 'URUA', 'M', SYSDATE, 'ANDERSON', 'G6942UN', 2554473, 'W1', 'CN',
        TO_DATE('1969-12-30', 'YYYY-MM-DD'), 'WALES', 'UK');

INSERT INTO offender_bookings (offender_book_id, booking_begin_date, booking_end_date, booking_no, offender_id, agy_loc_id,
                               living_unit_id, disclosure_flag, in_out_status, active_flag, booking_status, youth_adult_code,
                               assigned_staff_id, booking_type, root_offender_id, service_fee_flag,
                               community_active_flag, comm_status, status_reason, booking_seq)
VALUES (1176157, TO_DATE('2017-02-17', 'YYYY-MM-DD'), NULL, '26972A', 2554473, 'LEI', -9, 'N', 'OUT', 'Y', 'O', 'N',
        6991, 'INST', 2554473, 'N', 'N', 'DET', 'REL-HP', 1);

INSERT INTO offender_external_movements (offender_book_id, movement_seq, movement_date, movement_time,
                                         movement_type, movement_reason_code, direction_code, escort_code, from_agy_loc_id,
                                         to_agy_loc_id, active_flag, event_id)
VALUES (1176157, 1, TO_DATE('2017-04-01', 'YYYY-MM-DD'), TO_DATE('2017-04-01', 'YYYY-MM-DD'), 'ADM', 'I', 'IN', 'GEOAME', 'ABDRCT', 'BXI', 'N', NULL) ,
(1176157, 2, TO_DATE('2021-11-30', 'YYYY-MM-DD'), TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'REL', 'HP', 'OUT', 'GEOAME', 'MDI', 'BXI', 'Y', 455654697);

