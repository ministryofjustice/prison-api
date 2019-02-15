create table INCIDENT_CASE_PARTIES
(
  INCIDENT_CASE_ID              NUMBER(10)                             not null
    constraint INC_CASE_PTY_INC_CASE_FK
      references INCIDENT_CASES,
  PARTY_SEQ                     NUMBER(6)         default 1            not null,
  PARTICIPATION_ROLE            VARCHAR2(12 char)                      not null,
  OFFENDER_BOOK_ID              NUMBER(10)
    constraint INC_CASE_PTY_OFF_BKG_FK
      references OFFENDER_BOOKINGS,
  STAFF_ID                      NUMBER(10)
    constraint INC_CASE_PTY_STF_FK
      references STAFF_MEMBERS,
  PERSON_ID                     NUMBER(10),
  COMMENT_TEXT                  VARCHAR2(240 char),
  OUTCOME_CODE                  VARCHAR2(12 char),
  RECORD_STAFF_ID               NUMBER(10),
  CREATE_DATETIME               TIMESTAMP(9)      default systimestamp not null,
  CREATE_USER_ID                VARCHAR2(32 char) default USER         not null,
  MODIFY_DATETIME               TIMESTAMP(9),
  MODIFY_USER_ID                VARCHAR2(32 char),
  AUDIT_TIMESTAMP               TIMESTAMP(9),
  AUDIT_USER_ID                 VARCHAR2(32 char),
  AUDIT_MODULE_NAME             VARCHAR2(65 char),
  AUDIT_CLIENT_USER_ID          VARCHAR2(64 char),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39 char),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
  AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char),
  constraint INCIDENT_CASE_PARTIES_PK
    primary key (INCIDENT_CASE_ID, PARTY_SEQ)
);

comment on table INCIDENT_CASE_PARTIES is 'The parties involvement in an incident cases.';
comment on column INCIDENT_CASE_PARTIES.INCIDENT_CASE_ID is 'Incident ID';
comment on column INCIDENT_CASE_PARTIES.PARTY_SEQ is 'Sequence';
comment on column INCIDENT_CASE_PARTIES.PARTICIPATION_ROLE is 'The role of the participation parties. Reference Code (IR_STF_PART), (IR_OFF_PART)';
comment on column INCIDENT_CASE_PARTIES.OFFENDER_BOOK_ID is 'FK : Offender Bookings';
comment on column INCIDENT_CASE_PARTIES.STAFF_ID is 'FK : Staff Members';
comment on column INCIDENT_CASE_PARTIES.PERSON_ID is 'FK : Persons';
comment on column INCIDENT_CASE_PARTIES.COMMENT_TEXT is 'The general comment text';
comment on column INCIDENT_CASE_PARTIES.OUTCOME_CODE is 'The outcome the party.  Reference Code (IR_OUTCOME)';
comment on column INCIDENT_CASE_PARTIES.RECORD_STAFF_ID is 'The staff who input the record';
comment on column INCIDENT_CASE_PARTIES.CREATE_DATETIME is 'The timestamp when the record is created';
comment on column INCIDENT_CASE_PARTIES.CREATE_USER_ID is 'The user who creates the record';
comment on column INCIDENT_CASE_PARTIES.MODIFY_DATETIME is 'The timestamp when the record is modified';
comment on column INCIDENT_CASE_PARTIES.MODIFY_USER_ID is 'The user who modifies the record';


create index INCIDENT_CASE_PARTIES_NI2 on INCIDENT_CASE_PARTIES (STAFF_ID);
create index INCIDENT_CASE_PARTIES_NI1 on INCIDENT_CASE_PARTIES (OFFENDER_BOOK_ID);


