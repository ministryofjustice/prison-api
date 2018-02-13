CREATE TABLE PROFILE_CODES
(
  "PROFILE_TYPE"                  VARCHAR2(12 CHAR)                       NOT NULL,
  "PROFILE_CODE"                  VARCHAR2(12 CHAR)                       NOT NULL,
  "DESCRIPTION"                   VARCHAR2(40 CHAR),
  "LIST_SEQ"                      NUMBER(6, 0)                            NOT NULL,
  "UPDATE_ALLOWED_FLAG"           VARCHAR2(1 CHAR)  DEFAULT 'Y'           NOT NULL,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR)  DEFAULT 'Y'           NOT NULL,
  "EXPIRY_DATE"                   DATE,
  "USER_ID"                       VARCHAR2(32 CHAR)                       NOT NULL,
  "MODIFIED_DATE"                 DATE                                    NOT NULL,
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp  NOT NULL,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER          NOT NULL,
  "MODIFY_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR),
  CONSTRAINT PROFILE_CODES_PK PRIMARY KEY (PROFILE_TYPE, PROFILE_CODE)
);