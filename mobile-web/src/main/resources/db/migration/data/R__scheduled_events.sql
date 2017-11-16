-------------------------------------------------------------------
-- Seed data for Prison Activities (PRISON_ACT) Scheduled Events --
-------------------------------------------------------------------

-- COURSE_ACTIVITIES (Course activity definition)
INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
  VALUES (-1, 'LEI', 'LEI', 'Chapel Cleaner', 15, 'Y', '2016-08-08', 'INST', -1, -25, 'AGY', 'LEI', 'CC1', 'CHAP', 'BAS'),
         (-2, 'LEI', 'LEI', 'Woodwork', 10, 'Y', '2012-02-28', 'INST', -2, -26, 'AGY', 'LEI', 'WOOD', 'EDUC', 'STD'),
         (-3, 'LEI', 'LEI', 'Substance misuse course', 5, 'Y', '2011-01-04', 'INST', -3, -27, 'AGY', 'LEI', 'SUBS', 'EDUC', 'BAS'),
         (-4, 'LEI', 'LEI', 'Core classes', 25, 'Y', '2009-07-04', 'INST', -4, -27, 'AGY', 'LEI', 'CORE', 'EDUC', 'STD');

-- COURSE_SCHEDULES (Course activity classes/occurrences/sessions)
-- NB: Dates deliberately out of sequence for first 5 records (to allow default sorting to be verified)
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
  VALUES (-1, -1, '2017-09-12', '2017-09-12 09:30:00', '2017-09-12 11:30:00', 'SCH'),
         (-2, -1, '2017-09-15', '2017-09-15 09:30:00', '2017-09-15 11:30:00', 'SCH'),
         (-3, -1, '2017-09-13', '2017-09-13 09:30:00', '2017-09-13 11:30:00', 'SCH'),
         (-4, -1, '2017-09-11', '2017-09-11 09:30:00', '2017-09-11 11:30:00', 'SCH'),
         (-5, -1, '2017-09-14', '2017-09-14 09:30:00', '2017-09-14 11:30:00', 'SCH'),
         (-6, -2, '2017-09-11', '2017-09-11 13:00:00', '2017-09-11 15:00:00', 'SCH'),
         (-7, -2, '2017-09-12', '2017-09-12 13:00:00', '2017-09-12 15:00:00', 'SCH'),
         (-8, -2, '2017-09-13', '2017-09-13 13:00:00', '2017-09-13 15:00:00', 'SCH'),
         (-9, -2, '2017-09-14', '2017-09-14 13:00:00', '2017-09-14 15:00:00', 'SCH'),
         (-10, -2, '2017-09-15', '2017-09-15 13:00:00', '2017-09-15 15:00:00', 'SCH'),
         (-11, -3, '2017-09-12', '2017-09-12 13:00:00', '2017-09-12 15:00:00', 'SCH'),
         (-12, -3, '2017-09-15', '2017-09-15 13:00:00', '2017-09-15 15:00:00', 'SCH'),
         (-13, -3, '2017-09-13', '2017-09-13 13:00:00', '2017-09-13 15:00:00', 'SCH'),
         (-14, -3, '2017-09-11', '2017-09-11 13:00:00', '2017-09-11 15:00:00', 'SCH'),
         (-15, -3, '2017-09-14', '2017-09-14 13:00:00', '2017-09-14 15:00:00', 'SCH'),
         (-16, -1, '2017-09-19', '2017-09-19 09:30:00', '2017-09-19 11:30:00', 'SCH'),
         (-17, -1, '2017-09-22', '2017-09-22 09:30:00', '2017-09-22 11:30:00', 'SCH'),
         (-18, -1, '2017-09-20', '2017-09-20 09:30:00', '2017-09-20 11:30:00', 'SCH'),
         (-19, -1, '2017-09-18', '2017-09-18 09:30:00', '2017-09-18 11:30:00', 'SCH'),
         (-20, -1, '2017-09-21', '2017-09-21 09:30:00', '2017-09-21 11:30:00', 'SCH'),
         (-21, -4, '2017-09-25', '2017-09-25 09:30:00', '2017-09-25 11:30:00', 'SCH'),
         (-22, -4, '2017-09-26', '2017-09-26 09:30:00', '2017-09-26 11:30:00', 'SCH'),
         (-23, -4, '2017-09-27', '2017-09-27 09:30:00', '2017-09-27 11:30:00', 'SCH'),
         (-24, -4, '2017-09-28', '2017-09-28 09:30:00', '2017-09-28 11:30:00', 'SCH'),
         (-25, -4, '2017-09-29', '2017-09-29 09:30:00', '2017-09-29 11:30:00', 'SCH');

