CREATE TABLE "DB_PATCHES"
(
  "PROFILE_CODE"                  VARCHAR2(12)                      NOT NULL ENABLE,
  "PROFILE_VALUE"                 VARCHAR2(40)                      NOT NULL ENABLE,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ENABLE,
  "CREATE_USER_ID"                VARCHAR2(32) DEFAULT USER         NOT NULL ENABLE,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32),
  "AUDIT_MODULE_NAME"             VARCHAR2(65),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256),
  CONSTRAINT "DB_PATCHES_PK" PRIMARY KEY ("PROFILE_CODE", "PROFILE_VALUE")
);