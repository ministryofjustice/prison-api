CREATE TABLE STAFF_USER_ACCOUNTS
(
  "USERNAME"                      VARCHAR2(30 CHAR)                      NOT NULL,
  "STAFF_ID"                      NUMBER(6, 0)                           NOT NULL,
  "STAFF_USER_TYPE"               VARCHAR2(12 CHAR)                      NOT NULL,
  "ID_SOURCE"                     VARCHAR2(12 CHAR)                      NOT NULL,
  "WORKING_CASELOAD_ID"           VARCHAR2(6 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp NOT NULL,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER         NOT NULL,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR)
);

