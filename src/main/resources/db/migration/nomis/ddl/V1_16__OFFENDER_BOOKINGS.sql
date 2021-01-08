CREATE TABLE "OFFENDER_BOOKINGS"
(
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ,
  "BOOKING_BEGIN_DATE"            DATE                              NOT NULL ,
  "BOOKING_END_DATE"              DATE,
  "BOOKING_NO"                    VARCHAR2(14 CHAR),
  "OFFENDER_ID"                   NUMBER(10, 0)                     NOT NULL ,
  "AGY_LOC_ID"                    VARCHAR2(6 CHAR),
  "LIVING_UNIT_ID"                NUMBER(10, 0),
  "DISCLOSURE_FLAG"               VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "IN_OUT_STATUS"                 VARCHAR2(12 CHAR)                 NOT NULL ,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'N',
  "BOOKING_STATUS"                VARCHAR2(12 CHAR),
  "YOUTH_ADULT_CODE"              VARCHAR2(12 CHAR)                 NOT NULL ,
  "FINGER_PRINTED_STAFF_ID"       NUMBER(10, 0),
  "SEARCH_STAFF_ID"               NUMBER(10, 0),
  "PHOTO_TAKING_STAFF_ID"         NUMBER(10, 0),
  "ASSIGNED_STAFF_ID"             NUMBER(10, 0),
  "CREATE_AGY_LOC_ID"             VARCHAR2(6 CHAR),
  "BOOKING_TYPE"                  VARCHAR2(12 CHAR),
  "BOOKING_CREATED_DATE"          DATE,
  "ROOT_OFFENDER_ID"              NUMBER(10, 0),
  "AGENCY_IML_ID"                 NUMBER(10, 0),
  "SERVICE_FEE_FLAG"              VARCHAR2(1 CHAR) DEFAULT 'N',
  "EARNED_CREDIT_LEVEL"           VARCHAR2(12 CHAR),
  "EKSTRAND_CREDIT_LEVEL"         VARCHAR2(12 CHAR),
  "INTAKE_AGY_LOC_ID"             VARCHAR2(6 CHAR),
  "ACTIVITY_DATE"                 DATE,
  "INTAKE_CASELOAD_ID"            VARCHAR2(6 CHAR),
  "INTAKE_USER_ID"                VARCHAR2(32 CHAR),
  "CASE_OFFICER_ID"               NUMBER(6, 0),
  "CASE_DATE"                     DATE,
  "CASE_TIME"                     DATE,
  "COMMUNITY_ACTIVE_FLAG"         VARCHAR2(1 CHAR) DEFAULT 'N',
  "CREATE_INTAKE_AGY_LOC_ID"      VARCHAR2(6 CHAR),
  "COMM_STAFF_ID"                 NUMBER(10, 0),
  "COMM_STATUS"                   VARCHAR2(12 CHAR),
  "COMMUNITY_AGY_LOC_ID"          VARCHAR2(6 CHAR),
  "NO_COMM_AGY_LOC_ID"            NUMBER(6, 0),
  "COMM_STAFF_ROLE"               VARCHAR2(12 CHAR),
  "AGY_LOC_ID_LIST"               VARCHAR2(80 CHAR),
  "STATUS_REASON"                 VARCHAR2(32 CHAR),
  "TOTAL_UNEXCUSED_ABSENCES"      NUMBER(6, 0),
  "REQUEST_NAME"                  VARCHAR2(240 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "RECORD_USER_ID"                VARCHAR2(30 CHAR),
  "INTAKE_AGY_LOC_ASSIGN_DATE"    DATE,
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "BOOKING_SEQ"                   NUMBER(6, 0)                      NOT NULL ,
  "ADMISSION_REASON"              VARCHAR2(12 CHAR),
  CONSTRAINT "OFFENDER_BOOKINGS_PK" PRIMARY KEY ("OFFENDER_BOOK_ID"),
  CONSTRAINT "OFF_BKG_OFF_F1"     FOREIGN KEY ("OFFENDER_ID") REFERENCES "OFFENDERS" ("OFFENDER_ID") ,
  CONSTRAINT "OFF_BKG_AGY_LOC_F1" FOREIGN KEY ("AGY_LOC_ID")  REFERENCES "AGENCY_LOCATIONS" ("AGY_LOC_ID")
);


COMMENT ON COLUMN "OFFENDER_BOOKINGS"."OFFENDER_BOOK_ID" IS ' The system generated identifier for an offender booking.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."BOOKING_BEGIN_DATE" IS ' The date the booking was created.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."BOOKING_END_DATE" IS 'The date the booking was closed.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."BOOKING_NO" IS ' The identifier for the booking which is visible to the user.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."OFFENDER_ID" IS ' The unique identifier for an offender.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."AGY_LOC_ID" IS ' The location residing within an agency.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."LIVING_UNIT_ID" IS 'System generated id for offender bed location.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."DISCLOSURE_FLAG" IS 'A flag relating to the disclosure of offender information.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."IN_OUT_STATUS" IS 'Reference Code ( IN_OUT_STS ) Indicates whether the offender is currently in or out of the facility.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."ACTIVE_FLAG" IS ' Indicates whether the offender is active or inactive at the location.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."BOOKING_STATUS" IS 'Reference Code ( BOOK_STS ). The open or closed status of the booking.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."YOUTH_ADULT_CODE" IS 'Reference Code (  YOUTH_ADULT ) : Indicates whether the offender is currently a youth or adult.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."FINGER_PRINTED_STAFF_ID" IS 'The staff member finger printing the offender.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."SEARCH_STAFF_ID" IS 'Id of staff member who searched offender.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."PHOTO_TAKING_STAFF_ID" IS ' Id of staff member who took offender"s image';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."ASSIGNED_STAFF_ID" IS 'COMM Side assigned Staff ID.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."ROOT_OFFENDER_ID" IS 'The Root Offender Identifier';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."CASE_OFFICER_ID" IS 'INST side Current Case Officer staff ID, with history assignment in Offender Case Officers';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."COMM_STAFF_ID" IS 'COMM side case plan staff ID.  History table in Case Plans';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."STATUS_REASON" IS 'For Ontario Header Status';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."TOTAL_UNEXCUSED_ABSENCES" IS 'Captures the total unexcused absences for the offender within the booking based on the event_outcome column in offender_schedules table.';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "OFFENDER_BOOKINGS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "OFFENDER_BOOKINGS" IS 'A period of supervision of the identified offender while under the supervision of the Prison and / or Probation services. This may include the actual period of a custodial sentence from initial incarceration through to actual release and any follow up period of supervision during probation and subsequently. This may be for one sentence, but may also be extended by additional periods (possibly due to other sentences) which are determined during the period of supervision.';


CREATE INDEX "OFFENDER_BOOKINGS_NI1"
  ON "OFFENDER_BOOKINGS" ("BOOKING_NO");


CREATE INDEX "OFFENDER_BOOKINGS_NI2"
  ON "OFFENDER_BOOKINGS" ("OFFENDER_ID");


CREATE INDEX "OFFENDER_BOOKINGS_NI3"
  ON "OFFENDER_BOOKINGS" ("AGY_LOC_ID", "LIVING_UNIT_ID");


CREATE INDEX "OFFENDER_BOOKINGS_NI5"
  ON "OFFENDER_BOOKINGS" ("ROOT_OFFENDER_ID", "ACTIVE_FLAG", "OFFENDER_BOOK_ID");


CREATE INDEX "OFFENDER_BOOKINGS_NI6"
  ON "OFFENDER_BOOKINGS" ("LIVING_UNIT_ID");


CREATE INDEX "OFFENDER_BOOKINGS_NI7"
  ON "OFFENDER_BOOKINGS" ("AGENCY_IML_ID");


CREATE INDEX "OFFENDER_BOOKINGS_NI8"
  ON "OFFENDER_BOOKINGS" ("ASSIGNED_STAFF_ID");


CREATE INDEX "OFFENDER_BOOKINGS_NI9"
  ON "OFFENDER_BOOKINGS" ("AGY_LOC_ID", "LIVING_UNIT_ID", "AGENCY_IML_ID", "IN_OUT_STATUS", "ACTIVE_FLAG");






