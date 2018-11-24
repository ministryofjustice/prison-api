create table BED_ASSIGNMENT_HISTORIES
(
  OFFENDER_BOOK_ID NUMBER(10) not null constraint BED_AH_OFF_BKG_F1 references OFFENDER_BOOKINGS,
  BED_ASSIGN_SEQ NUMBER(6) not null,
  LIVING_UNIT_ID NUMBER(10) not null,
  ASSIGNMENT_DATE DATE default SYSDATE not null,
  ASSIGNMENT_TIME DATE default SYSDATE not null,
  ASSIGNMENT_REASON VARCHAR2(12 char),
  ASSIGNMENT_END_DATE DATE,
  ASSIGNMENT_END_TIME DATE,
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
  constraint BED_ASSIGN_HISTORIES_PK primary key (OFFENDER_BOOK_ID, BED_ASSIGN_SEQ)
);

comment on table BED_ASSIGNMENT_HISTORIES is 'The Offender Bed Assignment History';

comment on column BED_ASSIGNMENT_HISTORIES.OFFENDER_BOOK_ID is 'The system generated identifier for an offender booking.';

comment on column BED_ASSIGNMENT_HISTORIES.BED_ASSIGN_SEQ is 'A sequence number forming part of primary key.';

comment on column BED_ASSIGNMENT_HISTORIES.LIVING_UNIT_ID is 'The bed assignment for an offender. This may encompass up to four levels.';

comment on column BED_ASSIGNMENT_HISTORIES.ASSIGNMENT_DATE is 'The date the location was assigned to the offender.';

comment on column BED_ASSIGNMENT_HISTORIES.CREATE_DATETIME is 'The timestamp when the record is created';

comment on column BED_ASSIGNMENT_HISTORIES.CREATE_USER_ID is 'The user who creates the record';

comment on column BED_ASSIGNMENT_HISTORIES.MODIFY_DATETIME is 'The timestamp when the record is modified';

comment on column BED_ASSIGNMENT_HISTORIES.MODIFY_USER_ID is 'The user who modifies the record';

create index BED_ASSIGNMENT_HISTORIES_NI2
  on BED_ASSIGNMENT_HISTORIES (LIVING_UNIT_ID);

create index BED_ASSIGNMENT_HISTORIES_NI1
  on BED_ASSIGNMENT_HISTORIES (ASSIGNMENT_DATE);
