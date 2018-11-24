create table COURT_EVENT_CHARGES
(
  EVENT_ID NUMBER(10) not null
    constraint CRT_EVENT_CHG_CRT_EVENT_FK
    references COURT_EVENTS,
  OFFENDER_CHARGE_ID NUMBER(10) not null
    constraint COURT_EVENT_CHG_OFF_CHG_FK
    references OFFENDER_CHARGES,
  PLEA_CODE VARCHAR2(12 char),
  RESULT_CODE_1 VARCHAR2(12 char),
  RESULT_CODE_2 VARCHAR2(12 char),
  RESULT_CODE_1_INDICATOR VARCHAR2(12 char),
  RESULT_CODE_2_INDICATOR VARCHAR2(12 char),
  MOST_SERIOUS_FLAG VARCHAR2(1 char) default 'N' not null,
  CREATE_DATETIME TIMESTAMP(9) default systimestamp not null,
  CREATE_USER_ID VARCHAR2(32 char) default USER not null,
  MODIFY_DATETIME TIMESTAMP(9),
  MODIFY_USER_ID VARCHAR2(32 char),
  PROPERTY_VALUE NUMBER(10,2),
  TOTAL_PROPERTY_VALUE NUMBER(10,2),
  NO_OF_OFFENCES NUMBER(3),
  OFFENCE_DATE DATE,
  OFFENCE_RANGE_DATE DATE,
  CJIT_OFFENCE_CODE_1 VARCHAR2(21 char),
  CJIT_OFFENCE_CODE_2 VARCHAR2(8 char),
  CJIT_OFFENCE_CODE_3 VARCHAR2(3 char),
  AUDIT_TIMESTAMP TIMESTAMP(9),
  AUDIT_USER_ID VARCHAR2(32 char),
  AUDIT_MODULE_NAME VARCHAR2(65 char),
  AUDIT_CLIENT_USER_ID VARCHAR2(64 char),
  AUDIT_CLIENT_IP_ADDRESS VARCHAR2(39 char),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
  AUDIT_ADDITIONAL_INFO VARCHAR2(256 char),
  constraint COURT_EVENT_CHARGES_PK
  primary key (EVENT_ID, OFFENDER_CHARGE_ID)
);

comment on table COURT_EVENT_CHARGES is 'The outcome of the offences in a court event';

comment on column COURT_EVENT_CHARGES.EVENT_ID is 'The court event ID';

comment on column COURT_EVENT_CHARGES.OFFENDER_CHARGE_ID is 'The Offender charge ID';

comment on column COURT_EVENT_CHARGES.PLEA_CODE is 'The Plea Code.  Reference Code(PLEA_STATUS)';

comment on column COURT_EVENT_CHARGES.RESULT_CODE_1 is 'The result code 1of the charge';

comment on column COURT_EVENT_CHARGES.RESULT_CODE_2 is 'The result code 2 of the charge';

comment on column COURT_EVENT_CHARGES.RESULT_CODE_1_INDICATOR is 'The indicator of the result code 1';

comment on column COURT_EVENT_CHARGES.RESULT_CODE_2_INDICATOR is 'The indicator of the result code 2';

comment on column COURT_EVENT_CHARGES.MOST_SERIOUS_FLAG is '? If this is the most serious offence charged';

comment on column COURT_EVENT_CHARGES.CREATE_DATETIME is 'The timestamp when the record is created';

comment on column COURT_EVENT_CHARGES.CREATE_USER_ID is 'The user who creates the record';

comment on column COURT_EVENT_CHARGES.MODIFY_DATETIME is 'The timestamp when the record is modified';

comment on column COURT_EVENT_CHARGES.MODIFY_USER_ID is 'The user who modifies the record';

comment on column COURT_EVENT_CHARGES.PROPERTY_VALUE is 'The property value of the offence';

comment on column COURT_EVENT_CHARGES.TOTAL_PROPERTY_VALUE is 'The total property value of the offence';

comment on column COURT_EVENT_CHARGES.NO_OF_OFFENCES is 'No of offences';

comment on column COURT_EVENT_CHARGES.OFFENCE_DATE is 'The Offence Date';

comment on column COURT_EVENT_CHARGES.OFFENCE_RANGE_DATE is 'The end date of the offence';

comment on column COURT_EVENT_CHARGES.CJIT_OFFENCE_CODE_1 is 'The CJIT Code 1';

comment on column COURT_EVENT_CHARGES.CJIT_OFFENCE_CODE_2 is 'The CJIT code 2';

comment on column COURT_EVENT_CHARGES.CJIT_OFFENCE_CODE_3 is 'The CJIT code 3';

create index COURT_EVENT_CHARGES_NI1
  on COURT_EVENT_CHARGES (OFFENDER_CHARGE_ID);

