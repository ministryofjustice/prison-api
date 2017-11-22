CREATE TABLE "ASSESSMENTS"
(
  "ASSESSMENT_ID"                 NUMBER(10, 0)                     NOT NULL ENABLE,
  "ASSESSMENT_CLASS"              VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "PARENT_ASSESSMENT_ID"          NUMBER(10, 0),
  "ASSESSMENT_CODE"               VARCHAR2(20 CHAR)                 NOT NULL ENABLE,
  "DESCRIPTION"                   VARCHAR2(300 CHAR)                NOT NULL ENABLE,
  "LIST_SEQ"                      NUMBER(6, 0),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ENABLE,
  "UPDATE_ALLOWED_FLAG"           VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ENABLE,
  "EFFECTIVE_DATE"                DATE,
  "EXPIRY_DATE"                   DATE,
  "SCORE"                         NUMBER(8, 2),
  "MUTUAL_EXCLUSIVE_FLAG"         VARCHAR2(1 CHAR) DEFAULT 'N',
  "DETERMINE_SUP_LEVEL_FLAG"      VARCHAR2(1 CHAR) DEFAULT 'N',
  "REQUIRE_EVALUATION_FLAG"       VARCHAR2(1 CHAR) DEFAULT 'N',
  "REQUIRE_APPROVAL_FLAG"         VARCHAR2(1 CHAR) DEFAULT 'N',
  "REVIEW_CYCLE_DAYS"             NUMBER(3, 0),
  "CASELOAD_TYPE"                 VARCHAR2(12 CHAR),
  "REVIEW_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'N',
  "ASSESS_COMMENT"                VARCHAR2(240 CHAR),
  "HIGH_VALUE"                    NUMBER(5, 2),
  "LOW_VALUE"                     NUMBER(5, 2),
  "SEARCH_CRITERIA_FLAG"          VARCHAR2(1 CHAR) DEFAULT 'N',
  "OVERRIDEABLE_FLAG"             VARCHAR2(1 CHAR) DEFAULT 'N',
  "ASSESSMENT_TYPE"               VARCHAR2(12 CHAR),
  "CALCULATE_TOTAL_FLAG"          VARCHAR2(1 CHAR) DEFAULT 'N',
  "MEASURE"                       VARCHAR2(12 CHAR),
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ENABLE,
  "CREATE_DATE"                   DATE,
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ENABLE,
  "CELL_SHARING_ALERT_FLAG"       VARCHAR2(1 CHAR) DEFAULT 'N',
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "TOTAL_PERCENT"                 NUMBER(6, 2),
  "REVERSE_SCORE"                 NUMBER(8, 2),
  "SCREEN_CODE"                   VARCHAR2(40 CHAR),
  CONSTRAINT "ASSESSMENTS_PK" PRIMARY KEY ("ASSESSMENT_ID") ENABLE,
  CONSTRAINT "ASSESS_ASSESS_F1" FOREIGN KEY ("PARENT_ASSESSMENT_ID")
  REFERENCES "ASSESSMENTS" ("ASSESSMENT_ID") ENABLE
);


COMMENT ON COLUMN "ASSESSMENTS"."ASSESSMENT_ID" IS 'System generated identifier for an assessment.';

COMMENT ON COLUMN "ASSESSMENTS"."ASSESSMENT_CLASS" IS 'Reference Code [ ASSESS_CLS ] : Assessment Class Such as (Category, Sections ...)';

COMMENT ON COLUMN "ASSESSMENTS"."PARENT_ASSESSMENT_ID" IS ' Pointer back to parent in hierarchy - ie. Type, Section,Category,Indicator';

COMMENT ON COLUMN "ASSESSMENTS"."ASSESSMENT_CODE" IS 'Assessment Code for different parts of assessment ie.  Type, Section, Category, Indicator.';

COMMENT ON COLUMN "ASSESSMENTS"."DESCRIPTION" IS 'Description of assessment code.';

COMMENT ON COLUMN "ASSESSMENTS"."LIST_SEQ" IS 'Listing Order ( 0 imply Default Value )';

COMMENT ON COLUMN "ASSESSMENTS"."ACTIVE_FLAG" IS 'If the code active ?';

COMMENT ON COLUMN "ASSESSMENTS"."UPDATE_ALLOWED_FLAG" IS 'If the code allowed to changed ( It is for controlling the code)';

COMMENT ON COLUMN "ASSESSMENTS"."EFFECTIVE_DATE" IS 'Effective date for assessment type.';

COMMENT ON COLUMN "ASSESSMENTS"."EXPIRY_DATE" IS 'Deactivation date for a code.';

COMMENT ON COLUMN "ASSESSMENTS"."SCORE" IS 'This calculated score will determine the offender"s security level.';

COMMENT ON COLUMN "ASSESSMENTS"."MUTUAL_EXCLUSIVE_FLAG" IS ' Mutually exclusive flag.';

COMMENT ON COLUMN "ASSESSMENTS"."DETERMINE_SUP_LEVEL_FLAG" IS 'The assessment type determines offender security level.';

COMMENT ON COLUMN "ASSESSMENTS"."REQUIRE_EVALUATION_FLAG" IS 'Flag indicating evaluation is required.';

COMMENT ON COLUMN "ASSESSMENTS"."REQUIRE_APPROVAL_FLAG" IS 'Approval required before recommended security level can be recorded.';

COMMENT ON COLUMN "ASSESSMENTS"."REVIEW_CYCLE_DAYS" IS 'Indicates number of days until next scheduled review.';

COMMENT ON COLUMN "ASSESSMENTS"."CASELOAD_TYPE" IS 'The Case Load Type';

COMMENT ON COLUMN "ASSESSMENTS"."ASSESSMENT_TYPE" IS 'Type - question, exculusive, inclusive or rank type - Reference Code[ASSESS_TYPE]';

COMMENT ON COLUMN "ASSESSMENTS"."CALCULATE_TOTAL_FLAG" IS 'Drives the display of columns in Assessments Screen.';

COMMENT ON COLUMN "ASSESSMENTS"."MEASURE" IS 'Populates with measure code if the question is Rank Type';

COMMENT ON COLUMN "ASSESSMENTS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "ASSESSMENTS"."CREATE_DATE" IS 'Populates with sysdate while creating a new record';

COMMENT ON COLUMN "ASSESSMENTS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "ASSESSMENTS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "ASSESSMENTS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON TABLE "ASSESSMENTS" IS 'A component or classification of a structured interview or check list which is designed to identify certain characteristics of an offender so as to provide the basis for decisions made about the offender during his/her period of supervision.';


CREATE INDEX "ASSESSMENTS_NI1"
  ON "ASSESSMENTS" ("PARENT_ASSESSMENT_ID");


CREATE INDEX "ASSESSMENTS_NI2"
  ON "ASSESSMENTS" ("DESCRIPTION");


CREATE INDEX "ASSESSMENTS_NI3"
  ON "ASSESSMENTS" ("ASSESSMENT_CLASS", "ASSESSMENT_CODE");


