
  CREATE TABLE "OFFENDER_BENEFICIARIES"
   (    "BENEFICIARY_ID" NUMBER(10,0) NOT NULL,
    "OFFENDER_DEDUCTION_ID" NUMBER(10,0) NOT NULL,
    "OFFENDER_ID" NUMBER(10,0) NOT NULL,
    "PERSON_ID" NUMBER(10,0),
    "CORPORATE_ID" NUMBER(10,0),
    "PRIORITY" NUMBER(3,0),
    "AMOUNT" NUMBER(11,2),
    "PERCENT" NUMBER(3,0),
    "OVERRIDE_AMOUNT" NUMBER(11,2),
    "RECEIVED_AMOUNT" NUMBER(11,2),
    "MODIFY_USER_ID" VARCHAR2(32 CHAR),
    "MODIFY_DATETIME" TIMESTAMP (9),
    "UNKNOWN_BEN_ID" NUMBER(10,0),
    "COMMENT_TEXT" VARCHAR2(240 CHAR),
    "MONTHLY_AMOUNT" NUMBER(11,2),
    "RECURSIVE_AMOUNT" NUMBER(11,2),
    "TBD_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "CREATE_DATETIME" TIMESTAMP (9) DEFAULT systimestamp NOT NULL,
    "CREATE_USER_ID" VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
    "AUDIT_TIMESTAMP" TIMESTAMP (9),
    "AUDIT_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_MODULE_NAME" VARCHAR2(65 CHAR),
    "AUDIT_CLIENT_USER_ID" VARCHAR2(64 CHAR),
    "AUDIT_CLIENT_IP_ADDRESS" VARCHAR2(39 CHAR),
    "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
    "AUDIT_ADDITIONAL_INFO" VARCHAR2(256 CHAR),
     CONSTRAINT OFF_BENEFICRY_PK PRIMARY KEY (BENEFICIARY_ID),
     CONSTRAINT "OFF_BNC_CK1" CHECK (    PERSON_ID IS NOT NULL        OR         CORPORATE_ID IS NOT NULL        OR         UNKNOWN_BEN_ID IS NOT NULL       ),
     CONSTRAINT "OFF_BENE_UK1" UNIQUE ("OFFENDER_DEDUCTION_ID", "PERSON_ID", "CORPORATE_ID"),
     CONSTRAINT OFFENDER_BENEFICIARIES_FK10 FOREIGN KEY (OFFENDER_ID) REFERENCES OFFENDERS(OFFENDER_ID),
     CONSTRAINT OFF_BNC_CORP_F1 FOREIGN KEY (CORPORATE_ID) REFERENCES CORPORATES(CORPORATE_ID),
     CONSTRAINT OFF_BNC_OFF_DED_F1 FOREIGN KEY (OFFENDER_DEDUCTION_ID) REFERENCES OFFENDER_DEDUCTIONS(OFFENDER_DEDUCTION_ID),
     CONSTRAINT OFF_BNC_PER_F1 FOREIGN KEY (PERSON_ID) REFERENCES PERSONS(PERSON_ID)
  );

  CREATE INDEX "OFFENDER_BENEFICIARIES_NI1" ON "OFFENDER_BENEFICIARIES" ("OFFENDER_ID");
  CREATE INDEX "OFFENDER_BENEFICIARIES_NI2" ON "OFFENDER_BENEFICIARIES" ("CORPORATE_ID");
  CREATE INDEX "OFFENDER_BENEFICIARIES_NI3" ON "OFFENDER_BENEFICIARIES" ("PERSON_ID");
  CREATE INDEX "OFFENDER_BENEFICIARIES_NI4" ON "OFFENDER_BENEFICIARIES" ("OFFENDER_DEDUCTION_ID", "CORPORATE_ID");
  CREATE UNIQUE INDEX "OFF_BENEFICRY_PK" ON "OFFENDER_BENEFICIARIES" ("BENEFICIARY_ID");
  CREATE UNIQUE INDEX "OFF_BENE_UK1" ON "OFFENDER_BENEFICIARIES" ("OFFENDER_DEDUCTION_ID", "PERSON_ID", "CORPORATE_ID");
