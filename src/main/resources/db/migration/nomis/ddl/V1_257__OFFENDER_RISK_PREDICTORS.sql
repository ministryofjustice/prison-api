
  CREATE TABLE "OFFENDER_RISK_PREDICTORS"
   (    "OFFENDER_RISK_PREDICTOR_ID" NUMBER(10,0) NOT NULL,
    "OFFENDER_BOOK_ID" NUMBER(10,0) NOT NULL,
    "FIRST_CONVICTION_AGE" NUMBER(6,0),
    "CURRENT_CONVICTION_AGE" NUMBER(6,0),
    "NO_OF_SENTENCES_BEFORE_21" NUMBER(6,0),
    "NO_OF_SENTENCES_OVER_21" NUMBER(6,0),
    "CURRENT_VIOLENT_OFFENCE_FLAG" VARCHAR2(1 CHAR) DEFAULT N,
    "CURRENT_SEXUAL_OFFENCE_FLAG" VARCHAR2(1 CHAR) DEFAULT N,
    "CURRENT_OTHER_OFFENCE_FLAG" VARCHAR2(1 CHAR) DEFAULT N,
    "NO_OF_COURTS_VIOLENT_OFFENCE" NUMBER(6,0),
    "NO_OF_COURTS_SEXUAL_OFFENCE" NUMBER(6,0),
    "NO_OF_COURTS_OTHER_OFFENCE" NUMBER(6,0),
    "NO_OF_COURTS_CONVICTED" NUMBER(6,0),
    "CURRENT_OFFENCE_TYPE" VARCHAR2(12 CHAR),
    "RISK_OF_VIOLENCE_OFFENCE" NUMBER(6,2),
    "RISK_OF_SEXUAL_OFFENCE" NUMBER(6,2),
    "RISK_OF_OTHER_OFFENCE" NUMBER(6,2),
    "RISK_OF_IMPRISONMENT" NUMBER(6,2),
    "RECORD_DATE" DATE NOT NULL,
    "RECORD_STAFF_ID" NUMBER(10,0) NOT NULL,
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
    "TOTAL_SENTENCE" NUMBER(10,0),
     CONSTRAINT "OFFENDER_RISK_PREDICTORS_PK" PRIMARY KEY ("OFFENDER_RISK_PREDICTOR_ID"),
     CONSTRAINT "OFF_RSIK_PRED_OFF_BKG_FK" FOREIGN KEY ("OFFENDER_BOOK_ID")
      REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID")
  );

  CREATE INDEX "OFFENDER_RISK_PREDICTORS_NI1" ON "OFFENDER_RISK_PREDICTORS" ("OFFENDER_BOOK_ID");


  CREATE UNIQUE INDEX "OFFENDER_RISK_PREDICTORS_PK" ON "OFFENDER_RISK_PREDICTORS" ("OFFENDER_RISK_PREDICTOR_ID");
