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


CREATE TABLE WORKS
(
  "WORK_ID"                       NUMBER(10, 0),
  "WORKFLOW_TYPE"                 VARCHAR2(12 CHAR),
  "WORK_TYPE"                     VARCHAR2(12 CHAR),
  "WORK_SUB_TYPE"                 VARCHAR2(12 CHAR),
  "MANUAL_CLOSE_FLAG"             VARCHAR2(1 CHAR)  DEFAULT 'N',
  "MODULE_NAME"                   VARCHAR2(20 CHAR),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR)  DEFAULT 'Y',
  "EXPIRY_DATE"                   DATE,
  "CASELOAD_TYPE"                 VARCHAR2(12 CHAR),
  "MANUAL_SELECT_FLAG"            VARCHAR2(1 CHAR)  DEFAULT NULL,
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR),
  "EMAIL_SUBJECT"                 VARCHAR2(240 CHAR),
  "EMAIL_BODY"                    VARCHAR2(4000 CHAR)
);
