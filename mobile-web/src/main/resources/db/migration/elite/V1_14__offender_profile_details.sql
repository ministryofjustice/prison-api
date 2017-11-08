CREATE TABLE OFFENDER_PROFILE_DETAILS
(
  "OFFENDER_BOOK_ID"              NUMBER(10, 0),
  "PROFILE_SEQ"                   NUMBER(6, 0),
  "PROFILE_TYPE"                  VARCHAR2(12 CHAR),
  "PROFILE_CODE"                  VARCHAR2(40 CHAR),
  "LIST_SEQ"                      NUMBER(6, 0),
  "COMMENT_TEXT"                  VARCHAR2(240 CHAR),
  "CASELOAD_TYPE"                 VARCHAR2(12 CHAR),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER,
  "SEAL_FLAG"                     VARCHAR2(1 CHAR)
);
