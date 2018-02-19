CREATE TABLE "STAFF_MEMBERS"
(
  "STAFF_ID"                      NUMBER(10, 0)                     NOT NULL ,
  "ASSIGNED_CASELOAD_ID"          VARCHAR2(6 CHAR),
  "WORKING_STOCK_LOC_ID"          VARCHAR2(6 CHAR),
  "WORKING_CASELOAD_ID"           VARCHAR2(6 CHAR),
  "USER_ID"                       VARCHAR2(32 CHAR),
  "BADGE_ID"                      VARCHAR2(20 CHAR),
  "LAST_NAME"                     VARCHAR2(35 CHAR)                 NOT NULL ,
  "FIRST_NAME"                    VARCHAR2(35 CHAR)                 NOT NULL ,
  "MIDDLE_NAME"                   VARCHAR2(35 CHAR),
  "ABBREVIATION"                  VARCHAR2(15 CHAR),
  "POSITION"                      VARCHAR2(25 CHAR),
  "BIRTHDATE"                     DATE,
  "TERMINATION_DATE"              DATE,
  "UPDATE_ALLOWED_FLAG"           VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "DEFAULT_PRINTER_ID"            NUMBER(10, 0),
  "SUSPENDED_FLAG"                VARCHAR2(1 CHAR) DEFAULT 'N',
  "SUPERVISOR_STAFF_ID"           NUMBER(10, 0),
  "COMM_RECEIPT_PRINTER_ID"       VARCHAR2(12 CHAR),
  "PERSONNEL_TYPE"                VARCHAR2(12 CHAR),
  "AS_OF_DATE"                    DATE,
  "EMERGENCY_CONTACT"             VARCHAR2(20 CHAR),
  "ROLE"                          VARCHAR2(12 CHAR),
  "SEX_CODE"                      VARCHAR2(12 CHAR),
  "STATUS"                        VARCHAR2(12 CHAR),
  "SUSPENSION_DATE"               DATE,
  "SUSPENSION_REASON"             VARCHAR2(12 CHAR),
  "FORCE_PASSWORD_CHANGE_FLAG"    VARCHAR2(1 CHAR) DEFAULT 'N',
  "LAST_PASSWORD_CHANGE_DATE"     DATE,
  "LICENSE_CODE"                  VARCHAR2(12 CHAR),
  "LICENSE_EXPIRY_DATE"           DATE,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "TITLE"                         VARCHAR2(12 CHAR),
  "NAME_SEQUENCE"                 VARCHAR2(12 CHAR),
  "QUEUE_CLUSTER_ID"              NUMBER(6, 0),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "FIRST_LOGON_FLAG"              VARCHAR2(1 CHAR) DEFAULT 'N',
  "SIGNIFICANT_DATE"              DATE,
  "SIGNIFICANT_NAME"              VARCHAR2(100 CHAR),
  "NATIONAL_INSURANCE_NUMBER"     VARCHAR2(13 CHAR),
  CONSTRAINT "STAFF_MEMBERS_PK" PRIMARY KEY ("STAFF_ID") ,
  CONSTRAINT "STAFF_MEMBERS_PK2" UNIQUE ("USER_ID") ,
  CONSTRAINT "STAFF_STAFF_F1" FOREIGN KEY ("SUPERVISOR_STAFF_ID") REFERENCES "STAFF_MEMBERS" ("STAFF_ID") ,
  CONSTRAINT "STAFF_CSLD_F2"  FOREIGN KEY ("WORKING_CASELOAD_ID") REFERENCES "CASELOADS" ("CASELOAD_ID") ,
  CONSTRAINT "STAFF_CSLD_F1"  FOREIGN KEY ("ASSIGNED_CASELOAD_ID") REFERENCES "CASELOADS" ("CASELOAD_ID")
);


