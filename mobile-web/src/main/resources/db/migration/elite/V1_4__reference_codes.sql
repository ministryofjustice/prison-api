CREATE TABLE REFERENCE_CODES
(
  "DOMAIN"                        VARCHAR2(12 CHAR),
  "CODE"                          VARCHAR2(12 CHAR),
  "DESCRIPTION"                   VARCHAR2(40 CHAR),
  "LIST_SEQ"                      NUMBER(6, 0),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR)  DEFAULT 'Y',
  "SYSTEM_DATA_FLAG"              VARCHAR2(1 CHAR)  DEFAULT 'Y',
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "EXPIRED_DATE"                  DATE,
  "NEW_CODE"                      VARCHAR2(12 CHAR),
  "PARENT_CODE"                   VARCHAR2(12 CHAR),
  "PARENT_DOMAIN"                 VARCHAR2(12 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT user,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR)
);
