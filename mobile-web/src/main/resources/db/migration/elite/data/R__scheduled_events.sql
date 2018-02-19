-------------------------------------------------------------------
-- Seed data for Prison Activities (PRISON_ACT) Scheduled Events --
-------------------------------------------------------------------

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
  VALUES (-17, -3, current_date, trunc(now()) + INTERVAL '3' HOUR, trunc(now()) + INTERVAL '3' HOUR, 'INT_MOV', 'APP', 'MEDE', 'SCH', 'LEI', -29, null, null, 'comment17'),
         (-18, -3, current_date, trunc(now()) + INTERVAL '4' HOUR, trunc(now()) + INTERVAL '4' HOUR, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment18'),
         (-19, -3, current_date + INTERVAL '1' DAY, now() + INTERVAL '1' DAY, now() + INTERVAL '1' DAY, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment19'),
         (-20, -3, current_date + INTERVAL '7' DAY, now() + INTERVAL '7' DAY, now() + INTERVAL '7' DAY, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment20'),
         (-21, -3, current_date + INTERVAL '12' DAY, now() + INTERVAL '12' DAY, now() + INTERVAL '12' DAY, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment21'),
         (-22, -3, current_date + INTERVAL '17' DAY, now() + INTERVAL '17' DAY, now() + INTERVAL '17' DAY, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment22'),
-- extra event today for different offender to test groups
         (-23, -5, current_date, trunc(now()) + INTERVAL '1' HOUR, trunc(now()) + INTERVAL '1' HOUR, 'INT_MOV', 'APP', 'EDUC', 'SCH', 'LEI', -28, null, null, 'comment23');
         
