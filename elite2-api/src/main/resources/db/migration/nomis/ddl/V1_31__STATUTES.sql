CREATE TABLE "STATUTES"
(
  "STATUTE_CODE"                  VARCHAR2(12 CHAR)                 NOT NULL ,
  "DESCRIPTION"                   VARCHAR2(240 CHAR)                NOT NULL ,
  "LEGISLATING_BODY_CODE"         VARCHAR2(12 CHAR)                 NOT NULL ,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "LIST_SEQ"                      NUMBER(6, 0),
  "UPDATE_ALLOWED_FLAG"           VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "EXPIRY_DATE"                   DATE,
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "STATUTES_PK" PRIMARY KEY ("STATUTE_CODE")
) ;


COMMENT ON COLUMN "STATUTES"."STATUTE_CODE" IS 'The statute code';

COMMENT ON COLUMN "STATUTES"."DESCRIPTION" IS 'The description of the statute';

COMMENT ON COLUMN "STATUTES"."LEGISLATING_BODY_CODE" IS 'Reference Code ( LEGISL BODY )';

COMMENT ON COLUMN "STATUTES"."ACTIVE_FLAG" IS '?If the statute active';

COMMENT ON COLUMN "STATUTES"."LIST_SEQ" IS 'Listing order';

COMMENT ON COLUMN "STATUTES"."UPDATE_ALLOWED_FLAG" IS '?If update operation allowed to this record';

COMMENT ON COLUMN "STATUTES"."EXPIRY_DATE" IS 'The expiry date of the statute';

COMMENT ON COLUMN "STATUTES"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "STATUTES"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "STATUTES"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "STATUTES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON TABLE "STATUTES" IS 'The statute of law';


