
  CREATE TABLE "OFFENDER_CASE_NOTE_SENTS"
   (    "CASE_NOTE_ID" NUMBER(10,0) NOT NULL,
    "OFFENDER_BOOK_ID" NUMBER(10,0) NOT NULL,
    "SENTENCE_SEQ" NUMBER(6,0) NOT NULL,
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
     CONSTRAINT "OFFENDER_CASE_NOTE_SENTS_PK" PRIMARY KEY ("CASE_NOTE_ID", "OFFENDER_BOOK_ID", "SENTENCE_SEQ")
  );

  CREATE INDEX "OFF_CN_SENT_OFF_SENT_FK" ON "OFFENDER_CASE_NOTE_SENTS" ("OFFENDER_BOOK_ID", "SENTENCE_SEQ");


  CREATE UNIQUE INDEX "OFFENDER_CASE_NOTE_SENTS_PK" ON "OFFENDER_CASE_NOTE_SENTS" ("CASE_NOTE_ID", "OFFENDER_BOOK_ID", "SENTENCE_SEQ");


  CREATE INDEX "OFFENDER_CASE_NOTE_SENTS_FK9" ON "OFFENDER_CASE_NOTE_SENTS" ("OFFENDER_BOOK_ID");
