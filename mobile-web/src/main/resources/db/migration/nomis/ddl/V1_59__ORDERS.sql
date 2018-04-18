CREATE TABLE "ORDERS"
(
  "ORDER_ID"                      NUMBER(10, 0)                     NOT NULL ,
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ,
  "CASE_ID"                       NUMBER(10, 0),
  "COURT_DATE"                    DATE                              NOT NULL ,
  "ORDER_TYPE"                    VARCHAR2(12 CHAR)                 NOT NULL ,
  "ISSUING_AGY_LOC_ID"            VARCHAR2(6 CHAR)                  NOT NULL ,
  "COURT_INFO_ID"                 VARCHAR2(60 CHAR),
  "ORDER_STATUS"                  VARCHAR2(12 CHAR),
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "DUE_DATE"                      DATE,
  "COURT_SERIOUSNESS_LEVEL"       VARCHAR2(12 CHAR),
  "ORDER_SERIOUSNESS_LEVEL"       VARCHAR2(12 CHAR),
  "REQUEST_DATE"                  DATE,
  "STAFF_WORK_ID"                 NUMBER(10, 0),
  "EVENT_ID"                      NUMBER(10, 0),
  "COMPLETE_DATE"                 DATE,
  "COMPLETE_STAFF_ID"             NUMBER(10, 0),
  "INTERVENTION_TIER_CODE"        VARCHAR2(12 CHAR),
  "NON_REPORT_FLAG"               VARCHAR2(1 CHAR) DEFAULT 'N',
  "CPS_RECEIVED_DATE"             DATE,
  "COMMENT_TEXT"                  VARCHAR2(2000 CHAR),
  "ISSUE_DATE"                    DATE,
  "MESSAGE_ID"                    VARCHAR2(64 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "WORKFLOW_ID"                   NUMBER(32, 0),
  "OFFENDER_PROCEEDING_ID"        NUMBER(10, 0),
  CONSTRAINT "ORDERS_PK" PRIMARY KEY ("ORDER_ID"),
  CONSTRAINT "OFF_ORD_OFF_BKG_FK" FOREIGN KEY ("OFFENDER_BOOK_ID")
  REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID") ,
  CONSTRAINT "OFF_ORD_OFF_CAS_FK" FOREIGN KEY ("CASE_ID")
  REFERENCES "OFFENDER_CASES" ("CASE_ID") ,
  CONSTRAINT "OFF_ORD_AGY_LOC_F1" FOREIGN KEY ("ISSUING_AGY_LOC_ID")
  REFERENCES "AGENCY_LOCATIONS" ("AGY_LOC_ID")
);


COMMENT ON COLUMN "ORDERS"."ORDER_ID" IS 'A system generated identifer for an order.';

COMMENT ON COLUMN "ORDERS"."OFFENDER_BOOK_ID" IS 'System generated primary key for offender booking.';

COMMENT ON COLUMN "ORDERS"."CASE_ID" IS 'FK to the Offender Case';

COMMENT ON COLUMN "ORDERS"."COURT_DATE" IS ' Latest scheduled date of the court appearance.';

COMMENT ON COLUMN "ORDERS"."ORDER_TYPE" IS ' The Reference Code ( ORDER_TYPE )type of legal order - ie. Remand, Detainer,Sentence ...';

COMMENT ON COLUMN "ORDERS"."ISSUING_AGY_LOC_ID" IS 'Court issuing the legal order.';

COMMENT ON COLUMN "ORDERS"."COURT_INFO_ID" IS ' A number assigned to the order by the court.';

COMMENT ON COLUMN "ORDERS"."ORDER_STATUS" IS 'Reference Code ( ORDER_STS ) THE CURRENT ORDER STATUS - ACTIVE, INACTIVE,EXPIRED..';

COMMENT ON COLUMN "ORDERS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "ORDERS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "ORDERS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "ORDERS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "ORDERS"."DUE_DATE" IS 'The due date of the order (Report)';

COMMENT ON COLUMN "ORDERS"."COURT_SERIOUSNESS_LEVEL" IS 'Reference Code (ORD_SERIOUS) The seriousness level from court''s view';

COMMENT ON COLUMN "ORDERS"."ORDER_SERIOUSNESS_LEVEL" IS 'Reference Code (ORD_SERIOUS) The seriousness level from the order''s view';

COMMENT ON COLUMN "ORDERS"."REQUEST_DATE" IS 'The request date (Reports)';

COMMENT ON COLUMN "ORDERS"."STAFF_WORK_ID" IS 'FK to Staff Work';

COMMENT ON COLUMN "ORDERS"."EVENT_ID" IS 'FK to Court Events';

COMMENT ON COLUMN "ORDERS"."COMPLETE_DATE" IS 'The date of completion';

COMMENT ON COLUMN "ORDERS"."COMPLETE_STAFF_ID" IS 'The Intervention tier';

COMMENT ON COLUMN "ORDERS"."INTERVENTION_TIER_CODE" IS 'The level of intervention Reference Code (INTV_TIER)';

COMMENT ON COLUMN "ORDERS"."NON_REPORT_FLAG" IS 'If the order an non-report.';

COMMENT ON COLUMN "ORDERS"."CPS_RECEIVED_DATE" IS 'The date CPS is received';

COMMENT ON COLUMN "ORDERS"."COMMENT_TEXT" IS 'The general comment text of the order';

COMMENT ON TABLE "ORDERS" IS 'The Legal Offender_legal_orders, including court reports such as Pre-Sentence reports';


CREATE INDEX "ORDERS_FK1"
  ON "ORDERS" ("OFFENDER_PROCEEDING_ID");


CREATE INDEX "ORDERS_NI1"
  ON "ORDERS" ("OFFENDER_BOOK_ID");


CREATE INDEX "ORDERS_NI2"
  ON "ORDERS" ("CASE_ID");


CREATE INDEX "ORDERS_NI3"
  ON "ORDERS" ("ISSUING_AGY_LOC_ID");


CREATE INDEX "ORDERS_NI4"
  ON "ORDERS" ("EVENT_ID");


CREATE INDEX "ORDERS_NI5"
  ON "ORDERS" ("COURT_INFO_ID");


CREATE INDEX "ORDERS_NI6"
  ON "ORDERS" ("COURT_DATE");

