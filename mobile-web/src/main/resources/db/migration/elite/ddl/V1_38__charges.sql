CREATE TABLE OFFENCES (
  OFFENCE_CODE VARCHAR2(25) NOT NULL,                         --'Reference Code ( OFFENCE )';
  STATUTE_CODE VARCHAR2(12) NOT NULL,
  DESCRIPTION VARCHAR2(1000) NOT NULL,
  OLD_STATUTE_CODE VARCHAR2(12),                              --'Previous Statue Code';
  SEVERITY_RANKING VARCHAR2(12),                              --'The severity ranking of Offence';
  DEFAULT_OFFENCE_TYPE VARCHAR2(12),                          --'The default offence type';
  MAX_SENTENCE_LENGTH NUMBER(3),                              --'Maximum Sentence';
  SENTENCE_UNIT_CODE VARCHAR2(12),                            --'Reference Code ( PERIOD_TYPE ) : Sentence unit of the max sentence';
  OFFENCE_GROUP VARCHAR2(12),
  UPDATE_ALLOWED_FLAG VARCHAR2(1) DEFAULT 'Y' NOT NULL,
  REPEALED_DATE DATE,
  ACTIVE_FLAG VARCHAR2(1) DEFAULT 'Y' NOT NULL,               --' The flag derives whether Offence is active or not';
  LIST_SEQ NUMBER(6),
  EXPIRY_DATE DATE,
  CHECK_BOX1 VARCHAR2(1) DEFAULT 'N',
  CHECK_BOX2 VARCHAR2(1) DEFAULT 'N',
  CHECK_BOX3 VARCHAR2(1) DEFAULT 'N',
  OFFENSE_DEGREE VARCHAR2(12),
  MAX_GOOD_TIME_PERC NUMBER(3),
  CREATE_DATE DATE DEFAULT SYSDATE NOT NULL,                  --'The date when Offence gets Created';
  HO_CODE VARCHAR2(12),                                       --'This column stores the value of Home Office Class and Home Office sub class';
  SEAL_FLAG VARCHAR2(1), 
  CREATE_DATETIME TIMESTAMP (9) DEFAULT systimestamp NOT NULL,--'The timestamp when the record is created';
  CREATE_USER_ID VARCHAR2(32) DEFAULT USER NOT NULL,          --'The user who creates the record';
  MODIFY_DATETIME TIMESTAMP (9),                              --'The timestamp when the record is modified ';
  MODIFY_USER_ID VARCHAR2(32),                                --'The user who modifies the record';
  CONSTRAINT OFFENCES_PK PRIMARY KEY (OFFENCE_CODE, STATUTE_CODE)
);

CREATE INDEX OFN_STT_F2 ON OFFENCES (OLD_STATUTE_CODE);
CREATE INDEX OFN_STT_F1 ON OFFENCES (STATUTE_CODE);
CREATE INDEX OFFEN_HO_CODES_FK ON OFFENCES (HO_CODE);

CREATE TABLE OFFENDER_CHARGES (
  OFFENDER_BOOK_ID NUMBER(10) NOT NULL,                       --'The Offender Book ID';
  OFFENDER_CHARGE_ID NUMBER(10) NOT NULL PRIMARY KEY,         --'PK The offender charge ID';
  STATUTE_CODE VARCHAR2(12) NOT NULL,                         --'The Statute Code';
  OFFENCE_CODE VARCHAR2(25) NOT NULL,                         --'The Offence Code';
  NO_OF_OFFENCES NUMBER(3),                                   --'No of offences';
  OFFENCE_DATE DATE,                                          --'The date of the offence';
  OFFENCE_RANGE_DATE DATE,                                    --'The end date of the offence';
  PLEA_CODE VARCHAR2(12),                                     --'The plea.  Reference Code(PLEA_STATUS)';
  PROPERTY_VALUE NUMBER(10,2),                                --'The value of the property of the offence';
  TOTAL_PROPERTY_VALUE NUMBER(10,2),                          --'The total value of the property of the offence';
  CJIT_OFFENCE_CODE_1 VARCHAR2(21),
  CJIT_OFFENCE_CODE_2 VARCHAR2(8),
  CJIT_OFFENCE_CODE_3 VARCHAR2(3),
  CHARGE_STATUS VARCHAR2(12),                                 --'The charge status.  Reference Code(CHARGE_STS)'
  CREATE_USER_ID VARCHAR2(32) DEFAULT USER NOT NULL,
  MODIFY_USER_ID VARCHAR2(32),
  MODIFY_DATETIME TIMESTAMP (9),                              --'The timestamp when the record is modified ';
  CREATE_DATETIME TIMESTAMP (9) DEFAULT systimestamp NOT NULL,--'The timestamp when the record is created';
  RESULT_CODE_1 VARCHAR2(12),                                 --'The result code 1 of the offender';
  RESULT_CODE_2 VARCHAR2(12),                                 --'The result code 2 of the offender';
  RESULT_CODE_1_INDICATOR VARCHAR2(12),                       --'The indicator of the result code 1';
  RESULT_CODE_2_INDICATOR VARCHAR2(12),                       --'The indicator of the result code 2';
  CASE_ID NUMBER(10) NOT NULL,                                --'The Offender Cases';
  MOST_SERIOUS_FLAG VARCHAR2(1) DEFAULT 'N' NOT NULL,         --'?The most serious offence out of all offences';
  CHARGE_SEQ NUMBER(6),
  ORDER_ID NUMBER(6),
  LIDS_OFFENCE_NUMBER NUMBER(6),
  OFFENCE_TYPE VARCHAR2(12), 
  SEAL_FLAG VARCHAR2(1)
);

CREATE INDEX OFFENDER_CHARGES_FK2 ON OFFENDER_CHARGES (RESULT_CODE_2);
CREATE INDEX OFFENDER_CHARGES_FK1 ON OFFENDER_CHARGES (RESULT_CODE_1);
CREATE INDEX OFF_CHG_OFN_FK ON OFFENDER_CHARGES (OFFENCE_CODE, STATUTE_CODE);
CREATE INDEX OFF_CHG_OFF_CASE_FK ON OFFENDER_CHARGES (CASE_ID);
CREATE INDEX OFFENDER_CHARGES_NI2 ON OFFENDER_CHARGES (CJIT_OFFENCE_CODE_1, CJIT_OFFENCE_CODE_2, CJIT_OFFENCE_CODE_3);
CREATE INDEX OFFENDER_CHARGES_NI1 ON OFFENDER_CHARGES (OFFENDER_BOOK_ID);
CREATE INDEX OFFENDER_CHARGES_NI3 ON OFFENDER_CHARGES (STATUTE_CODE, OFFENCE_CODE);
