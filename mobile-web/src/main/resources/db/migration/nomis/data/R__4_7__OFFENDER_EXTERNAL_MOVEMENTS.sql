-- 'Active-Out (TAP)'
INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME, MOVEMENT_TYPE, MOVEMENT_REASON_CODE, DIRECTION_CODE, ARREST_AGENCY_LOC_ID, TO_PROV_STAT_CODE, ESCORT_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID, ACTIVE_FLAG, ESCORT_TEXT, COMMENT_TEXT, REPORTING_DATE, TO_CITY, FROM_CITY, REPORTING_TIME, EVENT_ID, PARENT_EVENT_ID, TO_COUNTRY_CODE, OJ_LOCATION_CODE, APPLICATION_DATE, TO_ADDRESS_ID, FROM_ADDRESS_ID)
    VALUES  (-17, 1, '2011-11-07', '2011-11-07 21:13:54', 'ADM', 'I',    'IN',  null, null, null, 'OUT',    'LEI',    'N', null, null, null, null, null, null, 1, null, null, null, null, null, null),
            (-17, 2, '2012-09-16', '2012-09-16 11:09:12', 'TRN', 'NOTR', 'OUT', null, null, null, 'LEI',    'BMI',    'N', null, null, '2012-09-16', null, null, null, 2, null, null, null, null, null, null),
            (-17, 3, '2013-09-16', '2012-09-16 11:10:38', 'TRN', 'MOTR', 'IN',  null, null, null, 'BMI',    'LEI',    'N', null, null, null, null, null, null, 3, null, null, null, null, null, null),
            (-17, 4, '2015-07-16', '2015-07-16 13:22:00', 'CRT', 'CRT',  'OUT', null, null, null, 'LEI',    'ABDRCT', 'N', null, null, null, null, null, null, 4, null, null, null, null, null, null),
            (-17, 5, '2016-05-05', '2016-05-05 17:35:28', 'CRT', 'CRT',  'IN',  null, null, null, 'ABDRCT', 'LEI',    'N', null, null, null, null, null, null, 5, null, null, null, null, null, null),
            (-17, 6, '2016-06-05', '2016-06-05 19:00:30', 'TAP', 'C3',   'OUT', null, null, null, 'LEI',    null,     'N', null, null, null, '17917', null, null, 6, null, null, null, null, null, null),
            (-17, 7, '2016-07-15', '2016-07-15 09:00:35', 'TAP', 'C3',   'IN',  null, null, null,  null,    'LEI',    'N', null, null, null, null, '17917', null, 7, null, null, null, null, null, null),
            (-17, 8, '2017-02-07', '2017-02-07 09:00:00', 'TAP', 'C6',   'OUT', null, null, null, 'LEI',    null,     'Y', null, 'Test', null, '29059', null, null, 8, null, null, null, null, null, null);

-- 'Active-In'
INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME, MOVEMENT_TYPE, MOVEMENT_REASON_CODE, DIRECTION_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID, ACTIVE_FLAG)
    VALUES  (-18, 1, '2012-07-05', '2012-07-05 11:37:36', 'ADM', 'N',      'IN',   'OUT', 'LEI', 'Y');

-- 'In-Transit'
INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME, MOVEMENT_TYPE, MOVEMENT_REASON_CODE, DIRECTION_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID, ACTIVE_FLAG)
    VALUES  (-19, 1, '2011-11-07', '2011-11-07 21:13:54', 'ADM', 'I',      'IN',   'OUT', 'LEI', 'N'),
            (-19, 2, '2012-09-16', '2012-09-16 11:09:12', 'TRN', 'NOTR',   'OUT',  'LEI', 'BMI', 'Y');

-- 'Active-UAL'
INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME, MOVEMENT_TYPE, MOVEMENT_REASON_CODE, DIRECTION_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID, ACTIVE_FLAG)
    VALUES  (-20, 1, '2012-07-05', '2012-07-05 11:37:36', 'ADM', 'N',      'IN',   'OUT', 'LEI', 'N'),
            (-20, 2, '2012-07-16', '2012-07-16 15:46:19', 'REL', 'UAL',    'OUT',  'LEI', 'OUT', 'Y');

