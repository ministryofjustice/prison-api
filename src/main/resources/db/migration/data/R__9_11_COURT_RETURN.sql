INSERT INTO OFFENDERS (OFFENDER_ID, OFFENDER_NAME_SEQ, ID_SOURCE_CODE, LAST_NAME, NAME_TYPE, FIRST_NAME,
                                 MIDDLE_NAME, BIRTH_DATE, SEX_CODE, SUFFIX, LAST_NAME_SOUNDEX, BIRTH_PLACE,
                                 BIRTH_COUNTRY_CODE, CREATE_DATE, LAST_NAME_KEY, ALIAS_OFFENDER_ID, FIRST_NAME_KEY,
                                 MIDDLE_NAME_KEY, OFFENDER_ID_DISPLAY, ROOT_OFFENDER_ID, CASELOAD_TYPE, MODIFY_USER_ID,
                                 MODIFY_DATETIME, ALIAS_NAME_TYPE, PARENT_OFFENDER_ID, UNIQUE_OBLIGATION_FLAG,
                                 SUSPENDED_FLAG, SUSPENDED_DATE, RACE_CODE, REMARK_CODE, ADD_INFO_CODE, BIRTH_COUNTY,
                                 BIRTH_STATE, MIDDLE_NAME_2, TITLE, AGE, CREATE_USER_ID, LAST_NAME_ALPHA_KEY,
                                 CREATE_DATETIME, NAME_SEQUENCE, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                                 AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                                 AUDIT_ADDITIONAL_INFO)
VALUES (2554472, null, 'SEQ', 'AIMONIS', null, 'EF''LIAICO', 'URUA', TO_DATE('1987-08-07', 'YYYY-MM-DD'), 'M',
        null, 'A552', 'SHEFFIELD', 'ENG', TO_DATE('2017-02-17', 'YYYY-MM-DD'), 'AIMONIS', null, 'EFLIAICO',
        'URUA', 'G6942UN', 2554472, 'INST', 'OMS_OWNER',
        TO_DATE('2017-06-26', 'YYYY-MM-DD'), 'CN', null, null, null, null, 'W1',
        null, null, null, null, null, 'MR', null, 'QQM25S', 'A',
        TO_DATE('2017-02-17', 'YYYY-MM-DD'), '1234',
        TO_DATE('2017-06-26', 'YYYY-MM-DD'), 'OMS_OWNER', 'MERGE', 'pgrant',
        '10.102.1.69', 'unknown', null);

INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('NMI', 'NOTTINGHAM (HMP)', 'INST', 'Y');

INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('DRBYCC', 'Derby Crown Court', 'CRT', 'Y');

INSERT INTO MOVEMENT_REASONS (MOVEMENT_TYPE, MOVEMENT_REASON_CODE, DESCRIPTION, OPEN_CONTACT_FLAG,
                                        CLOSE_CONTACT_FLAG, ACTIVE_FLAG, LIST_SEQ, UPDATE_ALLOWED_FLAG, EXPIRY_DATE,
                                        CREATE_USER_ID, NOTIFICATION_TYPE, NOTIFICATION_FLAG, BILLING_SERVICE_FLAG,
                                        TRANSPORTATION_FLAG, HEADER_STATUS_FLAG, IN_MOVEMENT_TYPE,
                                        IN_MOVEMENT_REASON_CODE, ESC_RECAP_FLAG, CREATE_DATETIME, MODIFY_DATETIME,
                                        MODIFY_USER_ID, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                                        AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                                        AUDIT_ADDITIONAL_INFO, UNEMPLOYMENT_PAY)
VALUES ('CRT', '19', 'Court Appearance - Police Product Order', 'Y', 'N', 'Y', 99, 'Y', null, 'SYSTEM', 'N', 'N', 'N',
        'N', 'N', null, null, 'N', TO_DATE('2006-01-13', 'YYYY-MM-DD'),
        TO_DATE('2006-07-24', 'YYYY-MM-DD'), 'OMS_OWNER',
        TO_DATE('2006-07-24', 'YYYY-MM-DD'), 'OMS_OWNER',
        'sqlplus@ukmbnom003 (TNS V1-V3)', 'oracle', '10.96.137.200', 'pts/1', null, 'N');

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
                               AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME, AUDIT_CLIENT_USER_ID,
                               AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME, AUDIT_ADDITIONAL_INFO,
                               BOOKING_SEQ, ADMISSION_REASON)