COMMENT ON COLUMN "STAFF_MEMBERS"."STAFF_ID" IS 'System generated number associated with user account';
COMMENT ON COLUMN "STAFF_MEMBERS"."ASSIGNED_CASELOAD_ID" IS ' Caseload staff member assigned to.';
COMMENT ON COLUMN "STAFF_MEMBERS"."WORKING_STOCK_LOC_ID" IS ' Commissary location where stock items are kept.';
COMMENT ON COLUMN "STAFF_MEMBERS"."WORKING_CASELOAD_ID" IS ' Caseload staff member is currently working on.';
COMMENT ON COLUMN "STAFF_MEMBERS"."USER_ID" IS ' User Account Id for the staff member.';
COMMENT ON COLUMN "STAFF_MEMBERS"."BADGE_ID" IS 'Officer Badge No.';
COMMENT ON COLUMN "STAFF_MEMBERS"."LAST_NAME" IS ' Last name of staff member.';
COMMENT ON COLUMN "STAFF_MEMBERS"."FIRST_NAME" IS ' First name of staff member.';
COMMENT ON COLUMN "STAFF_MEMBERS"."MIDDLE_NAME" IS ' Middle name of staff member.';
COMMENT ON COLUMN "STAFF_MEMBERS"."ABBREVIATION" IS ' Abbreviation of staff member"s name.';
COMMENT ON COLUMN "STAFF_MEMBERS"."POSITION" IS ' Staff member"s job position.';
COMMENT ON COLUMN "STAFF_MEMBERS"."BIRTHDATE" IS ' Satff member"s birth date.';
COMMENT ON COLUMN "STAFF_MEMBERS"."TERMINATION_DATE" IS ' Date staff member terminated from job.';
COMMENT ON COLUMN "STAFF_MEMBERS"."UPDATE_ALLOWED_FLAG" IS ' Should user have update capability on caseload (Y/N)?';
COMMENT ON COLUMN "STAFF_MEMBERS"."DEFAULT_PRINTER_ID" IS ' Default printer for the staff member.';
COMMENT ON COLUMN "STAFF_MEMBERS"."SUSPENDED_FLAG" IS 'Allows for the temporary suspension of the staff member"s user account.';
COMMENT ON COLUMN "STAFF_MEMBERS"."SUPERVISOR_STAFF_ID" IS ' Supervisor"s staff id.';
COMMENT ON COLUMN "STAFF_MEMBERS"."CREATE_DATETIME" IS 'The timestamp when the record is created';
COMMENT ON COLUMN "STAFF_MEMBERS"."CREATE_USER_ID" IS 'The user who creates the record';
COMMENT ON COLUMN "STAFF_MEMBERS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';
COMMENT ON COLUMN "STAFF_MEMBERS"."MODIFY_USER_ID" IS 'The user who modifies the record';
COMMENT ON COLUMN "STAFF_MEMBERS"."TITLE" IS 'The title of the staff';
COMMENT ON COLUMN "STAFF_MEMBERS"."FIRST_LOGON_FLAG" IS '? If it is the first logon of the staff';
COMMENT ON TABLE "STAFF_MEMBERS" IS 'A person who has a direct (eg. prison officers) or supporting (eg. Human Resources) role within the Offender Management process Typically the person will have a contract of employment with NOMS but in some cases will not - for example, unpaid workers or contractors who require security passes or access to NOMIS.';


CREATE INDEX "STAFF_MEMBERS_FK1"
  ON "STAFF_MEMBERS" ("ASSIGNED_CASELOAD_ID");
CREATE INDEX "STAFF_MEMBERS_FK2"
  ON "STAFF_MEMBERS" ("WORKING_CASELOAD_ID");
CREATE INDEX "STAFF_MEMBERS_NI1"
  ON "STAFF_MEMBERS" ("LAST_NAME", "FIRST_NAME");
CREATE INDEX "STAFF_MEMBERS_X01"
  ON "STAFF_MEMBERS" ("USER_ID", "STAFF_ID", "WORKING_CASELOAD_ID");
CREATE INDEX "STAFF_MEMBERS_X02"
  ON "STAFF_MEMBERS" ("NATIONAL_INSURANCE_NUMBER");
CREATE INDEX "STAFF_PTR_F2"
  ON "STAFF_MEMBERS" ("COMM_RECEIPT_PRINTER_ID");
CREATE INDEX "STAFF_STAFF_FK1"
  ON "STAFF_MEMBERS" ("SUPERVISOR_STAFF_ID");
