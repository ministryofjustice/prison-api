-----------------------------
-- Seed data for Addresses --
-----------------------------
INSERT INTO ADDRESSES (ADDRESS_ID, OWNER_CLASS, OWNER_CODE, ADDRESS_TYPE, PRIMARY_FLAG, MAIL_FLAG, STREET)
  VALUES (-1, 'AGY', 'BRMSYC', 'BUS', 'Y', 'Y', 'Justice Avenue'),
         (-2, 'AGY', 'WELBYC', 'BUS', 'Y', 'Y', 'Peyton Place');


-------------------------------------------------------------------
-- Seed data for Prison Activities (PRISON_ACT) Scheduled Events --
-------------------------------------------------------------------

-- COURSE_ACTIVITIES (Course activity definition)
INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
  VALUES (-1, 'LEI', 'LEI', 'Chapel Cleaner', 15, 'Y', '2016-08-08', 'INST', -1, -25, 'AGY', 'LEI', 'CC1', 'CHAP', 'BAS'),
         (-2, 'LEI', 'LEI', 'Woodwork', 10, 'Y', '2012-02-28', 'INST', -2, -26, 'AGY', 'LEI', 'WOOD', 'EDUC', 'STD'),
         (-3, 'LEI', 'LEI', 'Substance misuse course', 5, 'Y', '2011-01-04', 'INST', -3, -27, 'AGY', 'LEI', 'SUBS', 'EDUC', 'BAS'),
         (-4, 'LEI', 'LEI', 'Core classes', 25, 'Y', '2009-07-04', 'INST', -4, -27, 'AGY', 'LEI', 'CORE', 'EDUC', 'STD'),
         (-5, 'LEI', 'LEI', 'Weeding', 20, 'Y', '2009-07-04', 'INST', -5, -28, 'AGY', 'LEI', 'FG1', 'EDUC', 'BAS'),
         (-6, 'LEI', 'LEI', 'Address Testing', 99, 'Y', '2009-07-04', 'INST', -6, -29, 'AGY', 'LEI', 'ABS', 'EDUC', 'STD');

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

-- These course schedules defined for current day, this week and next week (to test 'today', 'thisWeek' and 'nextWeek' endpoint actions).
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
  VALUES (-26, -2, current_date, now() + INTERVAL '5' SECOND, now() + INTERVAL '5' SECOND, 'SCH'),
         (-27, -2, current_date, now() + INTERVAL '6' SECOND, now() + INTERVAL '6' SECOND, 'SCH'),
         (-28, -5, current_date + INTERVAL  '6' DAY, now() + INTERVAL  '6' DAY, now() + INTERVAL  '6' DAY, 'SCH'),
         (-29, -6, current_date + INTERVAL  '3' DAY, now() + INTERVAL  '3' DAY, now() + INTERVAL  '3' DAY, 'SCH'),
         (-30, -5, current_date + INTERVAL '17' DAY, now() + INTERVAL '17' DAY, now() + INTERVAL '17' DAY, 'SCH'),
         (-31, -6, current_date + INTERVAL  '9' DAY, now() + INTERVAL  '9' DAY, now() + INTERVAL  '9' DAY, 'SCH');

-- OFFENDER_PROGRAM_PROFILES (Allocation of offenders to course activities)
INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
  VALUES (-1, -1, -1, '2016-11-09', 'ALLOC', -1, null, null, null, null, null, 'N', 'LEI', null, null),
         (-2, -1, -2, '2012-07-05', 'END', -2, null, null, 'TRF', null, '2016-10-21', 'N', 'LEI', null, null),
         (-3, -1, -3, '2016-11-09', 'ALLOC', -3, null, null, null, null, null, 'N', 'LEI', null, null),
         (-4, -1, -4, null, 'PLAN', null, null, null, null, null, null, 'N', null, null, -1),
         (-5, -2, -2, '2016-11-09', 'ALLOC', -2, null, null, null, null, null, 'N', 'LEI', null, null),
         (-6, -3, -2, '2016-11-09', 'ALLOC', -2, null, null, null, null, null, 'N', 'LEI', null, null),
         (-10, -3, -5, '2016-11-09', 'ALLOC', -5, null, null, null, null, null, 'N', 'LEI', null, null),
         (-11, -3, -6, '2016-11-09', 'ALLOC', -6, null, null, null, null, null, 'N', 'LEI', null, null),
         (-7, -4, -4, '2016-11-09', 'ALLOC', -4, null, null, null, null, null, 'N', 'LEI', null, null),
         (-8, -5, -1, '2016-11-09', 'ALLOC', -1, null, null, null, null, null, 'N', 'LEI', null, null);

