-------------------------------------------------------------------
-- Seed data for Prison Activities (PRISON_ACT) Scheduled Events --
-------------------------------------------------------------------

-- COURSE_SCHEDULES (Course activity classes/occurrences/sessions; what dates and times the courses run on)
-- NB: Dates deliberately out of sequence for first 5 records (to allow default sorting to be verified)
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-1, -1, TO_DATE('2017-09-12', 'YYYY-MM-DD'), TO_DATE('2017-09-12 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-12 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-2, -1, TO_DATE('2017-09-15', 'YYYY-MM-DD'), TO_DATE('2017-09-15 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-15 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-3, -1, TO_DATE('2017-09-13', 'YYYY-MM-DD'), TO_DATE('2017-09-13 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-13 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-4, -1, TO_DATE('2017-09-11', 'YYYY-MM-DD'), TO_DATE('2017-09-11 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-11 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-5, -1, TO_DATE('2017-09-14', 'YYYY-MM-DD'), TO_DATE('2017-09-14 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-14 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-6, -2, TO_DATE('2017-09-11', 'YYYY-MM-DD'), TO_DATE('2017-09-11 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-11 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-7, -2, TO_DATE('2017-09-12', 'YYYY-MM-DD'), TO_DATE('2017-09-12 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-12 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-8, -2, TO_DATE('2017-09-13', 'YYYY-MM-DD'), TO_DATE('2017-09-13 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-13 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-9, -2, TO_DATE('2017-09-14', 'YYYY-MM-DD'), TO_DATE('2017-09-14 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-14 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS, SLOT_CATEGORY_CODE) VALUES (-10, -2, TO_DATE('2017-09-15', 'YYYY-MM-DD'), TO_DATE('2017-09-15 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-15 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH', 'PM');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-11, -3, TO_DATE('2017-09-12', 'YYYY-MM-DD'), TO_DATE('2017-09-12 13:05:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-12 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-12, -3, TO_DATE('2017-09-15', 'YYYY-MM-DD'), TO_DATE('2017-09-15 13:05:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-15 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-13, -3, TO_DATE('2017-09-13', 'YYYY-MM-DD'), TO_DATE('2017-09-13 13:05:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-13 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-14, -3, TO_DATE('2017-09-11', 'YYYY-MM-DD'), TO_DATE('2017-09-11 13:05:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-11 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-15, -3, TO_DATE('2017-09-14', 'YYYY-MM-DD'), TO_DATE('2017-09-14 13:05:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-14 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-16, -1, TO_DATE('2017-09-19', 'YYYY-MM-DD'), TO_DATE('2017-09-19 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-19 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-17, -1, TO_DATE('2017-09-22', 'YYYY-MM-DD'), TO_DATE('2017-09-22 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-22 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-18, -1, TO_DATE('2017-09-20', 'YYYY-MM-DD'), TO_DATE('2017-09-20 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-20 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-19, -1, TO_DATE('2017-09-18', 'YYYY-MM-DD'), TO_DATE('2017-09-18 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-18 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-20, -1, TO_DATE('2017-09-21', 'YYYY-MM-DD'), TO_DATE('2017-09-21 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-21 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-21, -4, TO_DATE('2017-09-25', 'YYYY-MM-DD'), TO_DATE('2017-09-25 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-25 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-22, -4, TO_DATE('2017-09-26', 'YYYY-MM-DD'), TO_DATE('2017-09-26 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-26 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-23, -4, TO_DATE('2017-09-27', 'YYYY-MM-DD'), TO_DATE('2017-09-27 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-27 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-24, -4, TO_DATE('2017-09-28', 'YYYY-MM-DD'), TO_DATE('2017-09-28 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-28 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-25, -4, TO_DATE('2017-09-29', 'YYYY-MM-DD'), TO_DATE('2017-09-29 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-29 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-32, -3, TO_DATE('2017-09-12', 'YYYY-MM-DD'), TO_DATE('2017-09-12 13:10:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-12 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');

-- These course schedules defined for current day, this week and next week (to test 'today', 'thisWeek' and 'nextWeek' endpoint actions).
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-26, -2, trunc(sysdate), trunc(sysdate) + INTERVAL '12' HOUR, trunc(sysdate) + INTERVAL '12' HOUR, 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-27, -2, trunc(sysdate), trunc(sysdate) + INTERVAL '13' HOUR, trunc(sysdate) + INTERVAL '13' HOUR, 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-28, -5, trunc(sysdate) + INTERVAL  '6' DAY, trunc(sysdate) + INTERVAL  '6' DAY, trunc(sysdate) + INTERVAL  '6' DAY, 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-29, -6, trunc(sysdate) + INTERVAL  '3' DAY, trunc(sysdate) + INTERVAL  '3' DAY, trunc(sysdate) + INTERVAL  '3' DAY, 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-30, -5, trunc(sysdate) + INTERVAL '17' DAY, trunc(sysdate) + INTERVAL '17' DAY, trunc(sysdate) + INTERVAL '17' DAY, 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-31, -6, trunc(sysdate) + INTERVAL  '9' DAY, trunc(sysdate) + INTERVAL  '9' DAY, trunc(sysdate) + INTERVAL  '9' DAY, 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-34, -2, trunc(sysdate), trunc(sysdate) + 10/24, trunc(sysdate) + 12/24, 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-35, -4, TO_DATE('1985-01-01', 'YYYY-MM-DD'), TO_DATE('1985-01-01 13:10:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('1985-01-01 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
