CREATE TABLE "OFFENDER_ALERTS"
(
  "ALERT_DATE"                    DATE DEFAULT SYSDATE              NOT NULL ,
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ,
  "ROOT_OFFENDER_ID"              NUMBER(10, 0),
  "ALERT_SEQ"                     NUMBER(6, 0)                      NOT NULL ,
  "ALERT_TYPE"                    VARCHAR2(12 CHAR)                 NOT NULL ,
  "ALERT_CODE"                    VARCHAR2(12 CHAR)                 NOT NULL ,
  "AUTHORIZE_PERSON_TEXT"         VARCHAR2(40 CHAR),
  "CREATE_DATE"                   DATE,
  "ALERT_STATUS"                  VARCHAR2(12 CHAR)                 NOT NULL ,
  "VERIFIED_FLAG"                 VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "EXPIRY_DATE"                   DATE,
  "COMMENT_TEXT"                  VARCHAR2(1000 CHAR),
  "CASELOAD_ID"                   VARCHAR2(6 CHAR),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "CASELOAD_TYPE"                 VARCHAR2(12 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "ALERTS_PK" PRIMARY KEY ("OFFENDER_BOOK_ID", "ALERT_SEQ"),
  CONSTRAINT "OFF_ALERT_OFF_BKG_F1" FOREIGN KEY ("OFFENDER_BOOK_ID")
  REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID")
);


COMMENT ON COLUMN "OFFENDER_ALERTS"."ALERT_DATE" IS 'This indicates the date the alert was imposed on the offender. - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."OFFENDER_BOOK_ID" IS ' System identifier for offender booking. - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."ROOT_OFFENDER_ID" IS 'The Root Offender Identifier';

COMMENT ON COLUMN "OFFENDER_ALERTS"."ALERT_SEQ" IS 'Sequence number forming part of primary key. - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."ALERT_TYPE" IS ' Refrence Code [ ALERT ] :The type of alert placed on offender"s record. ie. Security,
 Medical, Tra - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."ALERT_CODE" IS 'Reference Code [ ALERT_CODE ] : The alert code associated with the type entered.
 For example, code - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."AUTHORIZE_PERSON_TEXT" IS 'Person AUTHORIZING PLACEMENT OF ALERT. - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."ALERT_STATUS" IS 'Refrence Code [ STATUS ] : Alerts can either be active or inactive. They become inactive after passi - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."VERIFIED_FLAG" IS ' - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."EXPIRY_DATE" IS 'Expiry Date of the Alert - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."COMMENT_TEXT" IS ' Comments describing the reason for the alert. - Retrofitted';

COMMENT ON COLUMN "OFFENDER_ALERTS"."CASELOAD_ID" IS 'The Case Load Identifier';

COMMENT ON COLUMN "OFFENDER_ALERTS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "OFFENDER_ALERTS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "OFFENDER_ALERTS"."CASELOAD_TYPE" IS 'Reference codes (CSLD_TYPE)';

COMMENT ON COLUMN "OFFENDER_ALERTS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "OFFENDER_ALERTS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON TABLE "OFFENDER_ALERTS" IS 'Alerts pertain to cautions such as security, medical or transport alerts.- Retrofitted';


CREATE INDEX "OFFENDER_ALERTS_NI2"
  ON "OFFENDER_ALERTS" ("ROOT_OFFENDER_ID");

