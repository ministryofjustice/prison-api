
  CREATE TABLE "CASELOAD_DEDUCTION_PROFILES"
   (    "DELAY_RECAPTURE" NUMBER(3,0),
    "ACTIVE_FLAG" VARCHAR2(1 CHAR) DEFAULT Y NOT NULL,
    "CASELOAD_ID" VARCHAR2(6 CHAR) NOT NULL,
    "DEDUCTION_TYPE" VARCHAR2(6 CHAR) NOT NULL,
    "EFFECTIVE_DATE" DATE NOT NULL,
    "FIFO_FLAG" VARCHAR2(1 CHAR) DEFAULT Y NOT NULL,
    "FO_AL_ALL_OFFENDER_FLAG" VARCHAR2(1 CHAR) DEFAULT N NOT NULL,
    "PERCENTAGE" NUMBER(5,2) NOT NULL,
    "INTERNAL_PRIORITY_NO" NUMBER(2,0) NOT NULL,
    "EXTERNAL_PRIORITY_NO" NUMBER(2,0) NOT NULL,
    "ACCOUNT_CODE" NUMBER(6,0) NOT NULL,
    "CO_LIMIT_AMOUNT" NUMBER(11,2),
    "CO_CREDIT_WHEN_INDIGENT_FLAG" VARCHAR2(1 CHAR) DEFAULT N,
    "MAX_MONTHLY_AMOUNT" NUMBER(11,2),
    "MAX_TOTAL_AMOUNT" NUMBER(11,2),
    "EXPIRY_DATE" DATE,
    "PAYEE_PERSON_ID" NUMBER(10,0),
    "PAYEE_CORPORATE_ID" NUMBER(10,0),
    "MODIFY_USER_ID" VARCHAR2(32 CHAR),
    "MODIFY_DATE" DATE NOT NULL,
    "LIST_SEQ" NUMBER(6,0) DEFAULT 99,
    "FLAT_RATE" NUMBER(11,2),
    "MINIMUM_TRUST_BALANCE" NUMBER(11,2),
    "INDIGENT_MANDATORY_FLAG" VARCHAR2(1 CHAR) DEFAULT N,
    "COMM_CONDITION_TYPE" VARCHAR2(12 CHAR),
    "COMM_CONDITION_CODE" VARCHAR2(12 CHAR),
    "MAX_RECURSIVE_AMOUNT" NUMBER(11,2),
    "CREATE_DATETIME" TIMESTAMP (9) DEFAULT systimestamp NOT NULL,
    "CREATE_USER_ID" VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
    "MODIFY_DATETIME" TIMESTAMP (9),
    "AUDIT_TIMESTAMP" TIMESTAMP (9),
    "AUDIT_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_MODULE_NAME" VARCHAR2(65 CHAR),
    "AUDIT_CLIENT_USER_ID" VARCHAR2(64 CHAR),
    "AUDIT_CLIENT_IP_ADDRESS" VARCHAR2(39 CHAR),
    "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
    "AUDIT_ADDITIONAL_INFO" VARCHAR2(256 CHAR),
     CONSTRAINT "DEDUCTION_PROFILES_PK" PRIMARY KEY ("CASELOAD_ID", "DEDUCTION_TYPE"),
  );

  CREATE INDEX "DEDPROF_DEDTYPE_F2" ON "CASELOAD_DEDUCTION_PROFILES" ("DEDUCTION_TYPE");


  CREATE INDEX "CASELOAD_DEDUCTION_PROFILES_N2" ON "CASELOAD_DEDUCTION_PROFILES" ("PAYEE_CORPORATE_ID");


  CREATE INDEX "CASELOAD_DEDUCTION_PROFILES_NI" ON "CASELOAD_DEDUCTION_PROFILES" ("PAYEE_PERSON_ID");


  CREATE UNIQUE INDEX "CASELOAD_DEDUCTION_PROFILES_U1" ON "CASELOAD_DEDUCTION_PROFILES" ("CASELOAD_ID", "EXTERNAL_PRIORITY_NO", "INTERNAL_PRIORITY_NO");


  CREATE UNIQUE INDEX "DEDUCTION_PROFILES_PK" ON "CASELOAD_DEDUCTION_PROFILES" ("CASELOAD_ID", "DEDUCTION_TYPE");
