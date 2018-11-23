CREATE TABLE OFFENDER_CHARGES (
  OFFENDER_BOOK_ID        NUMBER(10)                          NOT NULL,
  OFFENDER_CHARGE_ID      NUMBER(10)                          NOT NULL,
  STATUTE_CODE            VARCHAR2(12)                        NOT NULL,
  OFFENCE_CODE            VARCHAR2(25)                        NOT NULL,
  NO_OF_OFFENCES          NUMBER(3),
  OFFENCE_DATE            DATE,
  OFFENCE_RANGE_DATE      DATE,
  PLEA_CODE               VARCHAR2(12),
  PROPERTY_VALUE          NUMBER(10,2),
  TOTAL_PROPERTY_VALUE    NUMBER(10,2),
  CJIT_OFFENCE_CODE_1     VARCHAR2(21),
  CJIT_OFFENCE_CODE_2     VARCHAR2(8),
  CJIT_OFFENCE_CODE_3     VARCHAR2(3),
  CHARGE_STATUS           VARCHAR2(12),
  CREATE_USER_ID          VARCHAR2(32)   DEFAULT USER         NOT NULL,
  MODIFY_USER_ID          VARCHAR2(32),
  MODIFY_DATETIME         TIMESTAMP (9),
  CREATE_DATETIME         TIMESTAMP (9)  DEFAULT systimestamp NOT NULL,
  RESULT_CODE_1           VARCHAR2(12),
  RESULT_CODE_2           VARCHAR2(12),
  RESULT_CODE_1_INDICATOR VARCHAR2(12),
  RESULT_CODE_2_INDICATOR VARCHAR2(12),
  CASE_ID                 NUMBER(10)                          NOT NULL,
  MOST_SERIOUS_FLAG       VARCHAR2(1)    DEFAULT 'N'          NOT NULL,
  CHARGE_SEQ              NUMBER(6),
  ORDER_ID                NUMBER(6),
  LIDS_OFFENCE_NUMBER     NUMBER(6),
  OFFENCE_TYPE            VARCHAR2(12),
  SEAL_FLAG               VARCHAR2(1),

  CONSTRAINT OFFENDER_CHARGES_PK PRIMARY KEY (OFFENDER_CHARGE_ID),

  CONSTRAINT OFF_CHG_OFF_BKG_FK  FOREIGN KEY (OFFENDER_BOOK_ID)           REFERENCES OFFENDER_BOOKINGS(OFFENDER_BOOK_ID),
  CONSTRAINT OFF_CHG_OFF_CASE_FK FOREIGN KEY (CASE_ID)                    REFERENCES OFFENDER_CASES(CASE_ID),
  CONSTRAINT OFF_CHG_OFN_FK      FOREIGN KEY (OFFENCE_CODE, STATUTE_CODE) REFERENCES OFFENCES(OFFENCE_CODE, STATUTE_CODE)
);

CREATE INDEX OFFENDER_CHARGES_FK2 ON OFFENDER_CHARGES (RESULT_CODE_2);
CREATE INDEX OFFENDER_CHARGES_FK1 ON OFFENDER_CHARGES (RESULT_CODE_1);
CREATE INDEX OFF_CHG_OFN_FK ON OFFENDER_CHARGES (OFFENCE_CODE, STATUTE_CODE);
CREATE INDEX OFF_CHG_OFF_CASE_FK ON OFFENDER_CHARGES (CASE_ID);
CREATE INDEX OFFENDER_CHARGES_NI2 ON OFFENDER_CHARGES (CJIT_OFFENCE_CODE_1, CJIT_OFFENCE_CODE_2, CJIT_OFFENCE_CODE_3);
CREATE INDEX OFFENDER_CHARGES_NI1 ON OFFENDER_CHARGES (OFFENDER_BOOK_ID);
CREATE INDEX OFFENDER_CHARGES_NI3 ON OFFENDER_CHARGES (STATUTE_CODE, OFFENCE_CODE);

   
comment on table OFFENDER_CHARGES is 'The offences charged';
comment on column OFFENDER_CHARGES.OFFENDER_BOOK_ID is 'The Offender Book ID';
comment on column OFFENDER_CHARGES.OFFENDER_CHARGE_ID is 'PK The offender charge ID';
comment on column OFFENDER_CHARGES.STATUTE_CODE is 'The Statute Code';
comment on column OFFENDER_CHARGES.OFFENCE_CODE is 'The Offence Code';
comment on column OFFENDER_CHARGES.NO_OF_OFFENCES is 'No of offences';
comment on column OFFENDER_CHARGES.OFFENCE_DATE is 'The date of the offence';
comment on column OFFENDER_CHARGES.OFFENCE_RANGE_DATE is 'The end date of the offence';
comment on column OFFENDER_CHARGES.PLEA_CODE is 'The plea.  Reference Code(PLEA_STATUS)';
comment on column OFFENDER_CHARGES.PROPERTY_VALUE is 'The value of the property of the offence';
comment on column OFFENDER_CHARGES.TOTAL_PROPERTY_VALUE is 'The total value of the property of the offence';
comment on column OFFENDER_CHARGES.CJIT_OFFENCE_CODE_1 is 'CJIT Code 1';
comment on column OFFENDER_CHARGES.CJIT_OFFENCE_CODE_2 is 'CJIT Code 2';
comment on column OFFENDER_CHARGES.CJIT_OFFENCE_CODE_3 is 'CJIT Code 3';
comment on column OFFENDER_CHARGES.CHARGE_STATUS is 'The charge status.  Reference Code(CHARGE_STS)';
comment on column OFFENDER_CHARGES.CREATE_USER_ID is 'The user who creates the record';
comment on column OFFENDER_CHARGES.MODIFY_USER_ID is 'The user who modifies the record';
comment on column OFFENDER_CHARGES.MODIFY_DATETIME is 'The timestamp when the record is modified';
comment on column OFFENDER_CHARGES.CREATE_DATETIME is 'The timestamp when the record is created';
comment on column OFFENDER_CHARGES.RESULT_CODE_1 is 'The result Code 1of the offender';
comment on column OFFENDER_CHARGES.RESULT_CODE_2 is 'The result code 2 of the offender';
comment on column OFFENDER_CHARGES.RESULT_CODE_1_INDICATOR is 'The indicator of the result code 1';
comment on column OFFENDER_CHARGES.RESULT_CODE_2_INDICATOR is 'The indicator of the result code 2';
comment on column OFFENDER_CHARGES.CASE_ID is 'The Offender Cases';
comment on column OFFENDER_CHARGES.MOST_SERIOUS_FLAG is '?The most serious offence out of all offences'