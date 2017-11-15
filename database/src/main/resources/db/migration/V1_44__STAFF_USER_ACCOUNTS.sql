CREATE TABLE "STAFF_USER_ACCOUNTS"
(
  "USERNAME"                      VARCHAR2(30 CHAR)                 NOT NULL ENABLE,
  "STAFF_ID"                      NUMBER(10, 0)                     NOT NULL ENABLE,
  "STAFF_USER_TYPE"               VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "ID_SOURCE"                     VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "WORKING_CASELOAD_ID"           VARCHAR2(6 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ENABLE,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ENABLE,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "STAFF_USER_ACCOUNT_PK" PRIMARY KEY ("USERNAME"),
  CONSTRAINT "STAFF_USER_ACCOUNT_UK1" UNIQUE ("STAFF_ID", "STAFF_USER_TYPE"),
  CONSTRAINT "STAFF_USER_ACCOUNTS_FK1" FOREIGN KEY ("STAFF_ID")
  REFERENCES "STAFF_MEMBERS" ("STAFF_ID") ENABLE
);


CREATE INDEX "STAFF_USER_ACCOUNTS_FK1"
  ON "STAFF_USER_ACCOUNTS" ("STAFF_ID");


