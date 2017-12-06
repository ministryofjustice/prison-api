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
