CREATE TABLE "TRUST_AUDITS_TMP"
(
  "MODULE_NAME"                   VARCHAR2(20)                      NOT NULL ,
  "SID"                           NUMBER                            NOT NULL ,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32) DEFAULT USER         NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32),
  "AUDIT_MODULE_NAME"             VARCHAR2(65),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256)
);