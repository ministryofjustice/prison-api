INSERT INTO offenders (offender_id, id_source_code, last_name, middle_name, first_name, sex_code, create_date,
                       last_name_key, offender_id_display, root_offender_id, race_code, alias_name_type, birth_date,
                       birth_place, birth_country_code)
VALUES (999991, 'SEQ', 'AIMONIS', 'EF''LIAICO', 'URUA', 'M', SYSDATE, 'ANDERSON', 'A9991AA', 999991, 'W1', 'CN',
        TO_DATE('1969-12-30', 'YYYY-MM-DD'), 'WALES', 'UK');

INSERT INTO offender_bookings (offender_book_id, booking_begin_date, booking_end_date, booking_no, offender_id,
                               agy_loc_id,
                               living_unit_id, disclosure_flag, in_out_status, active_flag, booking_status,
                               youth_adult_code,
                               assigned_staff_id, booking_type, root_offender_id, service_fee_flag,
                               community_active_flag, comm_status, status_reason, booking_seq)
VALUES (999991, TO_DATE('2017-02-17', 'YYYY-MM-DD'), NULL, '26972A', 999991, 'LEI', -9, 'N', 'IN', 'Y', 'O', 'N', 6991,
        'INST', 999991, 'N', 'N', NULL, 'TAP-C3', 1);

INSERT INTO OFFENDER_SENTENCES (OFFENDER_BOOK_ID, SENTENCE_SEQ, ORDER_ID, SENTENCE_CALC_TYPE, SENTENCE_STATUS,
                                START_DATE, END_DATE, CASE_ID, ARD_CALCULATED_DATE, SED_CALCULATED_DATE,
                                SENTENCE_CATEGORY, FINE_AMOUNT, SENTENCE_LEVEL, DISCHARGE_DATE, STATUS_UPDATE_REASON,
                                LINE_SEQ, COMMENT_TEXT, STATUS_UPDATE_COMMENT)
VALUES (999991, 1, -1, 'ADIMP_ORA', 'A', TO_DATE('2017-07-05', 'YYYY-MM-DD'), TO_DATE('2017-11-04', 'YYYY-MM-DD'), -1,
        TO_DATE('2017-09-04', 'YYYY-MM-DD'), TO_DATE('2017-11-04', 'YYYY-MM-DD'), '2003', 120.00, 'IND',
        TO_DATE('2017-11-04', 'YYYY-MM-DD'), 'A', 1, 'Some Comment Text', 'Some Status Update Comment Text');
INSERT INTO OFFENDER_SENTENCES (OFFENDER_BOOK_ID, SENTENCE_SEQ, ORDER_ID, SENTENCE_CALC_TYPE, SENTENCE_STATUS,
                                START_DATE, END_DATE, CASE_ID, ARD_CALCULATED_DATE, SED_CALCULATED_DATE,
                                SENTENCE_CATEGORY, FINE_AMOUNT, SENTENCE_LEVEL, DISCHARGE_DATE, STATUS_UPDATE_REASON,
                                LINE_SEQ, COMMENT_TEXT, STATUS_UPDATE_COMMENT)
VALUES (999991, 2, -2, 'ADIMP_ORA', 'A', TO_DATE('2017-07-05', 'YYYY-MM-DD'), TO_DATE('2017-11-04', 'YYYY-MM-DD'), -1,
        TO_DATE('2017-09-04', 'YYYY-MM-DD'), TO_DATE('2017-11-04', 'YYYY-MM-DD'), '2003', 120.00, 'IND',
        TO_DATE('2017-11-04', 'YYYY-MM-DD'), 'A', 1, 'Some Comment Text', 'Some Status Update Comment Text');

INSERT INTO offenders (offender_id, id_source_code, last_name, middle_name, first_name, sex_code, create_date,
                       last_name_key, offender_id_display, root_offender_id, race_code, alias_name_type, birth_date,
                       birth_place, birth_country_code)
VALUES (999992, 'SEQ', 'AIMONIS', 'EF''LIAICO', 'URUA', 'M', SYSDATE, 'ANDERSON', 'A9992AA', 999992, 'W1', 'CN',
        TO_DATE('1969-12-30', 'YYYY-MM-DD'), 'WALES', 'UK');

INSERT INTO offender_bookings (offender_book_id, booking_begin_date, booking_end_date, booking_no, offender_id,
                               agy_loc_id,
                               living_unit_id, disclosure_flag, in_out_status, active_flag, booking_status,
                               youth_adult_code,
                               assigned_staff_id, booking_type, root_offender_id, service_fee_flag,
                               community_active_flag, comm_status, status_reason, booking_seq)
VALUES (999992, TO_DATE('2017-02-17', 'YYYY-MM-DD'), NULL, '26972A', 999992, 'LEI', -9, 'N', 'IN', 'Y', 'O', 'N',
        6991, 'INST', 999992, 'N', 'N', NULL, 'TAP-C3', 1);

INSERT INTO OFFENDER_SENTENCES (OFFENDER_BOOK_ID, SENTENCE_SEQ, ORDER_ID, SENTENCE_CALC_TYPE, SENTENCE_STATUS,
                                START_DATE, END_DATE, CASE_ID, ARD_CALCULATED_DATE, SED_CALCULATED_DATE,
                                SENTENCE_CATEGORY, FINE_AMOUNT, SENTENCE_LEVEL, DISCHARGE_DATE, STATUS_UPDATE_REASON,
                                LINE_SEQ, COMMENT_TEXT, STATUS_UPDATE_COMMENT)
VALUES (999992, 1, -1, 'ADIMP_ORA', 'A', TO_DATE('2017-07-05', 'YYYY-MM-DD'), TO_DATE('2017-11-04', 'YYYY-MM-DD'), -1,
        TO_DATE('2017-09-04', 'YYYY-MM-DD'), TO_DATE('2017-11-04', 'YYYY-MM-DD'), '2003', 120.00, 'IND',
        TO_DATE('2017-11-04', 'YYYY-MM-DD'), 'A', 1, 'Some Comment Text', 'Some Status Update Comment Text');
