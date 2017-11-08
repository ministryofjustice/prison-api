CREATE TABLE OFFENDER_IDENTIFYING_MARKS
(
  "OFFENDER_BOOK_ID"              NUMBER(10, 0),
  "ID_MARK_SEQ"                   NUMBER(6, 0),
  "BODY_PART_CODE"                VARCHAR2(12 CHAR),
  "MARK_TYPE"                     VARCHAR2(12 CHAR),
  "SIDE_CODE"                     VARCHAR2(12 CHAR),
  "PART_ORIENTATION_CODE"         VARCHAR2(12 CHAR),
  "COMMENT_TEXT"                  VARCHAR2(240 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
);