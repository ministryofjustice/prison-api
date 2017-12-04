-------------------------------
-- OFFENDER_KEY_DATE_ADJUSTS --
-------------------------------

-- Single 'ADA' record - additional days added should be value of ADJUST_DAYS from this record
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID)
  VALUES (-1, 'ADA', '2017-09-01', 12, null, -1);

-- Single 'UAL' record - additional days added should be zero because record is not an 'ADA' record
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID)
  VALUES (-2, 'UAL', '2017-09-01', 3, null, -3);

-- Multiple records, one 'ADA' and other 'UAL' - additional days added should be value of ADJUST_DAYS from 'ADA' record only
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID)
  VALUES (-3, 'ADA', '2017-09-01', 5, null, -4),
         (-4, 'UAL', '2017-09-01', 13, null, -4);

-- Multiple 'ADA' records - additional days added should be sum of ADJUST_DAYS from all 'ADA' records
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID)
  VALUES (-5, 'ADA', '2017-09-01', 6, null, -5),
         (-6, 'ADA', '2017-09-01', 8, null, -5);

-- Multiple records, some 'ADA' and some not - additional days added should be sum of ADJUST_DAYS from all 'ADA' records only
INSERT INTO OFFENDER_KEY_DATE_ADJUSTS (OFFENDER_KEY_DATE_ADJUST_ID, SENTENCE_ADJUST_CODE, ADJUST_DATE, ADJUST_DAYS, ADJUST_STATUS, OFFENDER_BOOK_ID)
  VALUES (-7, 'ADA', '2017-09-01', 4, null, -6),
         (-8, 'ADA', '2017-09-01', 13, null, -6),
         (-9, 'RX', '2017-09-01', 2, null, -6),
         (-10, 'UAL', '2017-09-01', 7, null, -6);
