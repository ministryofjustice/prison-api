
  CREATE TABLE "OFFENDER_SUPERVISING_COURTS"
   (    "OFFENDER_BOOK_ID" NUMBER(10,0) NOT NULL,
    "COURT_AGY_LOC_ID" VARCHAR2(6 CHAR) NOT NULL,
    "RECORD_DATE" DATE DEFAULT sysdate NOT NULL,
    "CREATE_DATETIME" TIMESTAMP (9) DEFAULT systimestamp NOT NULL,
    "CREATE_USER_ID" VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
    "MODIFY_DATETIME" TIMESTAMP (9),
    "MODIFY_USER_ID" VARCHAR2(32 CHAR),
    "OFFENDER_SUPERVISING_COURT_ID" NUMBER(10,0) NOT NULL,
    "AUDIT_TIMESTAMP" TIMESTAMP (9),
    "AUDIT_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_MODULE_NAME" VARCHAR2(65 CHAR),
    "AUDIT_CLIENT_USER_ID" VARCHAR2(64 CHAR),
    "AUDIT_CLIENT_IP_ADDRESS" VARCHAR2(39 CHAR),
    "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
    "AUDIT_ADDITIONAL_INFO" VARCHAR2(256 CHAR),
     CONSTRAINT "OFFENDER_SUPERVISING_COURTS_PK" PRIMARY KEY ("OFFENDER_SUPERVISING_COURT_ID")
  );

  CREATE UNIQUE INDEX "OFFENDER_SUPERVISING_COURTS_UK" ON "OFFENDER_SUPERVISING_COURTS" ("OFFENDER_BOOK_ID", "COURT_AGY_LOC_ID", "RECORD_DATE");
  CREATE INDEX "OFFENDER_SUPERVISING_COURTS_N1" ON "OFFENDER_SUPERVISING_COURTS" ("COURT_AGY_LOC_ID");
  CREATE UNIQUE INDEX "OFFENDER_SUPERVISING_COURTS_PK" ON "OFFENDER_SUPERVISING_COURTS" ("OFFENDER_SUPERVISING_COURT_ID");
