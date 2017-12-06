CREATE TABLE "OFFENDER_CASE_NOTES"
(
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ,
  "CONTACT_DATE"                  DATE                              NOT NULL ,
  "CONTACT_TIME"                  DATE                              NOT NULL ,
  "CASE_NOTE_TYPE"                VARCHAR2(12 CHAR)                 NOT NULL ,
  "CASE_NOTE_SUB_TYPE"            VARCHAR2(12 CHAR)                 NOT NULL ,
  "STAFF_ID"                      NUMBER(10, 0)                     NOT NULL ,
  "CASE_NOTE_TEXT"                VARCHAR2(4000 CHAR),
  "AMENDMENT_FLAG"                VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "IWP_FLAG"                      VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "CHECK_BOX1"                    VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "CHECK_BOX2"                    VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "CHECK_BOX3"                    VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "CHECK_BOX4"                    VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "CHECK_BOX5"                    VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "EVENT_ID"                      NUMBER(10, 0),
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "CASE_NOTE_ID"                  NUMBER(10, 0)                     NOT NULL ,
  "NOTE_SOURCE_CODE"              VARCHAR2(12 CHAR),
  "DATE_CREATION"                 DATE DEFAULT sysdate,
  "TIME_CREATION"                 DATE,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "OFFENDER_CASE_NOTES_PK" PRIMARY KEY ("CASE_NOTE_ID"),
  CONSTRAINT "OFF_CN_OFF_BKG_FK" FOREIGN KEY ("OFFENDER_BOOK_ID")
  REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID") ,
  CONSTRAINT "OFF_CN_STF_FK" FOREIGN KEY ("STAFF_ID")
  REFERENCES "STAFF_MEMBERS" ("STAFF_ID")
);

COMMENT ON COLUMN "OFFENDER_CASE_NOTES"."OFFENDER_BOOK_ID" IS 'The Related Offender Book Identifier';
COMMENT ON COLUMN "OFFENDER_CASE_NOTES"."CREATE_USER_ID" IS 'The user who creates the record';
COMMENT ON COLUMN "OFFENDER_CASE_NOTES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';
COMMENT ON COLUMN "OFFENDER_CASE_NOTES"."MODIFY_USER_ID" IS 'The user who modifies the record';
COMMENT ON COLUMN "OFFENDER_CASE_NOTES"."CREATE_DATETIME" IS 'The timestamp when the record is created';
COMMENT ON TABLE "OFFENDER_CASE_NOTES" IS 'A free format textual note pertinent to an event or contact that occurs during an offenders period of supervision and/or custody. Notes are held chronologically and may either be entered manually or generated automatically. Instances created automatically provide narrative for newly scheduled events. Narrative text format varies dependant upon scheduled event type but is internally constructed by the respective application function and not user configurable. e.g. An Appointment/Home Visit Offender Schedule entry is created. The note An appointment for Appointment/Home Visit has been made. Comment: is automatically generated.';


CREATE INDEX "OFFENDER_CASE_NOTES_NI1"
  ON "OFFENDER_CASE_NOTES" ("STAFF_ID");


CREATE INDEX "OFFENDER_CASE_NOTES_NI2"
  ON "OFFENDER_CASE_NOTES" ("OFFENDER_BOOK_ID");


CREATE INDEX "OFFENDER_CASE_NOTES_NI3"
  ON "OFFENDER_CASE_NOTES" ("CONTACT_DATE");


CREATE INDEX "OFFENDER_CASE_NOTES_X01"
  ON "OFFENDER_CASE_NOTES" ("OFFENDER_BOOK_ID", "CONTACT_DATE");


CREATE INDEX "OFFENDER_CASE_NOTES_X02"
  ON "OFFENDER_CASE_NOTES" ("EVENT_ID", "CASE_NOTE_ID");


CREATE INDEX "OFFENDER_CASE_NOTES_X04"
  ON "OFFENDER_CASE_NOTES" ("AUDIT_TIMESTAMP", "CASE_NOTE_TYPE", "CASE_NOTE_SUB_TYPE");