-- These course schedules defined for current day (to test 'today' endpoint action).
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
  VALUES (-26, -2, current_date, now(), now(), 'SCH'),
         (-27, -2, current_date, now(), now(), 'SCH'),
         (-28, -4, current_date, now() + interval '3' day, now() + interval '3' day, 'SCHC1'),
         (-29, -4, current_date, now() + interval '3' day, now() + interval '3' day, 'SCHC2');

-- OFFENDER_PROGRAM_PROFILES (Allocation of offenders to course activities)
INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
  VALUES (-1, -1, -1, '2016-11-09', 'ALLOC', -1, null, null, null, null, null, 'N', 'LEI', null, null),
         (-2, -1, -2, '2012-07-05', 'END', -2, null, null, 'TRF', null, '2016-10-21', 'N', 'LEI', null, null),
         (-3, -1, -3, '2016-11-09', 'ALLOC', -3, null, null, null, null, null, 'N', 'LEI', null, null),
         (-4, -1, -4, null, 'PLAN', null, null, null, null, null, null, 'N', null, null, -1),
         (-5, -2, -2, '2016-11-09', 'ALLOC', -2, null, null, null, null, null, 'N', 'LEI', null, null),
         (-6, -3, -2, '2016-11-09', 'ALLOC', -2, null, null, null, null, null, 'N', 'LEI', null, null),
         (-7, -4, -4, '2016-11-09', 'ALLOC', -4, null, null, null, null, null, 'N', 'LEI', null, null),
         (-8, -5, -1, '2016-11-09', 'ALLOC', -1, null, null, null, null, null, 'N', 'LEI', null, null);

-- OFFENDER_COURSE_ATTENDANCES (record of offenders having attended scheduled activities)
INSERT INTO OFFENDER_COURSE_ATTENDANCES (EVENT_ID, OFFENDER_BOOK_ID, CRS_SCH_ID, EVENT_CLASS, EVENT_TYPE, EVENT_SUB_TYPE, EVENT_DATE, EVENT_STATUS)
  VALUES (-1, -3, -6, 'INT_MOV', 'PRISON_ACT', 'EDUC', '2017-09-11', 'EXP'),
         (-2, -3, -7, 'INT_MOV', 'PRISON_ACT', 'EDUC', '2017-09-12', 'SCH');


---------------------------------------------------
-- Seed data for Visits (VISIT) Scheduled Events --
---------------------------------------------------

