
  CREATE TABLE "OFFENDER_TMP_REL_SCHEDULES"
   (    "OFFENDER_BOOK_ID" NUMBER(10,0) NOT NULL,
    "SESSION_ID" NUMBER(10,0) NOT NULL,
    "OFFENDER_ID_DISPLAY" VARCHAR2(10 CHAR),
    "LAST_NAME" VARCHAR2(35 CHAR),
    "FIRST_NAME" VARCHAR2(35 CHAR),
    "COLUMN_FOUR_DATE" DATE,
    "COLUMN_FIVE_DATE" DATE,
    "COLUMN_SIX_DATE" DATE,
    "RELEASE_DATE" DATE,
    "COMMENT_TEXT" VARCHAR2(240 CHAR),
    "MOVEMENT_TYPE" VARCHAR2(12 CHAR),
    "MOVEMENT_REASON_CODE" VARCHAR2(12 CHAR),
    "COLUMN_SEVEN_DATE" DATE,
    "COLUMN_EIGHT_DATE" DATE,
    "COLUMN_NINE_DATE" DATE,
    "CREATE_DATETIME" TIMESTAMP (9) DEFAULT systimestamp NOT NULL,
    "CREATE_USER_ID" VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
    "MODIFY_DATETIME" TIMESTAMP (9),
    "MODIFY_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_TIMESTAMP" TIMESTAMP (9),
    "AUDIT_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_MODULE_NAME" VARCHAR2(65 CHAR),
    "AUDIT_CLIENT_USER_ID" VARCHAR2(64 CHAR),
    "AUDIT_CLIENT_IP_ADDRESS" VARCHAR2(39 CHAR),
    "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
    "AUDIT_ADDITIONAL_INFO" VARCHAR2(256 CHAR),
     CONSTRAINT "OFFENDER_TMP_REL_SCHEDULES_PK" PRIMARY KEY ("OFFENDER_BOOK_ID", "SESSION_ID")
  );

  CREATE INDEX "OFF_TRS_MOV_RSN_FK1" ON "OFFENDER_TMP_REL_SCHEDULES" ("MOVEMENT_TYPE", "MOVEMENT_REASON_CODE");


  CREATE UNIQUE INDEX "OFFENDER_TMP_REL_SCHEDULES_PK" ON "OFFENDER_TMP_REL_SCHEDULES" ("OFFENDER_BOOK_ID", "SESSION_ID");
