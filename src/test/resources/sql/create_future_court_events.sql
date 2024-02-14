-- Fully populated court event (for booking id -5)
INSERT INTO OFFENDER_CASES (CASE_ID, CASE_INFO_NUMBER, OFFENDER_BOOK_ID, CASE_TYPE, CASE_STATUS, BEGIN_DATE, AGY_LOC_ID,  STATUS_UPDATE_REASON, LIDS_CASE_NUMBER, CASE_SEQ) VALUES (-259951, 'TS9951A', -5, 'A', 'A', TO_DATE('2017-08-31', 'YYYY-MM-DD'), 'LEI', 'A', 1, 2);

INSERT INTO COURT_EVENTS (EVENT_ID,CASE_ID,OFFENDER_BOOK_ID,EVENT_DATE,START_TIME,COURT_EVENT_TYPE,EVENT_STATUS,AGY_LOC_ID,OUTCOME_REASON_CODE,COMMENT_TEXT,EVENT_OUTCOME,NEXT_EVENT_REQUEST_FLAG,ORDER_REQUESTED_FLAG,RESULT_CODE,NEXT_EVENT_DATE,DIRECTION_CODE,HOLD_FLAG) VALUES (-20194,-259951,-5,SYSDATE - 1,SYSDATE - 1,'CRT','COMP','LEI','4531', 'Old Court event', NULL,'N', 'N',NULL,TO_DATE('14-08-2017','DD-MM-YYYY'),'OUT','N');
INSERT INTO COURT_EVENTS (EVENT_ID,CASE_ID,OFFENDER_BOOK_ID,EVENT_DATE,START_TIME,COURT_EVENT_TYPE,EVENT_STATUS,AGY_LOC_ID,OUTCOME_REASON_CODE,COMMENT_TEXT,EVENT_OUTCOME,NEXT_EVENT_REQUEST_FLAG,ORDER_REQUESTED_FLAG,RESULT_CODE,NEXT_EVENT_DATE,DIRECTION_CODE,HOLD_FLAG) VALUES (-20195,-259951,-5,SYSDATE + 1,SYSDATE + 1,'CRT','COMP','LEI','4531', 'Next Court Event!', NULL,'N', 'N',NULL,TO_DATE('14-08-2017','DD-MM-YYYY'),'OUT','N');
INSERT INTO COURT_EVENTS (EVENT_ID,CASE_ID,OFFENDER_BOOK_ID,EVENT_DATE,START_TIME,COURT_EVENT_TYPE,EVENT_STATUS,AGY_LOC_ID,OUTCOME_REASON_CODE,COMMENT_TEXT,EVENT_OUTCOME,NEXT_EVENT_REQUEST_FLAG,ORDER_REQUESTED_FLAG,RESULT_CODE,NEXT_EVENT_DATE,DIRECTION_CODE,HOLD_FLAG) VALUES (-20196,-259951,-5,SYSDATE + 2,SYSDATE + 2,'CRT','COMP','LEI','4531', 'Next Court + 1  Event!', NULL,'N', 'N',NULL,TO_DATE('14-08-2017','DD-MM-YYYY'),'OUT','N');

-- Partially populated court event (for booking id -6)
INSERT INTO COURT_EVENTS (EVENT_ID,CASE_ID,OFFENDER_BOOK_ID,EVENT_DATE,START_TIME,COURT_EVENT_TYPE,EVENT_STATUS,AGY_LOC_ID,OUTCOME_REASON_CODE,COMMENT_TEXT,EVENT_OUTCOME,NEXT_EVENT_REQUEST_FLAG,ORDER_REQUESTED_FLAG,RESULT_CODE,NEXT_EVENT_DATE,DIRECTION_CODE,HOLD_FLAG) VALUES (-30194,NULL,-6,SYSDATE - 1,SYSDATE - 1,'CRT',NULL,'LEI',NULL, 'Old Court event', NULL,'N', 'N',NULL,TO_DATE('14-08-2017','DD-MM-YYYY'),'OUT','N');
INSERT INTO COURT_EVENTS (EVENT_ID,CASE_ID,OFFENDER_BOOK_ID,EVENT_DATE,START_TIME,COURT_EVENT_TYPE,EVENT_STATUS,AGY_LOC_ID,OUTCOME_REASON_CODE,COMMENT_TEXT,EVENT_OUTCOME,NEXT_EVENT_REQUEST_FLAG,ORDER_REQUESTED_FLAG,RESULT_CODE,NEXT_EVENT_DATE,DIRECTION_CODE,HOLD_FLAG) VALUES (-30195,NULL,-6,SYSDATE + 1,SYSDATE + 1,'CRT',NULL,'LEI',NULL, NULL, NULL,'N', 'N',NULL,TO_DATE('14-08-2017','DD-MM-YYYY'),'OUT','N');
INSERT INTO COURT_EVENTS (EVENT_ID,CASE_ID,OFFENDER_BOOK_ID,EVENT_DATE,START_TIME,COURT_EVENT_TYPE,EVENT_STATUS,AGY_LOC_ID,OUTCOME_REASON_CODE,COMMENT_TEXT,EVENT_OUTCOME,NEXT_EVENT_REQUEST_FLAG,ORDER_REQUESTED_FLAG,RESULT_CODE,NEXT_EVENT_DATE,DIRECTION_CODE,HOLD_FLAG) VALUES (-30196,NULL,-6,SYSDATE + 2,SYSDATE + 2,'CRT',NULL,'LEI',NULL, 'Next Court + 1  Event!', NULL,'N', 'N',NULL,TO_DATE('14-08-2017','DD-MM-YYYY'),'OUT','N');