create table OFFENDER_SENTENCE_CHARGES
(
  OFFENDER_BOOK_ID NUMBER(10) not null,
  SENTENCE_SEQ NUMBER(6) not null,
  OFFENDER_CHARGE_ID NUMBER(10) not null constraint OFF_SENT_CHG_OFF_CHG_FK references OFFENDER_CHARGES,
  CREATE_DATETIME TIMESTAMP(9) default systimestamp not null,
  CREATE_USER_ID VARCHAR2(32 char) default USER not null,
  MODIFY_DATETIME TIMESTAMP(9),
  MODIFY_USER_ID VARCHAR2(32 char),
  AUDIT_TIMESTAMP TIMESTAMP(9),
  AUDIT_USER_ID VARCHAR2(32 char),
  AUDIT_MODULE_NAME VARCHAR2(65 char),
  AUDIT_CLIENT_USER_ID VARCHAR2(64 char),
  AUDIT_CLIENT_IP_ADDRESS VARCHAR2(39 char),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
  AUDIT_ADDITIONAL_INFO VARCHAR2(256 char),
  constraint OFFENDER_SENTENCE_CHARGES_PK primary key (OFFENDER_BOOK_ID, SENTENCE_SEQ, OFFENDER_CHARGE_ID)
);

comment on table OFFENDER_SENTENCE_CHARGES is 'The assocation between offender sentences and offender charges';

comment on column OFFENDER_SENTENCE_CHARGES.OFFENDER_BOOK_ID is 'The offender Book ID';

comment on column OFFENDER_SENTENCE_CHARGES.SENTENCE_SEQ is 'The sentence seq';

comment on column OFFENDER_SENTENCE_CHARGES.OFFENDER_CHARGE_ID is 'The offender charge ID';

comment on column OFFENDER_SENTENCE_CHARGES.CREATE_DATETIME is 'The timestamp when the record is created';

comment on column OFFENDER_SENTENCE_CHARGES.CREATE_USER_ID is 'The user who creates the record';

comment on column OFFENDER_SENTENCE_CHARGES.MODIFY_DATETIME is 'The timestamp when the record is modified';

comment on column OFFENDER_SENTENCE_CHARGES.MODIFY_USER_ID is 'The user who modifies the record';

create index OFFENDER_SENTENCE_CHARGES_NI1 on OFFENDER_SENTENCE_CHARGES (OFFENDER_CHARGE_ID);

