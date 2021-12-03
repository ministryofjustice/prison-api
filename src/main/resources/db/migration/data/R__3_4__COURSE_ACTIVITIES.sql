-------------------------------------------------------------------
-- Seed data for Prison Activities (PRISON_ACT) Scheduled Events --
-------------------------------------------------------------------

-- COURSE_ACTIVITIES (Course activity definition)

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, SCHEDULE_END_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-1,  'LEI',  'LEI', 'Chapel Cleaner', 15, 'Y', TO_DATE('2016-08-08', 'YYYY-MM-DD'), TO_DATE('2016-09-08', 'YYYY-MM-DD'), 'INST', -1, -25, 'AGY',  'LEI', 'CC1', 'CHAP', 'BAS');

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, SCHEDULE_END_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-2,  'LEI',  'LEI', 'Woodwork', 10, 'Y', TO_DATE('2012-02-28', 'YYYY-MM-DD'), TO_DATE('2012-03-01', 'YYYY-MM-DD'), 'INST', -2, -26, 'AGY',  'LEI', 'WOOD', 'EDUC', 'STD');

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, SCHEDULE_END_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-3,  'LEI',  'LEI', 'Substance misuse course', 5, 'Y', TO_DATE('2011-01-04', 'YYYY-MM-DD'), TO_DATE('2012-01-08', 'YYYY-MM-DD'), 'INST', -3, -27, 'AGY',  'LEI', 'SUBS', 'EDUC', 'BAS');

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-4,  'LEI',  'LEI', 'Core classes', 25, 'Y', TO_DATE('2009-07-04', 'YYYY-MM-DD'), 'INST', -4, -27, 'AGY',  'LEI', 'CORE', 'EDUC', 'STD');

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-5,  'LEI',  'LEI', 'Weeding', 20, 'Y', TO_DATE('2009-07-04', 'YYYY-MM-DD'), 'INST', -5, -28, 'AGY',  'LEI', 'FG1', 'EDUC', 'BAS');

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-6,  'LEI',  'LEI', 'Address Testing', 99, 'Y', TO_DATE('2009-07-04', 'YYYY-MM-DD'), 'INST', -6, -29, 'AGY',  'LEI', 'ABS', 'EDUC', 'STD');

-- For Brixton
INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-3001,  'BXI',  'BXI', 'Gym session 1', 99, 'Y', TO_DATE('2009-07-04', 'YYYY-MM-DD'), 'INST', -6, -3001, 'AGY',  'BXI', 'ABS', 'EDUC', 'STD');

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-3002,  'BXI',  'BXI', 'Gym session 2', 99, 'Y', TO_DATE('2009-07-04', 'YYYY-MM-DD'), 'INST', -6, -3002, 'AGY',  'BXI', 'ABS', 'EDUC', 'STD');