VALUES (1176156, TO_DATE('2017-02-17', 'YYYY-MM-DD'), null, '26972A', 2554472, 'NMI', 106258, 'N',
        'OUT', 'Y', 'O', 'N', null, null, null, 46991, 'NMI', 'INST', null, 2554472, null, 'N', null, null, null, null,
        null, null, null, null, null, 'N', null, null, 'DET', null, null, null, null, 'CRT-19', null, null,
        TO_DATE('2017-02-17', 'YYYY-MM-DD'), 'QQM25S',
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'MNAWROCKI_GEN', null, null,
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'MNAWROCKI_GEN', 'OCUCANTR',
        'mnawrocki', '10.102.1.72', 'MGPRW4RI2SN0001', null, 1, null);

INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME,
                                         INTERNAL_SCHEDULE_TYPE, INTERNAL_SCHEDULE_REASON_CODE, MOVEMENT_TYPE,
                                         MOVEMENT_REASON_CODE, DIRECTION_CODE, ARREST_AGENCY_LOC_ID,
                                         TO_PROV_STAT_CODE, ESCORT_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID,
                                         ACTIVE_FLAG, ESCORT_TEXT, COMMENT_TEXT, REPORTING_DATE, TO_CITY,
                                         FROM_CITY, REPORTING_TIME, CREATE_DATETIME, CREATE_USER_ID,
                                         MODIFY_DATETIME, MODIFY_USER_ID, EVENT_ID, PARENT_EVENT_ID,
                                         TO_COUNTRY_CODE, OJ_LOCATION_CODE, APPLICATION_DATE,
                                         APPLICATION_TIME, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                                         AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS,
                                         AUDIT_CLIENT_WORKSTATION_NAME, AUDIT_ADDITIONAL_INFO, TO_ADDRESS_ID,
                                         FROM_ADDRESS_ID)
VALUES (1176156, 1, TO_DATE('2017-04-01', 'YYYY-MM-DD'),
        TO_DATE('2017-04-01', 'YYYY-MM-DD'), null, null, 'ADM', 'I', 'IN', 'POL', null, 'GEOAME',
        'DRBYCC', 'NMI', 'N', null, null, null, null, null, null,
        TO_DATE('2017-02-17', 'YYYY-MM-DD'), 'QQM25S',
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'MNAWROCKI_GEN', null, null, null,
        null, null, null, TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'MNAWROCKI_GEN',
        'OCUCANTR', 'mnawrocki', '10.102.1.72', 'MGPRW4RI2SN0001', null, null, null);

INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME,
                                         INTERNAL_SCHEDULE_TYPE, INTERNAL_SCHEDULE_REASON_CODE, MOVEMENT_TYPE,
                                         MOVEMENT_REASON_CODE, DIRECTION_CODE, ARREST_AGENCY_LOC_ID,
                                         TO_PROV_STAT_CODE, ESCORT_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID,
                                         ACTIVE_FLAG, ESCORT_TEXT, COMMENT_TEXT, REPORTING_DATE, TO_CITY,
                                         FROM_CITY, REPORTING_TIME, CREATE_DATETIME, CREATE_USER_ID,
                                         MODIFY_DATETIME, MODIFY_USER_ID, EVENT_ID, PARENT_EVENT_ID,
                                         TO_COUNTRY_CODE, OJ_LOCATION_CODE, APPLICATION_DATE,
                                         APPLICATION_TIME, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                                         AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS,
                                         AUDIT_CLIENT_WORKSTATION_NAME, AUDIT_ADDITIONAL_INFO, TO_ADDRESS_ID,
                                         FROM_ADDRESS_ID)
VALUES (1176156, 2, TO_DATE('2021-11-30', 'YYYY-MM-DD'),
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), null, null, 'CRT', '19', 'OUT', null, null, null,
        'NMI', 'ABDRCT', 'Y', null, null, null, null, null, null,
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'MNAWROCKI_GEN', null, null,
        455654697, null, null, null, null, null,
        TO_DATE('2021-11-30', 'YYYY-MM-DD'), 'MNAWROCKI_GEN', 'OCUCANTR',
        'mnawrocki', '10.102.1.72', 'MGPRW4RI2SN0001', null, null, null);