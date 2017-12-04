CREATE TABLE "REFERENCE_CODES"
(
  "DOMAIN"                        VARCHAR2(12 CHAR)                 NOT NULL ,
  "CODE"                          VARCHAR2(12 CHAR)                 NOT NULL ,
  "DESCRIPTION"                   VARCHAR2(40 CHAR)                 NOT NULL ,
  "LIST_SEQ"                      NUMBER(6, 0),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "SYSTEM_DATA_FLAG"              VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "EXPIRED_DATE"                  DATE,
  "NEW_CODE"                      VARCHAR2(12 CHAR),
  "PARENT_CODE"                   VARCHAR2(12 CHAR),
  "PARENT_DOMAIN"                 VARCHAR2(12 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT user    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "REFERENCE_CODES_PK" PRIMARY KEY ("DOMAIN", "CODE"),
  CONSTRAINT "REF_CODE_REF_DMN_F1" FOREIGN KEY ("DOMAIN")
  REFERENCES "REFERENCE_DOMAINS" ("DOMAIN")
);

COMMENT ON COLUMN "REFERENCE_CODES"."DOMAIN" IS 'The domain of the reference code';
COMMENT ON COLUMN "REFERENCE_CODES"."CODE" IS 'Reference code';
COMMENT ON COLUMN "REFERENCE_CODES"."DESCRIPTION" IS 'Description of the code';
COMMENT ON COLUMN "REFERENCE_CODES"."LIST_SEQ" IS 'Listing order of the code (It for controlling the order of listing in LOV).  If the value is "0", then it is taken as the default for this domain';
COMMENT ON COLUMN "REFERENCE_CODES"."ACTIVE_FLAG" IS 'Is the code active ?';

COMMENT ON COLUMN "REFERENCE_CODES"."SYSTEM_DATA_FLAG" IS 'If the code system data ? (if yes, code value can not be changed) ';

COMMENT ON COLUMN "REFERENCE_CODES"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "REFERENCE_CODES"."EXPIRED_DATE" IS 'The date which the code is no longer used';

COMMENT ON COLUMN "REFERENCE_CODES"."NEW_CODE" IS 'The new code used for replacing this code';

COMMENT ON COLUMN "REFERENCE_CODES"."PARENT_CODE" IS 'Parent Code in hierarchical structure';

COMMENT ON COLUMN "REFERENCE_CODES"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "REFERENCE_CODES"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "REFERENCE_CODES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON TABLE "REFERENCE_CODES" IS 'The reference codes- Retrofitted- Retrofitted';


CREATE UNIQUE INDEX "REFERENCE_CODES_NI1"
  ON "REFERENCE_CODES" ("DOMAIN", "CODE", "DESCRIPTION", "ACTIVE_FLAG");


