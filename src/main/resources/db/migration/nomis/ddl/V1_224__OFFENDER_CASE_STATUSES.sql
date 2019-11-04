
  CREATE TABLE "OFFENDER_CASE_STATUSES"
   (    "CASE_ID" NUMBER(10,0) NOT NULL,
    "STATUS_UPDATE_REASON" VARCHAR2(12 CHAR) NOT NULL,
    "STATUS_UPDATE_COMMENT" VARCHAR2(400 CHAR),
    "STATUS_UPDATE_DATE" DATE DEFAULT sysdate NOT NULL,
    "STATUS_UPDATE_STAFF_ID" NUMBER(10,0) NOT NULL,
    "OFFENDER_CASE_STATUS_ID" NUMBER(10,0) NOT NULL,
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
     CONSTRAINT "OFFENDER_CASE_STATUSES_PK" PRIMARY KEY ("OFFENDER_CASE_STATUS_ID"),
  );

  CREATE INDEX "OFF_CASE_STS_LGL_UPD_RSN_FK" ON "OFFENDER_CASE_STATUSES" ("STATUS_UPDATE_REASON");


  CREATE INDEX "OFF_CASE_STS_OFF_CASE_FK" ON "OFFENDER_CASE_STATUSES" ("CASE_ID");


  CREATE UNIQUE INDEX "OFFENDER_CASE_STATUSES_PK" ON "OFFENDER_CASE_STATUSES" ("OFFENDER_CASE_STATUS_ID");
