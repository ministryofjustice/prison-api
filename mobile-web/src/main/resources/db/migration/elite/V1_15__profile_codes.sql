CREATE TABLE PROFILE_CODES
(
  "PROFILE_TYPE"                  VARCHAR2(12 CHAR),
  "PROFILE_CODE"                  VARCHAR2(12 CHAR),
  "DESCRIPTION"                   VARCHAR2(40 CHAR),
  "LIST_SEQ"                      NUMBER(6, 0),
  "UPDATE_ALLOWED_FLAG"           VARCHAR2(1 CHAR)  DEFAULT 'Y',
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR)  DEFAULT 'Y',
  "EXPIRY_DATE"                   DATE,
  "USER_ID"                       VARCHAR2(32 CHAR),
  "MODIFIED_DATE"                 DATE,
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR)
);