-- OFFENDER_VISITS (record of scheduled visits)
-- NB: Dates deliberately out of sequence (to allow default sorting to be verified)
INSERT INTO OFFENDER_VISITS (OFFENDER_VISIT_ID, OFFENDER_BOOK_ID, VISIT_DATE, START_TIME, END_TIME, VISIT_TYPE, VISIT_STATUS, VISIT_INTERNAL_LOCATION_ID, AGY_LOC_ID)
  VALUES (-1, -1, '2017-09-12', '2017-09-12 14:30:00', '2017-09-12 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-2, -1, '2017-11-13', '2017-11-13 14:30:00', '2017-11-13 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-3, -1, '2017-12-12', '2017-12-12 14:30:00', '2017-12-12 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-4, -1, '2017-10-13', '2017-10-13 14:30:00', '2017-10-13 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-5, -1, '2017-09-15', '2017-09-15 14:00:00', '2017-09-15 16:00:00', 'OFFI', 'SCH', -25, 'LEI'),
         (-6, -1, '2017-09-10', '2017-09-10 14:30:00', '2017-09-10 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-7, -1, '2017-07-10', '2017-07-10 14:30:00', '2017-07-10 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-8, -1, '2017-08-10', '2017-08-10 14:30:00', '2017-08-10 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-9, -1, '2017-05-10', '2017-05-10 14:30:00', '2017-05-10 16:30:00', 'OFFI', 'SCH', -25, 'LEI'),
         (-10, -1, '2017-06-10', '2017-06-10 14:30:00', '2017-06-10 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-11, -1, '2017-01-10', '2017-01-10 14:30:00', '2017-01-10 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-12, -1, '2017-02-10', '2017-02-10 14:30:00', '2017-02-10 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-13, -1, '2017-04-10', '2017-04-10 14:30:00', '2017-04-10 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-14, -1, '2017-03-10', '2017-03-10 14:30:00', '2017-03-10 16:30:00', 'OFFI', 'SCH', -25, 'LEI'),
         (-15, -1, '2016-12-11', '2016-12-11 14:30:00', '2016-12-11 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-16, -2, '2017-10-10', '2017-10-10 10:00:00', '2017-10-10 12:00:00', 'OFFI', 'SCH', -25, 'LEI');

-- These visits defined for current day (to test 'today' endpoint action).
INSERT INTO OFFENDER_VISITS (OFFENDER_VISIT_ID, OFFENDER_BOOK_ID, VISIT_DATE, START_TIME, END_TIME, VISIT_TYPE, VISIT_STATUS, VISIT_INTERNAL_LOCATION_ID, AGY_LOC_ID)
  VALUES (-17, -3, current_date, now(), now(), 'OFFI', 'SCH', -25, 'LEI'),
         (-18, -3, current_date, now(), now(), 'SCON', 'SCH', -28, 'LEI'),
         (-19, -3, current_date, now() + interval '3' day, now() + interval '3' day, 'SCON', 'SCHV1', -28, 'LEI'),
         (-20, -3, current_date, now() + interval '3' day, now() + interval '3' day, 'SCON', 'SCHV2', -28, 'LEI'),
         (-21, -3, current_date, now() + interval '3' day, now() + interval '3' day, 'SCON', 'SCHV3', -28, 'LEI'),
         (-22, -3, current_date, now() + interval '3' day, now() + interval '3' day, 'SCON', 'SCHV4', -28, 'LEI');


-------------------------------------------------------
-- Seed data for Appointments (APP) Scheduled Events --
-------------------------------------------------------

-- OFFENDER_IND_SCHEDULES (record of individual scheduled events, incl. appointments)
-- NB: Dates deliberately out of sequence (to allow default sorting to be verified)
INSERT INTO OFFENDER_IND_SCHEDULES (EVENT_ID, OFFENDER_BOOK_ID, EVENT_DATE, START_TIME, END_TIME, EVENT_CLASS, EVENT_TYPE, EVENT_SUB_TYPE, EVENT_STATUS, TO_AGY_LOC_ID, TO_INTERNAL_LOCATION_ID, TO_ADDRESS_ID, TO_CITY_CODE)
  VALUES (-1, -1, '2017-09-15', '2017-09-15 14:30:00',    '2017-09-15 15:00:00',    'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null),
         (-2, -3, current_date, now() + interval '3' day, now() + interval '3' day, 'INT_MOV', 'APP', 'IMM', 'SCHA1', 'LEI', -28, null, null),
         (-3, -3, current_date, now() + interval '3' day, now() + interval '3' day, 'INT_MOV', 'APP', 'IMM', 'SCHA2', 'LEI', -28, null, null),
         (-4, -3, current_date, now() + interval '3' day, now() + interval '3' day, 'INT_MOV', 'APP', 'IMM', 'SCHA3', 'LEI', -28, null, null),
         (-16, -2, '2017-05-12', '2017-05-12 09:30:00',   '2017-05-12 10:00:00',    'INT_MOV', 'APP', 'IMM', 'SCH', 'LEI', -28, null, null);
