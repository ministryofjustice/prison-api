
  CREATE TABLE "OFFENDER_VSC_ERROR_LOGS"
   (    "OFFENDER_VSC_LOG_ID" NUMBER(10,0) NOT NULL,
    "OFFENDER_ID_DISPLAY" VARCHAR2(10 CHAR) NOT NULL,
    "OFFENDER_BOOK_ID" NUMBER(10,0) NOT NULL,
    "AGY_LOC_ID" VARCHAR2(6 CHAR),
    "LOG_MESSAGE" VARCHAR2(240 CHAR) NOT NULL,
    "AUDIT_TIMESTAMP" TIMESTAMP (9),
    "AUDIT_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_MODULE_NAME" VARCHAR2(65 CHAR),
    "AUDIT_CLIENT_USER_ID" VARCHAR2(64 CHAR),
    "AUDIT_CLIENT_IP_ADDRESS" VARCHAR2(39 CHAR),
    "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
    "AUDIT_ADDITIONAL_INFO" VARCHAR2(256 CHAR),
    "CREATE_DATETIME" TIMESTAMP (9) DEFAULT systimestamp NOT NULL,
    "CREATE_USER_ID" VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
    "MODIFY_DATETIME" TIMESTAMP (9),
    "MODIFY_USER_ID" VARCHAR2(32 CHAR),
     PRIMARY KEY ("OFFENDER_VSC_LOG_ID")
  );

  CREATE INDEX "OFFENDER_VSC_ERROR_LOGS_NI1" ON "OFFENDER_VSC_ERROR_LOGS" ("OFFENDER_BOOK_ID");


  CREATE UNIQUE INDEX "SYS_C0012076" ON "OFFENDER_VSC_ERROR_LOGS" ("OFFENDER_VSC_LOG_ID");
