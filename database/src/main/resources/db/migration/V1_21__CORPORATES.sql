CREATE TABLE "CORPORATES"
(
  "CORPORATE_ID"                  NUMBER(10, 0)                     NOT NULL ENABLE,
  "CORPORATE_NAME"                VARCHAR2(40 CHAR),
  "CASELOAD_ID"                   VARCHAR2(6 CHAR),
  "CONTACT_PERSON_NAME"           VARCHAR2(40 CHAR),
  "CREATED_DATE"                  DATE                              NOT NULL ENABLE,
  "UPDATED_DATE"                  DATE,
  "USER_ID"                       VARCHAR2(32 CHAR),
  "COMMENT_TEXT"                  VARCHAR2(240 CHAR),
  "START_DATE"                    DATE,
  "ACCOUNT_TERM_CODE"             VARCHAR2(60 CHAR),
  "SHIPPING_TERM_CODE"            VARCHAR2(60 CHAR),
  "MINIMUM_PURCHASE_AMOUNT"       NUMBER(9, 2),
  "MAXIMUM_PURCHASE_AMOUNT"       NUMBER(9, 2),
  "MEMO_TEXT"                     VARCHAR2(40 CHAR),
  "SUSPENDED_FLAG"                VARCHAR2(1 CHAR)                  NOT NULL ENABLE,
  "SUSPENDED_DATE"                DATE,
  "FEI_NUMBER"                    VARCHAR2(40 CHAR),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ENABLE,
  "EXPIRY_DATE"                   DATE,
  "TAX_NO"                        VARCHAR2(12 CHAR),
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
  CONSTRAINT "CORPORATES_PK" PRIMARY KEY ("CORPORATE_ID") ENABLE
);

COMMENT ON COLUMN "CORPORATES"."CORPORATE_ID" IS ' - Column already exists';
COMMENT ON COLUMN "CORPORATES"."CORPORATE_NAME" IS ' - Column already exists';
COMMENT ON COLUMN "CORPORATES"."CASELOAD_ID" IS ' - Column already exists';
COMMENT ON COLUMN "CORPORATES"."CONTACT_PERSON_NAME" IS ' - Column already exists';
COMMENT ON COLUMN "CORPORATES"."CREATED_DATE" IS ' - Column already exists';
COMMENT ON COLUMN "CORPORATES"."UPDATED_DATE" IS ' - Column already exists';
COMMENT ON COLUMN "CORPORATES"."USER_ID" IS ' - Column already exists';
COMMENT ON COLUMN "CORPORATES"."COMMENT_TEXT" IS ' - Column already exists';
COMMENT ON COLUMN "CORPORATES"."ACTIVE_FLAG" IS 'Active data indicator';
COMMENT ON COLUMN "CORPORATES"."EXPIRY_DATE" IS 'Expiry date for the data';
COMMENT ON COLUMN "CORPORATES"."CREATE_DATETIME" IS 'The timestamp when the record is created';
COMMENT ON COLUMN "CORPORATES"."CREATE_USER_ID" IS 'The user who creates the record';
COMMENT ON COLUMN "CORPORATES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';
COMMENT ON COLUMN "CORPORATES"."MODIFY_USER_ID" IS 'The user who modifies the record';
COMMENT ON TABLE "CORPORATES" IS 'An organisation not managed or owned by the National Offender Management Service - typically, these will be Program Providers. NOTE: There are two clear exceptional cases to the aforementioned definition providing workarounds for TAG system functional gaps: - 1. Corporate instances representing Administrative Caseloads can be created and subsequently assigned as Corporate Beneficiaries to enable repayments of Prisoner Advances (Prisoner Finances - Deductions); 2. Corporate instances representing Institutional Receptions can be created and subsequently utilised as a target for trust account funds transfers (e.g. when an offender is relocated).';

CREATE INDEX "CORPORATES_NI1" ON "CORPORATES" ("CASELOAD_ID");
CREATE INDEX "CORPORATES_NI2" ON "CORPORATES" ("CORPORATE_NAME");
