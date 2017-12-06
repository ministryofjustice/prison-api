----------------
-- IEP_LEVELS --
----------------

INSERT INTO IEP_LEVELS (IEP_LEVEL, AGY_LOC_ID, ACTIVE_FLAG, DEFAULT_FLAG, REMAND_TRANSFER_LIMIT, REMAND_SPEND_LIMIT, CONVICTED_TRANSFER_LIMIT, CONVICTED_SPEND_LIMIT)
  VALUES ('ENT', 'LEI', 'Y', 'Y', 35.00, 350.00, 15.00, 150.00),
         ('BAS', 'LEI', 'Y', 'N', 20.00, 200.00, 10.00, 100.00),
         ('STD', 'LEI', 'Y', 'N', 50.00, 500.00, 20.00, 200.00),
         ('ENH', 'LEI', 'Y', 'N', 60.00, 600.00, 25.00, 250.00);

-------------------------
-- OFFENDER_IEP_LEVELS --
-------------------------

-- Single detail record for offender - IEP level and date will come from this record
INSERT INTO OFFENDER_IEP_LEVELS (OFFENDER_BOOK_ID, IEP_LEVEL_SEQ, IEP_DATE, IEP_TIME, AGY_LOC_ID, IEP_LEVEL, COMMENT_TEXT, USER_ID)
  VALUES (-1, 1, '2017-08-15', '2017-08-15 16:04:35', 'LEI', 'STD', null, null);

-- Multiple detail records (with different IEP dates) for offender
--  IEP level and date will come from latest record (one with most recent IEP_DATE value)
INSERT INTO OFFENDER_IEP_LEVELS (OFFENDER_BOOK_ID, IEP_LEVEL_SEQ, IEP_DATE, IEP_TIME, AGY_LOC_ID, IEP_LEVEL, COMMENT_TEXT, USER_ID)
  VALUES (-2, 1, '2017-07-04', '2017-07-04 12:15:42', 'LEI', 'ENT', null, null),
         (-2, 2, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'BAS', 'Assaulted another inmate.', 'ITAG_USER');

-- Multiple detail records (most recent with same IEP date) for offender
--  IEP level and date will come from latest record (one with most recent IEP_DATE value and highest IEP_LEVEL_SEQ)
INSERT INTO OFFENDER_IEP_LEVELS (OFFENDER_BOOK_ID, IEP_LEVEL_SEQ, IEP_DATE, IEP_TIME, AGY_LOC_ID, IEP_LEVEL, COMMENT_TEXT, USER_ID)
  VALUES (-3, 1, '2017-07-04', '2017-07-04 12:15:42', 'LEI', 'ENT', null, null),
         (-3, 2, '2017-10-12', '2017-10-12 09:44:01', 'LEI', 'BAS', 'Assaulted another inmate.', 'ITAG_USER'),
         (-3, 3, '2017-10-12', '2017-10-12 07:53:45', 'LEI', 'ENH', 'Did not assault another inmate - data entry error.', 'ITAG_USER'),
         (-3, 4, '2017-08-22', '2017-08-22 18:42:35', 'LEI', 'STD', 'He has been a very good boy.', 'ITAG_USER');

-- Single detail records for other offenders
INSERT INTO OFFENDER_IEP_LEVELS (OFFENDER_BOOK_ID, IEP_LEVEL_SEQ, IEP_DATE, IEP_TIME, AGY_LOC_ID, IEP_LEVEL, COMMENT_TEXT, USER_ID)
  VALUES ( -4, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         ( -5, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         ( -6, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         ( -7, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         ( -8, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-10, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-11, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-12, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-13, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-14, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-15, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-16, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-17, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-18, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-19, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-20, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-21, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-22, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-23, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-24, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER'),
         (-25, 1, '2017-09-06', '2017-09-06 09:44:01', 'LEI', 'STD', null, 'ITAG_USER');
