CREATE TABLE "WORKS"
(
  "WORK_ID"                       NUMBER(10, 0)                     NOT NULL ENABLE,
  "WORKFLOW_TYPE"                 VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "WORK_TYPE"                     VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "WORK_SUB_TYPE"                 VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "MANUAL_CLOSE_FLAG"             VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ENABLE,
  "MODULE_NAME"                   VARCHAR2(20 CHAR),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ENABLE,
  "EXPIRY_DATE"                   DATE,
  "CASELOAD_TYPE"                 VARCHAR2(12 CHAR),
  "MANUAL_SELECT_FLAG"            VARCHAR2(1 CHAR) DEFAULT NULL,
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
  CONSTRAINT "WORKS_UK" UNIQUE ("WORKFLOW_TYPE", "WORK_TYPE", "WORK_SUB_TYPE", "CASELOAD_TYPE"),
  CONSTRAINT "WORKS_PK" PRIMARY KEY ("WORK_ID")
);


COMMENT ON COLUMN "WORKS"."WORK_ID" IS 'PK of the work';

COMMENT ON COLUMN "WORKS"."WORKFLOW_TYPE" IS 'Type of workflow job. ALERT or TASK or CASENOTE. Reference Code (ALERT_TASK)';

COMMENT ON COLUMN "WORKS"."WORK_TYPE" IS 'Type of the work. Reference Code(TASK_TYPE)';

COMMENT ON COLUMN "WORKS"."WORK_SUB_TYPE" IS 'Subtype of the work. Reference (Code(TASK_SUBTYPE)';

COMMENT ON COLUMN "WORKS"."MANUAL_CLOSE_FLAG" IS 'Flag to control wether to close the job automatically or not';

COMMENT ON COLUMN "WORKS"."MODULE_NAME" IS 'Name of the module which will carry out the task';

COMMENT ON COLUMN "WORKS"."ACTIVE_FLAG" IS 'If the record active';

COMMENT ON COLUMN "WORKS"."EXPIRY_DATE" IS 'Expriy date of the records';

COMMENT ON COLUMN "WORKS"."CASELOAD_TYPE" IS 'The agency location type Reference Code(AGY_LOC_TYPE)';

COMMENT ON COLUMN "WORKS"."MANUAL_SELECT_FLAG" IS '? If it is manudal select for Case notes';

COMMENT ON COLUMN "WORKS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "WORKS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "WORKS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "WORKS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "WORKS" IS 'A business event recorded in C-NOMIS which, depending on its work flow type, may trigger either : 1. the creation of one or more case notes 2. a Team being notified that an action is required as a result of the event (a task) 3. one or more Teams being notified that the event has occurred (a memo) The business event is categorised according to a) its Workflow type (eg Memo, Task, Case Note) b) its work type (eg Court Report, HDC, Appointment, Release) and c) its work subtype (eg HDC Request) For example, an occurrence of this entity might represent a Task which relates to Home Detention Curfew and which is a HDC Request for further information regarding the suitability of an address for HDC.';


CREATE INDEX "WORKS_X01"
  ON "WORKS" ("CASELOAD_TYPE", "WORKFLOW_TYPE");


