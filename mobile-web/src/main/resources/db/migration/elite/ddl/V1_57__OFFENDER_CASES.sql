CREATE TABLE OFFENDER_CASES
(
  CASE_ID                NUMBER(10)                                 NOT NULL ,
  OFFENDER_BOOK_ID       NUMBER(10)                                 NOT NULL ,
  CASE_INFO_NUMBER       VARCHAR2(13),
  CASE_TYPE              VARCHAR2(12),
  CASE_STATUS            VARCHAR2(12),
  COMBINED_CASE_ID       NUMBER(10)  ,
  MODIFY_DATETIME        TIMESTAMP(9)        DEFAULT SYSTIMESTAMP,
  MODIFY_USER_ID         VARCHAR2(32),
  BEGIN_DATE             DATE,
  AGY_LOC_ID             VARCHAR2(6)                                NOT NULL,
  CREATE_USER_ID         VARCHAR2(32)         DEFAULT user          NOT NULL,
  CREATE_DATETIME        TIMESTAMP(9)        DEFAULT SYSTIMESTAMP   NOT NULL,
  CASE_INFO_PREFIX       VARCHAR2(12),
  VICTIM_LIAISON_UNIT    VARCHAR2(12),
  STATUS_UPDATE_REASON   VARCHAR2(12),
  STATUS_UPDATE_COMMENT  VARCHAR2(400),
  STATUS_UPDATE_DATE     DATE,
  STATUS_UPDATE_STAFF_ID NUMBER(10),
  LIDS_CASE_NUMBER       NUMBER(10),
  CASE_SEQ               NUMBER(6)                                  NOT NULL,
  SEAL_FLAG              VARCHAR2(1),

  CONSTRAINT OFFENDER_CASES_PK PRIMARY KEY(CASE_ID),

  CONSTRAINT OFF_CS_OFF_BKG_FK FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS(OFFENDER_BOOK_ID),
  CONSTRAINT OFF_CS_OFF_CS_F1  FOREIGN KEY (COMBINED_CASE_ID) REFERENCES OFFENDER_CASES(CASE_ID)
);

CREATE UNIQUE INDEX OFFENDER_CASES_UK on OFFENDER_CASES (OFFENDER_BOOK_ID, CASE_SEQ);

create index OFFENDER_CASES_NI2 on OFFENDER_CASES (CASE_INFO_NUMBER);

create index OFFENDER_CASES_NI4 on OFFENDER_CASES (COMBINED_CASE_ID);

create index OFFENDER_CASES_NI3 on OFFENDER_CASES (AGY_LOC_ID);



comment on table OFFENDER_CASES is 'The Offender Cases';

comment on column OFFENDER_CASES.CASE_ID is 'The Case ID';

comment on column OFFENDER_CASES.OFFENDER_BOOK_ID is 'The Ofender Booking ID';

comment on column OFFENDER_CASES.CASE_INFO_NUMBER is 'The case Info number';

comment on column OFFENDER_CASES.CASE_TYPE is 'The case type.  Reference Codes(LEG_CASE_TYP)';

comment on column OFFENDER_CASES.CASE_STATUS is 'The status of the case';

comment on column OFFENDER_CASES.COMBINED_CASE_ID is 'The combined case ID';

comment on column OFFENDER_CASES.MODIFY_DATETIME is 'The timestamp when the record is modified';

comment on column OFFENDER_CASES.MODIFY_USER_ID is 'The user who modifies the record';

comment on column OFFENDER_CASES.BEGIN_DATE is 'The Begin date';

comment on column OFFENDER_CASES.AGY_LOC_ID is 'The court agency location ID';

comment on column OFFENDER_CASES.CREATE_USER_ID is 'The user who creates the record';

comment on column OFFENDER_CASES.CREATE_DATETIME is 'The timestamp when the record is created';

comment on column OFFENDER_CASES.CASE_INFO_PREFIX is 'The Prefix of the case number';

comment on column OFFENDER_CASES.VICTIM_LIAISON_UNIT is 'The liaison unit in charge of the case';

comment on column OFFENDER_CASES.STATUS_UPDATE_REASON is 'The reason of status updated';

comment on column OFFENDER_CASES.STATUS_UPDATE_COMMENT is 'The comment of status updated';

comment on column OFFENDER_CASES.STATUS_UPDATE_DATE is 'The date of status updated';

comment on column OFFENDER_CASES.STATUS_UPDATE_STAFF_ID is 'The staff who perform of status updated';

comment on column OFFENDER_CASES.CASE_SEQ is 'The case seq no for the offender';