-- OFFENDER_COURSE_ATTENDANCES (record of offenders attendance of scheduled activities)
INSERT INTO OFFENDER_COURSE_ATTENDANCES (EVENT_ID, OFFENDER_BOOK_ID, CRS_SCH_ID, EVENT_CLASS, EVENT_TYPE, EVENT_SUB_TYPE, EVENT_DATE, EVENT_STATUS)
  VALUES (-1, -3, -6, 'INT_MOV', 'PRISON_ACT', 'EDUC', '2017-09-11', 'COMP'),
         (-2, -3, -7, 'INT_MOV', 'PRISON_ACT', 'EDUC', '2017-09-12', 'EXP'),
         (-3, -3, -8, 'INT_MOV', 'PRISON_ACT', 'EDUC', '2017-09-13', 'CANC'),
         (-4, -3, -9, 'INT_MOV', 'PRISON_ACT', 'EDUC', '2017-09-14', 'SCH'),
         (-5, -3, -10, 'INT_MOV', 'PRISON_ACT', 'EDUC', '2017-09-15', 'SCH');

---------------------------------------------------
-- Seed data for Visits (VISIT) Scheduled Events --
---------------------------------------------------

-- OFFENDER_VISITS (record of scheduled visits)
-- NB: Dates deliberately out of sequence (to allow default sorting to be verified)
INSERT INTO OFFENDER_VISITS (OFFENDER_VISIT_ID, OFFENDER_BOOK_ID, VISIT_DATE, START_TIME, END_TIME, VISIT_TYPE, VISIT_STATUS, VISIT_INTERNAL_LOCATION_ID, AGY_LOC_ID)
  VALUES (-1, -1, '2017-09-12', '2017-09-12 14:30:00', '2017-09-12 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-2, -1, '2017-11-13', '2017-11-13 14:30:00', '2017-11-13 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
         (-3, -1, '2017-12-10', '2017-12-10 14:30:00', '2017-12-10 15:30:00', 'SCON', 'SCH', -28, 'LEI'),
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

-- These visits defined for current day, this week and next week (to test 'today', 'thisWeek' and 'nextWeek' endpoint actions).
INSERT INTO OFFENDER_VISITS (OFFENDER_VISIT_ID, OFFENDER_BOOK_ID, VISIT_DATE, START_TIME, END_TIME, VISIT_TYPE, VISIT_STATUS, VISIT_INTERNAL_LOCATION_ID, AGY_LOC_ID)
  VALUES (-17, -3, current_date + INTERVAL '1' SECOND, now() + INTERVAL '1' SECOND, now() + INTERVAL '1' SECOND, 'OFFI', 'SCH', -25, 'LEI'),
         (-18, -3, current_date + INTERVAL '2' SECOND, now() + INTERVAL '2' SECOND, now() + INTERVAL '2' SECOND, 'SCON', 'SCH', -28, 'LEI'),
         (-19, -3, current_date + INTERVAL '1' DAY, now() + INTERVAL '1' DAY, now() + INTERVAL '1' DAY, 'SCON', 'SCH', -26, 'LEI'),
         (-21, -3, current_date + INTERVAL '2' DAY, now() + INTERVAL '2' DAY, now() + INTERVAL '2' DAY, 'SCON', 'SCH', -29, 'LEI'),
         (-22, -3, current_date + INTERVAL '4' DAY, now() + INTERVAL '4' DAY, now() + INTERVAL '4' DAY, 'SCON', 'SCH', -13, 'LEI'),
         (-23, -3, current_date + INTERVAL '8' DAY, now() + INTERVAL '8' DAY, now() + INTERVAL '8' DAY, 'SCON', 'SCH', -26, 'LEI'),
         (-24, -3, current_date + INTERVAL '10' DAY, now() + INTERVAL '10' DAY, now() + INTERVAL '10' DAY, 'SCON', 'SCH', -27, 'LEI'),
         (-25, -3, current_date + INTERVAL '16' DAY, now() + INTERVAL '16' DAY, now() + INTERVAL '16' DAY, 'SCON', 'SCH', -28, 'LEI');

-- These visits defined to be found by the 'last' endpoint.
--INSERT INTO OFFENDER_VISITS (OFFENDER_VISIT_ID, OFFENDER_BOOK_ID, VISIT_DATE, START_TIME, END_TIME, VISIT_TYPE, VISIT_STATUS, VISIT_INTERNAL_LOCATION_ID, AGY_LOC_ID)
--  VALUES (-26, -4, current_date, now() - INTERVAL '2' MINUTE, now() - INTERVAL '1' MINUTE, 'OFFI', 'SCH', -25, 'LEI'),
--         (-27, -4, current_date, now() + INTERVAL '1' MINUTE, now() + INTERVAL '2' MINUTE, 'SCON', 'SCH', -28, 'LEI'),
 
-------------------------------------------------------
-- Seed data for Appointments (APP) Scheduled Events --
-------------------------------------------------------

-- OFFENDER_IND_SCHEDULES (record of individual scheduled events, incl. appointments)
-- NB: Dates deliberately out of sequence (to allow default sorting to be verified)
INSERT INTO OFFENDER_IND_SCHEDULES (EVENT_ID, OFFENDER_BOOK_ID, EVENT_DATE, START_TIME, END_TIME, EVENT_CLASS, EVENT_TYPE, EVENT_SUB_TYPE, EVENT_STATUS, TO_AGY_LOC_ID, TO_INTERNAL_LOCATION_ID, TO_ADDRESS_ID, TO_CITY_CODE, COMMENT_TEXT)
  VALUES (-1, -1, '2017-09-15', '2017-09-15 14:30:00', '2017-09-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment1'),
         (-2, -1, '2017-08-15', '2017-08-15 14:30:00', '2017-08-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment2'),
         (-3, -1, '2017-08-12', '2017-08-12 15:00:00', '2017-08-15 16:00:00', 'INT_MOV', 'APP', 'CHAP', 'SCH', 'LEI', null, null, null, 'comment3'),
         (-4, -1, '2017-09-18', '2017-09-18 13:30:00', '2017-09-18 15:30:00', 'INT_MOV', 'APP', 'IMM', 'SCH', null, null, -1, null, 'comment4'),
         (-5, -1, '2017-07-22', '2017-07-22 09:30:00', '2017-07-22 11:30:00', 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -27, null, null, 'comment5'),
         (-6, -1, '2017-06-15', '2017-06-15 14:30:00', '2017-06-15 15:00:00', 'INT_MOV', 'APP', 'MEPS', 'SCH', 'LEI', -29, null, null, 'comment6'),
         (-7, -1, '2017-05-15', '2017-05-15 14:30:00', '2017-05-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment7'),
         (-8, -1, '2017-04-15', '2017-04-15 14:30:00', '2017-04-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', null, -29, null, null, 'comment8'),
         (-9, -1, '2017-03-15', '2017-03-15 14:30:00', '2017-03-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment9'),
         (-10, -1, '2017-02-15', '2017-02-15 14:30:00', '2017-02-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', null, null, null, '29216', 'comment10'),
         (-11, -1, '2017-01-15', '2017-01-15 14:30:00', '2017-01-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment11'),
         (-12, -1, '2017-10-15', '2017-10-15 14:30:00', '2017-10-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment12'),
         (-13, -1, '2017-11-15', '2017-11-15 14:30:00', '2017-11-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment13'),
         (-14, -1, '2017-12-15', '2017-12-15 14:30:00', '2017-12-15 15:00:00', 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment14'),
         (-15, -1, '2017-12-25', '2017-12-25 09:00:00', '2017-12-25 10:00:00', 'INT_MOV', 'APP', 'CHAP', 'SCH', 'LEI', -25, null, null, 'comment15'),
         (-16, -2, '2017-05-12', '2017-05-12 09:30:00', '2017-05-12 10:00:00', 'INT_MOV', 'APP', 'IMM', 'SCH', 'LEI', -28, null, null, 'comment16');

-- These appointments defined for current day, this week and next week (to test 'today', 'thisWeek' and 'nextWeek' endpoint actions).
INSERT INTO OFFENDER_IND_SCHEDULES (EVENT_ID, OFFENDER_BOOK_ID, EVENT_DATE, START_TIME, END_TIME, EVENT_CLASS, EVENT_TYPE, EVENT_SUB_TYPE, EVENT_STATUS, TO_AGY_LOC_ID, TO_INTERNAL_LOCATION_ID, TO_ADDRESS_ID, TO_CITY_CODE, COMMENT_TEXT)
  VALUES (-17, -3, current_date + INTERVAL '3' SECOND, now() + INTERVAL '3' SECOND, now() + INTERVAL '3' SECOND, 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment17'),
         (-18, -3, current_date + INTERVAL '4' SECOND, now() + INTERVAL '4' SECOND, now() + INTERVAL '4' SECOND, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment18'),
         (-19, -3, current_date + INTERVAL '1' DAY, now() + INTERVAL '1' DAY, now() + INTERVAL '1' DAY, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment19'),
         (-20, -3, current_date + INTERVAL '7' DAY, now() + INTERVAL '7' DAY, now() + INTERVAL '7' DAY, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment20'),
         (-21, -3, current_date + INTERVAL '12' DAY, now() + INTERVAL '12' DAY, now() + INTERVAL '12' DAY, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment21'),
         (-22, -3, current_date + INTERVAL '17' DAY, now() + INTERVAL '17' DAY, now() + INTERVAL '17' DAY, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment22');
