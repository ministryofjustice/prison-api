-----------------------------
-- OFFENDER_SENTENCE_TERMS --
-----------------------------

-- Single 'IMP' record
INSERT INTO OFFENDER_SENTENCE_TERMS (OFFENDER_BOOK_ID, SENTENCE_SEQ, TERM_SEQ, SENTENCE_TERM_CODE, YEARS, MONTHS, START_DATE, END_DATE, LIFE_SENTENCE_FLAG)
  VALUES (-1, 1, 1, 'IMP', 3, null, '2017-03-25', '2020-03-24', 'N');

-- Multiple 'IMP' records - earliest start date should be used
INSERT INTO OFFENDER_SENTENCE_TERMS (OFFENDER_BOOK_ID, SENTENCE_SEQ, TERM_SEQ, SENTENCE_TERM_CODE, YEARS, MONTHS, START_DATE, END_DATE, LIFE_SENTENCE_FLAG)
  VALUES (-2, 1, 1, 'IMP', null, 6, '2016-11-22', '2017-05-21', 'N'),
         (-2, 2, 1, 'IMP', 2, null, '2017-05-22', '2019-05-21', 'N');

-- Multiple records with different SENTENCE_TERM_CODE values - start date should come from record having 'IMP' sentence term code
INSERT INTO OFFENDER_SENTENCE_TERMS (OFFENDER_BOOK_ID, SENTENCE_SEQ, TERM_SEQ, SENTENCE_TERM_CODE, YEARS, MONTHS, START_DATE, END_DATE, LIFE_SENTENCE_FLAG)
  VALUES (-3, 1, 1, 'LIC', null, 2, '2016-11-05', '2017-01-04', 'N'),
         (-3, 2, 1, 'IMP', 5, null, '2015-03-16', '2020-03-15', 'N');

-- Single 'IMP' record
INSERT INTO OFFENDER_SENTENCE_TERMS (OFFENDER_BOOK_ID, SENTENCE_SEQ, TERM_SEQ, SENTENCE_TERM_CODE, YEARS, MONTHS, START_DATE, END_DATE, LIFE_SENTENCE_FLAG)
  VALUES (-4, 1, 1, 'IMP', 15, null, '2007-10-16', '2022-10-15', 'N'),
         (-5, 1, 1, 'IMP', 6, 6, '2017-02-08', '2023-08-07', 'N'),
         (-6, 1, 1, 'IMP', null, 9, '2017-09-01', '2018-05-31', 'N'),
         (-7, 1, 1, 'IMP', null, 9, '2017-09-01', '2018-05-31', 'N'),
         (-8, 1, 1, 'IMP', null, 9, '2017-09-01', '2018-05-31', 'N'),
         (-9, 1, 1, 'IMP', null, 9, '2017-09-01', '2018-05-31', 'N'),
         (-10, 1, 1, 'IMP', null, 9, '2017-09-01', '2018-05-31', 'N'),
         (-11, 1, 1, 'IMP', null, 9, '2017-09-01', '2018-05-31', 'N'),
         (-12, 1, 1, 'IMP', null, 9, '2017-09-01', '2018-05-31', 'N'),
         (-13, 1, 1, 'IMP', 6, 6, '2017-02-08', '2023-08-07', 'N'),
         (-14, 1, 1, 'IMP', 15, null, '2007-10-16', '2022-10-15', 'N'),
         (-17, 1, 1, 'IMP', 10, null, '2015-05-05', '2025-05-14', 'N'),
         (-18, 1, 1, 'IMP', 4, 9, '2016-11-17', '2021-08-16', 'N'),
         (-24, 1, 1, 'IMP', 7, 0, '2017-07-07', '2024-07-06', 'N'),
         (-25, 1, 1, 'IMP', 20, 0, '2009-09-09', '2029-09-08', 'N'),
         (-27, 1, 1, 'IMP', 25, 0, '2014-09-09', '2039-09-08', 'Y'),
         (-28, 1, 1, 'IMP', 25, 0, '2014-09-09', '2039-09-08', 'Y');
