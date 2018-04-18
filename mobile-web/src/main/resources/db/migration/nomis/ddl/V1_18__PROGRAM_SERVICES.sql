CREATE TABLE "PROGRAM_SERVICES"
(
  "PROGRAM_ID"                    NUMBER(10, 0)                     NOT NULL ,
  "PROGRAM_CATEGORY"              VARCHAR2(12 CHAR),
  "PROGRAM_CODE"                  VARCHAR2(40 CHAR),
  "DESCRIPTION"                   VARCHAR2(40 CHAR)                 NOT NULL ,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR)                  NOT NULL ,
  "EXPIRY_DATE"                   DATE,
  "COMMENT_TEXT"                  VARCHAR2(240 CHAR),
  "PROGRAM_CLASS"                 VARCHAR2(12 CHAR)                 NOT NULL ,
  "PARENT_PROGRAM_ID"             NUMBER(10, 0),
  "CONTACT_METHOD"                VARCHAR2(80 CHAR),
  "NO_OF_SESSIONS"                NUMBER(6, 0),
  "NO_OF_ALLOWABLE_RESTARTS"      NUMBER(6, 0),
  "NO_OF_ALLOWABLE_ABSENCES"      NUMBER(6, 0),
  "CAPACITY"                      NUMBER(6, 0),
  "SESSION_LENGTH"                NUMBER(6, 0),
  "COMPLETION_FLAG"               VARCHAR2(1 CHAR) DEFAULT 'N',
  "MODULE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'N',
  "MODULE_TYPE"                   VARCHAR2(12 CHAR),
  "BREAK_ALLOWED_FLAG"            VARCHAR2(1 CHAR) DEFAULT 'N',
  "START_FLAG"                    VARCHAR2(1 CHAR) DEFAULT 'N',
  "NO_OF_WEEKLY_SESSIONS"         NUMBER(6, 0),
  "PROGRAM_STATUS"                VARCHAR2(12 CHAR),
  "LIST_SEQ"                      NUMBER(6, 0),
  "PHASE_TYPE"                    VARCHAR2(12 CHAR),
  "START_DATE"                    DATE,
  "END_DATE"                      DATE,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "FUNCTION_TYPE"                 VARCHAR2(12 CHAR),
  CONSTRAINT "PROGRAM_SERVICES_CHK1" CHECK (program_class IN ('PRG', 'PRG_MOD', 'PRG_PH', 'PRG_BLK') ) ,
  CONSTRAINT "PROGRAM_SERVICES_PK" PRIMARY KEY ("PROGRAM_ID"),
  CHECK ( active_flag IN ('N', 'Y')  ) ,
  CONSTRAINT "PRG_SERV_PRG_SERV_FK" FOREIGN KEY ("PARENT_PROGRAM_ID")
  REFERENCES "PROGRAM_SERVICES" ("PROGRAM_ID")
);


COMMENT ON COLUMN "PROGRAM_SERVICES"."PROGRAM_ID" IS 'Unique ID across the table';

COMMENT ON COLUMN "PROGRAM_SERVICES"."PROGRAM_CATEGORY" IS 'Program category derived from reference code "PS_CATEGORY"';

COMMENT ON COLUMN "PROGRAM_SERVICES"."PROGRAM_CODE" IS 'User enterable free-form text';

COMMENT ON COLUMN "PROGRAM_SERVICES"."DESCRIPTION" IS 'Program/Service Description';

COMMENT ON COLUMN "PROGRAM_SERVICES"."ACTIVE_FLAG" IS 'Is the Program/Service Active';

COMMENT ON COLUMN "PROGRAM_SERVICES"."EXPIRY_DATE" IS 'Set to date on which the P/S becomes inactive';

COMMENT ON COLUMN "PROGRAM_SERVICES"."COMMENT_TEXT" IS 'The general comment text';

COMMENT ON COLUMN "PROGRAM_SERVICES"."PROGRAM_CLASS" IS 'Reference Code(PRG_CLS), such as services, programs, program phases, prgram module';

COMMENT ON COLUMN "PROGRAM_SERVICES"."PARENT_PROGRAM_ID" IS 'The parent of the program class';

COMMENT ON COLUMN "PROGRAM_SERVICES"."CONTACT_METHOD" IS 'Method of contact in free text';

COMMENT ON COLUMN "PROGRAM_SERVICES"."NO_OF_SESSIONS" IS 'Number of weekly sessions';

COMMENT ON COLUMN "PROGRAM_SERVICES"."NO_OF_ALLOWABLE_RESTARTS" IS 'Number of allowable restart for the program';

COMMENT ON COLUMN "PROGRAM_SERVICES"."NO_OF_ALLOWABLE_ABSENCES" IS 'Number of allowable absence for the program';

COMMENT ON COLUMN "PROGRAM_SERVICES"."CAPACITY" IS 'The capacity';

COMMENT ON COLUMN "PROGRAM_SERVICES"."SESSION_LENGTH" IS 'The session length in hours';

COMMENT ON COLUMN "PROGRAM_SERVICES"."COMPLETION_FLAG" IS 'The phase which decide when the services is considered as completed';

COMMENT ON COLUMN "PROGRAM_SERVICES"."MODULE_FLAG" IS 'If there are module for the program phase';

COMMENT ON COLUMN "PROGRAM_SERVICES"."MODULE_TYPE" IS 'The module type of the program phase. Reference Code(PS_MOD_TYPE)';

COMMENT ON COLUMN "PROGRAM_SERVICES"."BREAK_ALLOWED_FLAG" IS '?Allow break';

COMMENT ON COLUMN "PROGRAM_SERVICES"."START_FLAG" IS '?Can start at this module';

COMMENT ON COLUMN "PROGRAM_SERVICES"."NO_OF_WEEKLY_SESSIONS" IS 'No of session per week';

COMMENT ON COLUMN "PROGRAM_SERVICES"."PROGRAM_STATUS" IS 'Reference Code(PS_STATUS)';

COMMENT ON COLUMN "PROGRAM_SERVICES"."LIST_SEQ" IS 'The listing order';

COMMENT ON COLUMN "PROGRAM_SERVICES"."PHASE_TYPE" IS 'The phase type.  Reference Code(PS_PHS_TYPE)';

COMMENT ON COLUMN "PROGRAM_SERVICES"."START_DATE" IS 'The start date';

COMMENT ON COLUMN "PROGRAM_SERVICES"."END_DATE" IS 'The End date';

COMMENT ON COLUMN "PROGRAM_SERVICES"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "PROGRAM_SERVICES"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "PROGRAM_SERVICES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "PROGRAM_SERVICES"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "PROGRAM_SERVICES" IS 'A structured activity having the purpose of work, education, punishment or rehabilitation of offenders serving both custodial and non-custodial sentences. This entity holds the activity services of different categories and their respective structures, as part of the Service Catelogue.';


CREATE INDEX "PROGRAM_SERVICES_NI1"
  ON "PROGRAM_SERVICES" ("PROGRAM_CATEGORY");


CREATE INDEX "PROGRAM_SERVICES_NI2"
  ON "PROGRAM_SERVICES" ("PARENT_PROGRAM_ID");


CREATE UNIQUE INDEX "PROGRAM_SERVICES_UK1"
  ON "PROGRAM_SERVICES" ("PROGRAM_CODE");


CREATE INDEX "PROGRAM_SERVICES_X01"
  ON "PROGRAM_SERVICES" ("PROGRAM_CATEGORY", "PROGRAM_CLASS");


