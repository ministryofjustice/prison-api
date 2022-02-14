
  CREATE TABLE "OFFENDER_TEST_SELECTIONS"
   (    "OFFENDER_BOOK_ID" NUMBER(10,0) NOT NULL,
    "RTP_ID" NUMBER(10,0) NOT NULL,
    "TEST_SELECTION_TYPE" VARCHAR2(1 CHAR) NOT NULL,
    "TEST_SELECTION_NO" NUMBER(5,0) NOT NULL,
    "TESTED_FLAG" VARCHAR2(1 CHAR) DEFAULT NULL,
    "REASON_NOT_TESTED" VARCHAR2(12 CHAR),
    "NOTES" VARCHAR2(240 CHAR),
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
     CONSTRAINT "OFF_TEST_SEL_TESTED_CHK" CHECK (tested_flag IN ('Y', 'N')),
     CONSTRAINT "OFF_TEST_SEL_TYPE_CHK" CHECK (test_selection_type IN ('M', 'R')),
     CONSTRAINT "OFF_TEST_SEL_PK" PRIMARY KEY ("OFFENDER_BOOK_ID", "RTP_ID")
  );

  CREATE INDEX "OFFENDER_TEST_SELECTIONS_NI1" ON "OFFENDER_TEST_SELECTIONS" ("RTP_ID");


  CREATE UNIQUE INDEX "OFF_TEST_SEL_PK" ON "OFFENDER_TEST_SELECTIONS" ("OFFENDER_BOOK_ID", "RTP_ID");
