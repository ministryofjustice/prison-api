CREATE TABLE PROFILE_TYPES
(
  "PROFILE_TYPE"                  VARCHAR2(12 CHAR),
  "PROFILE_CATEGORY"              VARCHAR2(12 CHAR),
  "DESCRIPTION"                   VARCHAR2(40 CHAR),
  "LIST_SEQ"                      NUMBER(6, 0),
  "MANDATORY_FLAG"                VARCHAR2(1 CHAR)  DEFAULT 'Y',
  "UPDATED_ALLOWED_FLAG"          VARCHAR2(1 CHAR)  DEFAULT 'Y',
  "CODE_VALUE_TYPE"               VARCHAR2(12 CHAR),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR)  DEFAULT 'Y',
  "EXPIRY_DATE"                   DATE,
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "MODIFIED_DATE"                 DATE,
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR)
);
