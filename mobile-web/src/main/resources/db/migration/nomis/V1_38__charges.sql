CREATE TABLE OFFENCES (
  OFFENCE_CODE VARCHAR(25) NOT NULL,                     --'Reference Code ( OFFENCE )';
  STATUTE_CODE VARCHAR(12) NOT NULL,
  DESCRIPTION VARCHAR(1000) NOT NULL,
  OLD_STATUTE_CODE VARCHAR(12),                          --'Previous Statue Code';
  SEVERITY_RANKING VARCHAR(12),                          --'The severity ranking of Offence';
  DEFAULT_OFFENCE_TYPE VARCHAR(12),                      --'The default offence type';
  MAX_SENTENCE_LENGTH INTEGER,                           --'Maximum Sentence';
  SENTENCE_UNIT_CODE VARCHAR(12),                        --'Reference Code ( PERIOD_TYPE ) : Sentence unit of the max sentence';
  OFFENCE_GROUP VARCHAR(12),
  UPDATE_ALLOWED_FLAG VARCHAR(1) NOT NULL DEFAULT 'Y',
  REPEALED_DATE DATE,
  ACTIVE_FLAG VARCHAR(1)  NOT NULL DEFAULT 'Y',          --' The flag derives whether Offence is active or not';
  LIST_SEQ INTEGER,
  EXPIRY_DATE DATE,
  CREATE_USER_ID VARCHAR(32)  NOT NULL DEFAULT USER,     --'The user who creates the record';
  CHECK_BOX1 VARCHAR(1) DEFAULT 'N',
  CHECK_BOX2 VARCHAR(1) DEFAULT 'N',
  CHECK_BOX3 VARCHAR(1) DEFAULT 'N',
  OFFENSE_DEGREE VARCHAR(12),
  MAX_GOOD_TIME_PERC INTEGER,
  CREATE_DATE DATE  NOT NULL DEFAULT SYSDATE,            --'The date when Offence gets Created';
  MODIFY_USER_ID VARCHAR(32),                            --'The user who modifies the record';
  MODIFY_DATETIME TIMESTAMP,                             --'The timestamp when the record is modified ';
  HO_CODE VARCHAR(12),                                   --'This column stores the value of Home Office Class and Home Office sub class';
  CREATE_DATETIME TIMESTAMP  NOT NULL DEFAULT now(),     --'The timestamp when the record is created';
  AUDIT_TIMESTAMP TIMESTAMP, 
  AUDIT_USER_ID VARCHAR(32), 
  AUDIT_MODULE_NAME VARCHAR(65), 
  AUDIT_CLIENT_USER_ID VARCHAR(64), 
  AUDIT_CLIENT_IP_ADDRESS VARCHAR(39), 
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64), 
  AUDIT_ADDITIONAL_INFO VARCHAR(256)
);

CREATE INDEX OFN_STT_F2 ON OFFENCES (OLD_STATUTE_CODE);
CREATE INDEX OFN_STT_F1 ON OFFENCES (STATUTE_CODE);
CREATE INDEX OFFEN_HO_CODES_FK ON OFFENCES (HO_CODE);

ALTER TABLE OFFENCES ADD PRIMARY KEY (OFFENCE_CODE, STATUTE_CODE);

CREATE TABLE OFFENDER_CHARGES (
  OFFENDER_BOOK_ID BIGINT NOT NULL,                     --'The Offender Book ID';
  OFFENDER_CHARGE_ID BIGINT NOT NULL PRIMARY KEY,       --'PK The offender charge ID';
  STATUTE_CODE VARCHAR(12) NOT NULL,                    --'The Statute Code';
  OFFENCE_CODE VARCHAR(25) NOT NULL,                    --'The Offence Code';
  NO_OF_OFFENCES INTEGER,                               --'No of offences';
  OFFENCE_DATE DATE,                                    --'The date of the offence';
  OFFENCE_RANGE_DATE DATE,                              --'The end date of the offence';
  PLEA_CODE VARCHAR(12),                                --'The plea.  Reference Code(PLEA_STATUS)';
  PROPERTY_VALUE DECIMAL(10,2),                         --'The value of the property of the offence';
  TOTAL_PROPERTY_VALUE DECIMAL(10,2),                   --'The total value of the property of the offence';
  CJIT_OFFENCE_CODE_1 VARCHAR(21),
  CJIT_OFFENCE_CODE_2 VARCHAR(8),
  CJIT_OFFENCE_CODE_3 VARCHAR(3),
  CHARGE_STATUS VARCHAR(12),
  CREATE_USER_ID VARCHAR(32) NOT NULL DEFAULT USER,
  MODIFY_USER_ID VARCHAR(32),
  MODIFY_DATETIME TIMESTAMP,                            --'The timestamp when the record is modified ';
  CREATE_DATETIME TIMESTAMP NOT NULL DEFAULT now(),     --'The timestamp when the record is created';
  RESULT_CODE_1 VARCHAR(12),
  RESULT_CODE_2 VARCHAR(12),                            --'The result code 2 of the offender';
  RESULT_CODE_1_INDICATOR VARCHAR(12),                  --'The indicator of the result code 1';
  RESULT_CODE_2_INDICATOR VARCHAR(12),                  --'The indicator of the result code 2';
  CASE_ID BIGINT NOT NULL,                              --'The Offender Cases';
  MOST_SERIOUS_FLAG VARCHAR(1) NOT NULL DEFAULT 'N',    --'?The most serious offence out of all offences';
  CHARGE_SEQ INTEGER,
  ORDER_ID BIGINT,
  AUDIT_TIMESTAMP TIMESTAMP, 
  AUDIT_USER_ID VARCHAR(32), 
  AUDIT_MODULE_NAME VARCHAR(65), 
  AUDIT_CLIENT_USER_ID VARCHAR(64), 
  AUDIT_CLIENT_IP_ADDRESS VARCHAR(39), 
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64), 
  AUDIT_ADDITIONAL_INFO VARCHAR(256), 
  LIDS_OFFENCE_NUMBER INTEGER
);

CREATE INDEX OFFENDER_CHARGES_FK2 ON OFFENDER_CHARGES (RESULT_CODE_2);
CREATE INDEX OFFENDER_CHARGES_FK1 ON OFFENDER_CHARGES (RESULT_CODE_1);
CREATE INDEX OFF_CHG_OFN_FK ON OFFENDER_CHARGES (OFFENCE_CODE, STATUTE_CODE);
CREATE INDEX OFF_CHG_OFF_CASE_FK ON OFFENDER_CHARGES (CASE_ID);
CREATE INDEX OFFENDER_CHARGES_NI2 ON OFFENDER_CHARGES (CJIT_OFFENCE_CODE_1, CJIT_OFFENCE_CODE_2, CJIT_OFFENCE_CODE_3);
CREATE INDEX OFFENDER_CHARGES_NI1 ON OFFENDER_CHARGES (OFFENDER_BOOK_ID);
CREATE INDEX OFFENDER_CHARGES_NI3 ON OFFENDER_CHARGES (STATUTE_CODE, OFFENCE_CODE);
