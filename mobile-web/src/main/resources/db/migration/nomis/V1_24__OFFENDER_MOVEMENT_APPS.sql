CREATE TABLE "OFFENDER_MOVEMENT_APPS"
(
  "OFFENDER_MOVEMENT_APP_ID"      NUMBER(10, 0)                     NOT NULL ,
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ,
  "EVENT_CLASS"                   VARCHAR2(12 CHAR)                 NOT NULL ,
  "EVENT_TYPE"                    VARCHAR2(12 CHAR)                 NOT NULL ,
  "EVENT_SUB_TYPE"                VARCHAR2(12 CHAR)                 NOT NULL ,
  "APPLICATION_DATE"              DATE                              NOT NULL ,
  "APPLICATION_TIME"              DATE                              NOT NULL ,
  "FROM_DATE"                     DATE                              NOT NULL ,
  "RELEASE_TIME"                  DATE                              NOT NULL ,
  "TO_DATE"                       DATE                              NOT NULL ,
  "RETURN_TIME"                   DATE                              NOT NULL ,
  "APPLICATION_STATUS"            VARCHAR2(12 CHAR)                 NOT NULL ,
  "ESCORT_CODE"                   VARCHAR2(12 CHAR),
  "TRANSPORT_CODE"                VARCHAR2(12 CHAR),
  "COMMENT_TEXT"                  VARCHAR2(4000 CHAR),
  "TO_ADDRESS_OWNER_CLASS"        VARCHAR2(12 CHAR),
  "TO_ADDRESS_ID"                 NUMBER(10, 0),
  "AGY_LOC_ID"                    VARCHAR2(6 CHAR),
  "TO_AGY_LOC_ID"                 VARCHAR2(6 CHAR),
  "CONTACT_PERSON_NAME"           VARCHAR2(40 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "APPLICATION_TYPE"              VARCHAR2(12 CHAR)                 NOT NULL ,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "TAP_ABS_TYPE"                  VARCHAR2(12 CHAR),
  "TAP_ABS_SUBTYPE"               VARCHAR2(12 CHAR),
  CONSTRAINT "OFFENDER_MOVEMENT_APPS_PK" PRIMARY KEY ("OFFENDER_MOVEMENT_APP_ID") ,
  CONSTRAINT "OFF_MOV_APP_AGY_LOC_FK" FOREIGN KEY ("AGY_LOC_ID")
  REFERENCES "AGENCY_LOCATIONS" ("AGY_LOC_ID") ,
  CONSTRAINT "OFF_MOV_APP_OFF_BKG_FK" FOREIGN KEY ("OFFENDER_BOOK_ID")
  REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID")
);


CREATE INDEX "OFF_MOV_APP_AGY_LOC_FK"
  ON "OFFENDER_MOVEMENT_APPS" ("AGY_LOC_ID");


CREATE INDEX "OFF_MOV_APP_OFF_BKG_FK"
  ON "OFFENDER_MOVEMENT_APPS" ("OFFENDER_BOOK_ID");


