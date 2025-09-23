INSERT INTO OFFENDER_MILITARY_RECORDS (OFFENDER_BOOK_ID, MILITARY_SEQ, WAR_ZONE_CODE, START_DATE, END_DATE,
                                       MILITARY_DISCHARGE_CODE, MILITARY_BRANCH_CODE, DESCRIPTION, UNIT_NUMBER,
                                       ENLISTMENT_LOCATION, DISCHARGE_LOCATION, SELECTIVE_SERVICES_FLAG,
                                       MILITARY_RANK_CODE, SERVICE_NUMBER, DISCIPLINARY_ACTION_CODE, CREATE_DATETIME,
                                       CREATE_USER_ID, MODIFY_DATETIME, MODIFY_USER_ID, AUDIT_TIMESTAMP, AUDIT_USER_ID,
                                       AUDIT_MODULE_NAME, AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS,
                                       AUDIT_CLIENT_WORKSTATION_NAME, AUDIT_ADDITIONAL_INFO)
VALUES (-1, 1, 'AFG', TIMESTAMP '2000-01-01 00:00:00.000000', TIMESTAMP '2020-10-17 00:00:00.000000',
        'DIS', 'ARM', 'Some Description Text', 'auno', 'Somewhere', 'Sheffield', 'N', 'LCPL_RMA', 'asno', 'CM', sysdate,
        'PPHILLIPS_GEN', sysdate - 1 / 24, 'PPHILLIPS_GEN', sysdate - 2 / 24,
        'PPHILLIPS_GEN', 'OIDMHIST', 'pphillips', '10.102.2.4', 'MGMRW0100', null),
       (-1, 2, null, TIMESTAMP '2001-01-01 00:00:00.000000', null, null, 'NAV', 'second record', null, null, null, 'N',
        null, null, null, sysdate - 3 / 24, 'PPHILLIPS_GEN', null, null, sysdate - 4 / 24, 'PPHILLIPS_GEN', 'OIDMHIST',
        'pphillips', '10.102.2.4', 'MGMRW0100', null);

-- Used by PrisonerSearchResourceInt to check that the militaryRecord flag is set correctly.
INSERT INTO OFFENDER_MILITARY_RECORDS (OFFENDER_BOOK_ID, MILITARY_SEQ, WAR_ZONE_CODE, START_DATE, END_DATE,
                                       MILITARY_DISCHARGE_CODE, MILITARY_BRANCH_CODE, DESCRIPTION, UNIT_NUMBER,
                                       ENLISTMENT_LOCATION, DISCHARGE_LOCATION, SELECTIVE_SERVICES_FLAG)
VALUES (-2, 1, 'AFG', TIMESTAMP '2000-01-01 00:00:00.000000', TIMESTAMP '2020-10-17 00:00:00.000000',
        'DIS', 'ARM', 'Some Description Text', 'auno', 'Somewhere', 'Sheffield', 'N');
