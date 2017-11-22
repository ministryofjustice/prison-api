CREATE TABLE OFFENDER_CONTACT_PERSONS
( OFFENDER_BOOK_ID NUMBER(10) NOT NULL,                      --OFFENDER_BOOK_ID IS 'System generated identifier for an offender booking.';
  PERSON_ID NUMBER(10),                                      --PERSON_ID IS 'System generated identider for a person.';
  CONTACT_TYPE VARCHAR2(12) NOT NULL,                        --CONTACT_TYPE IS 'The contact type with offender ie. Emergency, Professional..';
  RELATIONSHIP_TYPE VARCHAR2(12) NOT NULL,                   --RELATIONSHIP_TYPE IS 'The relationship with offender ie. Friend, Wife, Brother..';
  APPROVED_VISITOR_FLAG VARCHAR2(1) DEFAULT 'N',             --APPROVED_VISITOR_FLAG IS 'Is this person an approved visitor?';
  CASELOAD_TYPE VARCHAR2(12),                                --CASELOAD_TYPE IS 'Caseload Type';
  MODIFY_DATETIME TIMESTAMP DEFAULT SYSTIMESTAMP,            --MODIFY_DATETIME IS 'The timestamp when the record is modified ';
  MODIFY_USER_ID VARCHAR2(32),                               --MODIFY_USER_ID IS 'The user who modifies the record';
  COMMENT_TEXT VARCHAR2(240),                                --COMMENT_TEXT IS 'Comment Text';
  CASE_INFO_NUMBER VARCHAR2(60),                             --CASE_INFO_NUMBER IS 'Case Info Number';
  AWARE_OF_CHARGES_FLAG VARCHAR2(1),                         --AWARE_OF_CHARGES_FLAG IS 'Is the person aware of the charges';
  CAN_BE_CONTACTED_FLAG VARCHAR2(1),                         --CAN_BE_CONTACTED_FLAG IS 'Can be contacted ?';
  CREATE_DATETIME TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,   --CREATE_DATETIME IS 'The timestamp when the record is created';
  CREATE_USER_ID VARCHAR2(32) DEFAULT user NOT NULL,         --CREATE_USER_ID IS 'The user who creates the record';
  EMERGENCY_CONTACT_FLAG VARCHAR2(1) DEFAULT 'N' NOT NULL,   --EMERGENCY_CONTACT_FLAG IS 'Is the person emergency contact';
  NEXT_OF_KIN_FLAG VARCHAR2(1) DEFAULT 'N' NOT NULL,         --NEXT_OF_KIN_FLAG IS 'Is the person next of Kin';
  ACTIVE_FLAG VARCHAR2(1) DEFAULT 'N' NOT NULL,              --ACTIVE_FLAG IS 'Is it a active record';
  EXPIRY_DATE DATE,
  OFFENDER_CONTACT_PERSON_ID NUMBER(10) PRIMARY KEY,
  CONTACT_ROOT_OFFENDER_ID NUMBER(10),
  SEAL_FLAG VARCHAR2(1)
);

CREATE INDEX OFF_CP_CONT_PT_F1 ON OFFENDER_CONTACT_PERSONS (CONTACT_TYPE, RELATIONSHIP_TYPE);
CREATE INDEX OFFENDER_CONTACT_PERSONS_NI1 ON OFFENDER_CONTACT_PERSONS (PERSON_ID);
CREATE INDEX OFFENDER_CONTACT_PERSONS_NI2 ON OFFENDER_CONTACT_PERSONS (OFFENDER_BOOK_ID);
CREATE INDEX OFFENDER_CONTACT_PERSONS_NI3 ON OFFENDER_CONTACT_PERSONS (CONTACT_ROOT_OFFENDER_ID);
