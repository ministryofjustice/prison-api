-------------------------------
-- OFFENDER_KEY_DATE_ADJUSTS --
-------------------------------

-- Single, active 'ADA' record - additional days added should be value of ADJUST_DAYS from this record
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID, ACTIVE_FLAG)
  VALUES (-1, 'ADA', '2017-09-01', 12, null, -1, 'Y');

-- Single, inactive 'ADA' record - additional days added should be zero because record is not active
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID, ACTIVE_FLAG)
  VALUES (-2, 'ADA', '2017-09-01', 15, null, -2, 'N');

-- Single, active 'UAL' record - additional days added should be zero because record is not an 'ADA' record
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID, ACTIVE_FLAG)
  VALUES (-3, 'UAL', '2017-09-01', 3, null, -3, 'Y');

-- Multiple, active records, one 'ADA' and other 'UAL' - additional days added should be value of ADJUST_DAYS from 'ADA' record only
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID, ACTIVE_FLAG)
  VALUES (-4, 'ADA', '2017-09-01', 5, null, -4, 'Y'),
         (-5, 'UAL', '2017-09-01', 13, null, -4, 'Y');

-- Multiple, active 'ADA' records - additional days added should be sum of ADJUST_DAYS from all active 'ADA' records
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID, ACTIVE_FLAG)
  VALUES (-6, 'ADA', '2017-09-01', 6, null, -5, 'Y'),
         (-7, 'ADA', '2017-09-01', 8, null, -5, 'Y');

-- Multiple records, some active and some inactive, some 'ADA' and some not - additional days added should be sum of ADJUST_DAYS from all active 'ADA' records
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID, ACTIVE_FLAG)
  VALUES (-8, 'ADA', '2017-09-01', 4, null, -6, 'Y'),
         (-9, 'ADA', '2017-09-01', 9, null, -6, 'N'),
         (-10, 'ADA', '2017-09-01', 13, null, -6, 'Y'),
         (-11, 'UAL', '2017-09-01', 1, null, -6, 'N'),
         (-12, 'RX', '2017-09-01', 2, null, -6, 'Y'),
         (-13, 'UAL', '2017-09-01', 7, null, -6, 'Y');
