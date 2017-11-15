CREATE TABLE "OFFENDER_CASES"
(
  "CASE_ID"                       NUMBER(10, 0)                     NOT NULL ENABLE,
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ENABLE,
  "CASE_INFO_NUMBER"              VARCHAR2(13 CHAR),
  "CASE_TYPE"                     VARCHAR2(12 CHAR),
  "CASE_STATUS"                   VARCHAR2(12 CHAR),
  "COMBINED_CASE_ID"              NUMBER(10, 0),
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "BEGIN_DATE"                    DATE,
  "AGY_LOC_ID"                    VARCHAR2(6 CHAR)                  NOT NULL ENABLE,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ENABLE,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ENABLE,
  "CASE_INFO_PREFIX"              VARCHAR2(12 CHAR),
  "VICTIM_LIAISON_UNIT"           VARCHAR2(12 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "STATUS_UPDATE_REASON"          VARCHAR2(12 CHAR),
  "STATUS_UPDATE_COMMENT"         VARCHAR2(400 CHAR),
  "STATUS_UPDATE_DATE"            DATE,
  "STATUS_UPDATE_STAFF_ID"        NUMBER(10, 0),
  "LIDS_CASE_NUMBER"              NUMBER(10, 0),
  "NOMLEGALCASEREF"               NUMBER(10, 0),
  "NOMLEGALCASEREFTRANSTO"        NUMBER(10, 0),
  "CASE_SEQ"                      NUMBER(6, 0)                      NOT NULL ENABLE,
  CONSTRAINT "OFFENDER_CASES_PK" PRIMARY KEY ("CASE_ID"),
  CONSTRAINT "OFF_CS_OFF_CS_F1" FOREIGN KEY ("COMBINED_CASE_ID")
  REFERENCES "OFFENDER_CASES" ("CASE_ID") ENABLE,
  CONSTRAINT "OFF_CS_OFF_BKG_FK" FOREIGN KEY ("OFFENDER_BOOK_ID")
  REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID") ENABLE
);


COMMENT ON COLUMN "OFFENDER_CASES"."CASE_ID" IS 'The Case ID';

COMMENT ON COLUMN "OFFENDER_CASES"."OFFENDER_BOOK_ID" IS 'The Ofender Booking ID';

COMMENT ON COLUMN "OFFENDER_CASES"."CASE_INFO_NUMBER" IS 'The case Info number';

COMMENT ON COLUMN "OFFENDER_CASES"."CASE_TYPE" IS 'The case type.  Reference Codes(LEG_CASE_TYP)';

COMMENT ON COLUMN "OFFENDER_CASES"."CASE_STATUS" IS 'The status of the case';

COMMENT ON COLUMN "OFFENDER_CASES"."COMBINED_CASE_ID" IS 'The combined case ID';

COMMENT ON COLUMN "OFFENDER_CASES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "OFFENDER_CASES"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "OFFENDER_CASES"."BEGIN_DATE" IS 'The Begin date';

COMMENT ON COLUMN "OFFENDER_CASES"."AGY_LOC_ID" IS 'The court agency location ID';

COMMENT ON COLUMN "OFFENDER_CASES"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "OFFENDER_CASES"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "OFFENDER_CASES"."CASE_INFO_PREFIX" IS 'The Prefix of the case number';

COMMENT ON COLUMN "OFFENDER_CASES"."VICTIM_LIAISON_UNIT" IS 'The liaison unit in charge of the case';

COMMENT ON COLUMN "OFFENDER_CASES"."STATUS_UPDATE_REASON" IS 'The reason of status updated';

COMMENT ON COLUMN "OFFENDER_CASES"."STATUS_UPDATE_COMMENT" IS 'The comment of status updated';

COMMENT ON COLUMN "OFFENDER_CASES"."STATUS_UPDATE_DATE" IS 'The date of status updated';

COMMENT ON COLUMN "OFFENDER_CASES"."STATUS_UPDATE_STAFF_ID" IS 'The staff who perform of status updated';

COMMENT ON COLUMN "OFFENDER_CASES"."NOMLEGALCASEREF" IS 'LIDS generated Unique Identifier-Case Id';

COMMENT ON COLUMN "OFFENDER_CASES"."NOMLEGALCASEREFTRANSTO" IS 'LIDS generated value of combined case id';

COMMENT ON COLUMN "OFFENDER_CASES"."CASE_SEQ" IS 'The case seq no for the offender';

COMMENT ON TABLE "OFFENDER_CASES" IS 'The involvement of an offender in a court case. This is a way of grouping related orders for one set of offenses into one criminal event called a case. All legal events that follow from one set of charges are linked with a parent case.';


CREATE INDEX "OFFENDER_CASES_NI2"
  ON "OFFENDER_CASES" ("CASE_INFO_NUMBER");


CREATE INDEX "OFFENDER_CASES_NI3"
  ON "OFFENDER_CASES" ("AGY_LOC_ID");


CREATE INDEX "OFFENDER_CASES_NI4"
  ON "OFFENDER_CASES" ("COMBINED_CASE_ID");


CREATE UNIQUE INDEX "OFFENDER_CASES_UK"
  ON "OFFENDER_CASES" ("OFFENDER_BOOK_ID", "CASE_SEQ");

