
  CREATE TABLE "OFFENDER_SENT_CONDITIONS"
   (    "SENTENCE_SEQ" NUMBER(6,0) NOT NULL,
    "COMM_CONDITION_TYPE" VARCHAR2(12 CHAR) NOT NULL,
    "COMM_CONDITION_CODE" VARCHAR2(12 CHAR) NOT NULL,
    "START_DATE" DATE,
    "CONDITION_STATUS" VARCHAR2(12 CHAR),
    "STATUS_DATE" DATE,
    "OFFENDER_BOOK_ID" NUMBER(10,0) NOT NULL,
    "EXPIRY_DATE" DATE,
    "LIST_SEQ" NUMBER(3,0),
    "COMMENT_TEXT" VARCHAR2(240 CHAR),
    "CURFEW_START_TIME" DATE,
    "CURFEW_END_TIME" DATE,
    "CONDITION_RECOMMENDED_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "GOVERNOR_CONDITION_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "LENGTH" NUMBER(6,0),
    "LENGTH_UNIT" VARCHAR2(12 CHAR),
    "DETAILS_TEXT" VARCHAR2(240 CHAR),
    "OFFENDER_SENT_CONDITION_ID" NUMBER(10,0) NOT NULL,
    "CURFEW_PROVIDER" VARCHAR2(12 CHAR),
    "EXCLUSION_CODE" VARCHAR2(12 CHAR),
    "RESIDENCY_ADDRESS_ID" NUMBER(10,0),
    "MENTAL_HEALTH_PROVIDER" VARCHAR2(12 CHAR),
    "ALCOHOL_TREATMENT_PROVIDER" VARCHAR2(12 CHAR),
    "ATTENDANCE_CENTRE" VARCHAR2(12 CHAR),
    "CREATE_DATETIME" TIMESTAMP (9) DEFAULT systimestamp NOT NULL,
    "CREATE_USER_ID" VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
    "MODIFY_DATETIME" TIMESTAMP (9),
    "MODIFY_USER_ID" VARCHAR2(32 CHAR),
    "CONDITION_REQUIRED_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "CONDITION_APPLIED_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL,
    "LONG_COMMENT_TEXT" VARCHAR2(4000 CHAR),
    "APPOINTMENT_PERSON_NAME" VARCHAR2(240 CHAR),
    "REVIEW_CODE" VARCHAR2(12 CHAR),
    "SUPERVISOR_NAME" VARCHAR2(240 CHAR),
    "REPORT_TIME" DATE,
    "REPORT_DATE" DATE,
    "PERSONAL_RELATIONSHIP_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "VEHICLE_DETAILS_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "NON_ASSOCIATED_OFFENDERS" VARCHAR2(240 CHAR),
    "DRUG_TESTING" VARCHAR2(240 CHAR),
    "TERMINATION_DATE" DATE,
    "STATUS_REASON_CODE" VARCHAR2(12 CHAR),
    "NO_RESIDENT_UNDER_AGE_OF" NUMBER(6,0),
    "PROHIBITED_CONTACT" VARCHAR2(240 CHAR),
    "RESTRICTED_CHILD_AGE_OF" NUMBER(6,0),
    "RESTRICTED_APPROVAL_PERSON" VARCHAR2(240 CHAR),
    "CURFEW_TAGGING_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "OTHER_PROGRAM" VARCHAR2(240 CHAR),
    "NO_WORK_WITH_UNDER_AGE" VARCHAR2(1 CHAR) DEFAULT 'N',
    "NO_WORK_WITH_UNDER_AGE_OF" NUMBER(3,0),
    "NO_ACCESS_TO_INTERNET" VARCHAR2(1 CHAR) DEFAULT 'N',
    "NO_USER_OF_COMPUTER" VARCHAR2(1 CHAR) DEFAULT 'N',
    "STATUS_UPDATE_REASON" VARCHAR2(12 CHAR),
    "STATUS_UPDATE_COMMENT" VARCHAR2(400 CHAR),
    "STATUS_UPDATE_DATE" DATE,
    "STATUS_UPDATE_STAFF_ID" NUMBER(10,0),
    "AUDIT_TIMESTAMP" TIMESTAMP (9),
    "AUDIT_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_MODULE_NAME" VARCHAR2(65 CHAR),
    "AUDIT_CLIENT_USER_ID" VARCHAR2(64 CHAR),
    "AUDIT_CLIENT_IP_ADDRESS" VARCHAR2(39 CHAR),
    "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
    "AUDIT_ADDITIONAL_INFO" VARCHAR2(256 CHAR),
    "WORKFLOW_ID" NUMBER(32,0),
    "ACTIVITY_CODE" VARCHAR2(12 CHAR),
    "COND_ACT_TYPE" VARCHAR2(12 CHAR),
    "ACTIVITY_STATUS" VARCHAR2(12 CHAR),
    "PROGRAM_ID" NUMBER(10,0),
    "PRACTITIONER_TYPE" VARCHAR2(12 CHAR),
    "HOME_VISITOR_NAME" VARCHAR2(240 CHAR),
    "MOBILE_SIM_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "MOBILE_CAMERA_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "CAMERA_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "PRIOR_APPROVAL" VARCHAR2(240 CHAR),
    "PROBLEM_TYPE" VARCHAR2(12 CHAR),
    "POLICE_ESCORT_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "SEX_OFFENDER_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "PRISONER_ASSOCIATION_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "ORGANISATION_NAME" VARCHAR2(240 CHAR) DEFAULT 'N',
    "PLACE_OF_WORSHIP_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "PUBLIC_SPEAKING_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "WRITTEN_MATERIAL_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "GROOMING_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "PASSPORT_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "NOTIFY_RELATIONSHIPS" VARCHAR2(12 CHAR),
    "DEVICE_INSPECTION_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "NO_RESIDENT_GENDER_OF" VARCHAR2(12 CHAR),
    "PROHIBITED_CONTACT_GENDER_OF" VARCHAR2(12 CHAR),
    "CONDITION_DATE" DATE,
    "CONDITION_TIME" DATE,
     CONSTRAINT "OFFENDER_SENT_CONDITIONS_PK" PRIMARY KEY ("OFFENDER_SENT_CONDITION_ID"),
     CONSTRAINT "OFFENDER_SENT_CONDITIONS_FK1" FOREIGN KEY ("PROGRAM_ID")
      REFERENCES "PROGRAM_SERVICES" ("PROGRAM_ID"),
     CONSTRAINT "OFFENDER_SENT_CONDITIONS_FK9" FOREIGN KEY ("OFFENDER_BOOK_ID")
      REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID"),
     CONSTRAINT "OFF_SENT_COND_OFF_SENT_FK" FOREIGN KEY ("OFFENDER_BOOK_ID", "SENTENCE_SEQ")
      REFERENCES "OFFENDER_SENTENCES" ("OFFENDER_BOOK_ID", "SENTENCE_SEQ")
  );

  CREATE INDEX "OFFENDER_SENT_CONDITIONS_FK1" ON "OFFENDER_SENT_CONDITIONS" ("PROGRAM_ID");


  CREATE INDEX "OFFENDER_SENT_CONDITIONS_NI1" ON "OFFENDER_SENT_CONDITIONS" ("OFFENDER_BOOK_ID", "SENTENCE_SEQ");


  CREATE INDEX "OFFENDER_SENT_CONDITIONS_NI2" ON "OFFENDER_SENT_CONDITIONS" ("COMM_CONDITION_TYPE", "COMM_CONDITION_CODE");


  CREATE UNIQUE INDEX "OFFENDER_SENT_CONDITIONS_PK" ON "OFFENDER_SENT_CONDITIONS" ("OFFENDER_SENT_CONDITION_ID");
