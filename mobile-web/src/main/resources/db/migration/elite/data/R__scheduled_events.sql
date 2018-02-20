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
