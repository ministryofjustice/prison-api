INSERT INTO OFFENDERS (OFFENDER_ID, ID_SOURCE_CODE, LAST_NAME, MIDDLE_NAME, FIRST_NAME, SEX_CODE, CREATE_DATE, LAST_NAME_KEY, OFFENDER_ID_DISPLAY, ROOT_OFFENDER_ID, RACE_CODE, ALIAS_NAME_TYPE, BIRTH_DATE, BIRTH_PLACE, BIRTH_COUNTRY_CODE)
VALUES (2554472, 'SEQ', 'AIMONIS', 'EF''LIAICO', 'URUA', 'M', sysdate, 'ANDERSON', 'G6942UN', 2554472, 'W1', 'CN', TO_DATE('1969-12-30', 'YYYY-MM-DD'), 'WALES', 'UK');

INSERT INTO AGENCY_INTERNAL_LOCATIONS (INTERNAL_LOCATION_ID, INTERNAL_LOCATION_CODE, AGY_LOC_ID,
                                                 INTERNAL_LOCATION_TYPE, DESCRIPTION, SECURITY_LEVEL_CODE, CAPACITY,
                                                 CREATE_USER_ID, PARENT_INTERNAL_LOCATION_ID, ACTIVE_FLAG, LIST_SEQ,
                                                 CREATE_DATETIME, MODIFY_DATETIME, MODIFY_USER_ID, CNA_NO,
                                                 CERTIFIED_FLAG, DEACTIVATE_DATE, REACTIVATE_DATE,
                                                 DEACTIVATE_REASON_CODE, COMMENT_TEXT, USER_DESC, ACA_CAP_RATING,
                                                 UNIT_TYPE, OPERATION_CAPACITY, NO_OF_OCCUPANT, TRACKING_FLAG)
VALUES (106221, 'F', 'BXI', 'WING', 'BXI-F', null, 116, 'OMS_OWNER', null, 'Y', 10,
        TO_DATE('2009-12-10', 'YYYY-MM-DD'),
        TO_DATE('2021-11-19', 'YYYY-MM-DD'), 'API_PROXY_USER', 78, 'Y', null,
        null, null, null, null, null, 'NA', 102, 93, 'N'),
       (106249, '2', 'BXI', 'LAND', 'BXI-F-2', null, 40, 'OMS_OWNER', 106221, 'Y', 2,
        TO_DATE('2009-12-10', 'YYYY-MM-DD'),
        TO_DATE('2017-05-09', 'YYYY-MM-DD'), 'OMS_OWNER', 26, 'Y', null, null,
        null, null, null, null, 'NA', null, 34, 'N'),
       (106258, '009', 'BXI', 'CELL', 'BXI-F-2-009', null, 2, 'OMS_OWNER', 106249, 'Y', 9,
        TO_DATE('2009-12-10', 'YYYY-MM-DD'),
        TO_DATE('2017-05-09', 'YYYY-MM-DD'), 'OMS_OWNER', 1, 'Y', null, null,
        null, null, null, null, 'NA', null, 2, 'N');

INSERT INTO OFFENDER_BOOKINGS (OFFENDER_BOOK_ID, BOOKING_BEGIN_DATE, BOOKING_END_DATE, BOOKING_NO,
                               OFFENDER_ID, AGY_LOC_ID, LIVING_UNIT_ID, DISCLOSURE_FLAG, IN_OUT_STATUS,
                               ACTIVE_FLAG, BOOKING_STATUS, YOUTH_ADULT_CODE, FINGER_PRINTED_STAFF_ID,
                               SEARCH_STAFF_ID, PHOTO_TAKING_STAFF_ID, ASSIGNED_STAFF_ID, CREATE_AGY_LOC_ID,
                               BOOKING_TYPE, BOOKING_CREATED_DATE, ROOT_OFFENDER_ID, AGENCY_IML_ID,
                               SERVICE_FEE_FLAG, EARNED_CREDIT_LEVEL, EKSTRAND_CREDIT_LEVEL,
                               INTAKE_AGY_LOC_ID, ACTIVITY_DATE, INTAKE_CASELOAD_ID, INTAKE_USER_ID,
                               CASE_OFFICER_ID, CASE_DATE, CASE_TIME, COMMUNITY_ACTIVE_FLAG,
                               CREATE_INTAKE_AGY_LOC_ID, COMM_STAFF_ID, COMM_STATUS, COMMUNITY_AGY_LOC_ID,
                               NO_COMM_AGY_LOC_ID, COMM_STAFF_ROLE, AGY_LOC_ID_LIST, STATUS_REASON,
                               TOTAL_UNEXCUSED_ABSENCES, REQUEST_NAME, CREATE_DATETIME, CREATE_USER_ID,
                               MODIFY_DATETIME, MODIFY_USER_ID, RECORD_USER_ID, INTAKE_AGY_LOC_ASSIGN_DATE,
                               BOOKING_SEQ, ADMISSION_REASON)
VALUES (1176156, TO_DATE('2017-02-17', 'YYYY-MM-DD'), null, '26972A', 2554472, 'BXI', 106258, 'N',
        'OUT', 'Y', 'O', 'N', null, null, null, 46991, 'BXI', 'INST', null, 2554472, null, 'N', null, null, null, null,
        null, null, null, null, null, 'N', null, null, 'DET', null, null, null, null, 'CRT-19', null, null,
        TO_DATE('2017-02-17', 'YYYY-MM-DD'), 'QQM25S',
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'MNAWROCKI_GEN', null, null, 1, null);

INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME,
                                         INTERNAL_SCHEDULE_TYPE, INTERNAL_SCHEDULE_REASON_CODE, MOVEMENT_TYPE,
                                         MOVEMENT_REASON_CODE, DIRECTION_CODE, ARREST_AGENCY_LOC_ID,
                                         TO_PROV_STAT_CODE, ESCORT_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID,
                                         ACTIVE_FLAG, ESCORT_TEXT, COMMENT_TEXT, REPORTING_DATE, TO_CITY,
                                         FROM_CITY, REPORTING_TIME, CREATE_DATETIME, CREATE_USER_ID,
                                         MODIFY_DATETIME, MODIFY_USER_ID)
VALUES (1176156, 1, TO_DATE('2017-04-01', 'YYYY-MM-DD'),
        TO_DATE('2017-04-01', 'YYYY-MM-DD'), null, null, 'ADM', 'I', 'IN', 'POL', null, 'GEOAME',
        'ABDRCT', 'BXI', 'N', null, null, null, null, null, null,
        TO_DATE('2017-02-17', 'YYYY-MM-DD'), 'QQM25S',
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'MNAWROCKI_GEN'),
       (1176156, 2, TO_DATE('2021-11-30', 'YYYY-MM-DD'),
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), null, null, 'CRT', '19', 'OUT', null, null, null,
        'BXI', 'ABDRCT', 'Y', null, null, null, null, null, null,
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'MNAWROCKI_GEN', null, null);