-- 'UAL_ECL'
INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME, MOVEMENT_TYPE, MOVEMENT_REASON_CODE, DIRECTION_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID, ACTIVE_FLAG)
    VALUES  (-21, 1, '2012-07-05', '2012-07-05 11:37:36', 'ADM', 'N',      'IN',   'OUT', 'LEI', 'N'),
            (-21, 2, '2012-07-16', '2012-07-16 12:44:01', 'REL', 'UAL',    'OUT',  'LEI', 'OUT', 'N'),
            (-21, 3, '2012-08-16', '2012-08-16 17:32:45', 'REL', 'UAL_ECL','OUT',  'LEI', 'OUT', 'Y');

-- 'UAL (Escape)'
INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME, MOVEMENT_TYPE, MOVEMENT_REASON_CODE, DIRECTION_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID, ACTIVE_FLAG)
    VALUES  (-23, 1, '2012-07-05', '2012-07-05 11:37:36', 'ADM', 'N',      'IN',   'OUT', 'LEI', 'N'),
            (-23, 2, '2012-07-16', '2012-07-16 15:46:19', 'REL', 'ESCP',   'OUT',  'LEI', 'OUT', 'Y');

-- 'Active-In' (Recalled)
INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME, MOVEMENT_TYPE, MOVEMENT_REASON_CODE, DIRECTION_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID, ACTIVE_FLAG)
    VALUES  (-24, 1, '2012-07-05', '2012-07-05 11:37:36', 'ADM', 'N',      'IN',   'OUT', 'LEI', 'N'),
            (-24, 2, '2012-07-16', '2012-07-16 15:46:19', 'REL', 'ESCP',   'OUT',  'LEI', 'OUT', 'N'),
            (-24, 3, '2014-07-16', '2012-07-16 15:52:35', 'ADM', 'RECA',   'IN',   'OUT', 'LEI', 'Y');

-- 'Active-Out (CRT)'
INSERT INTO OFFENDER_EXTERNAL_MOVEMENTS (OFFENDER_BOOK_ID, MOVEMENT_SEQ, MOVEMENT_DATE, MOVEMENT_TIME, MOVEMENT_TYPE, MOVEMENT_REASON_CODE, DIRECTION_CODE, ARREST_AGENCY_LOC_ID, TO_PROV_STAT_CODE, ESCORT_CODE, FROM_AGY_LOC_ID, TO_AGY_LOC_ID, ACTIVE_FLAG, ESCORT_TEXT, COMMENT_TEXT, REPORTING_DATE, TO_CITY, FROM_CITY, REPORTING_TIME, EVENT_ID, PARENT_EVENT_ID, TO_COUNTRY_CODE, OJ_LOCATION_CODE, APPLICATION_DATE, TO_ADDRESS_ID, FROM_ADDRESS_ID)
    VALUES  (-25, 1, '2011-11-07', '2011-11-07 21:13:54', 'ADM', 'I', 'IN', null, null, null, 'OUT', 'LEI', 'N', null, null, null, null, null, null, 100, null, null, null, null, null, null),
            (-25, 2, '2012-09-16', '2012-09-16 11:09:12', 'TRN', 'NOTR', 'OUT', null, null, null, 'LEI', 'BMI', 'N', null, null, '2012-09-16', null, null, null, 101, null, null, null, null, null, null),
            (-25, 3, '2012-09-22', '2012-09-22 11:10:38', 'ADM', 'INT', 'IN', null, null, null, 'LEI', 'LEI', 'N', null, null, null, null, null, null, 102, null, null, null, null, null, null),
            (-25, 4, '2015-07-16', '2015-07-16 13:22:00', 'CRT', 'CRT', 'OUT', null, null, null, 'LEI', 'ABDRCT', 'Y', null, null, null, null, null, null, 103, null, null, null, null, null, null);
