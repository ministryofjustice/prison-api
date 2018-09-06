create table COURT_EVENTS
(
  EVENT_ID                      NUMBER(10) not null constraint COURT_EVENTS_PK primary key,
  CASE_ID                       NUMBER(10) constraint COURT_EVENTS_OFF_CASES_FK references OFFENDER_CASES,
  OFFENDER_BOOK_ID              NUMBER(10) not null constraint CRT_EVENTS_OFF_BKG_FK references OFFENDER_BOOKINGS,
  EVENT_DATE                    DATE not null,
  START_TIME                    DATE not null,
  END_TIME                      DATE,
  COURT_EVENT_TYPE              VARCHAR2(12 char) not null,
  JUDGE_NAME                    VARCHAR2(60 char),
  EVENT_STATUS                  VARCHAR2(12 char) default 'SCH',
  PARENT_EVENT_ID               NUMBER(10),
  AGY_LOC_ID                    VARCHAR2(6 char) not null constraint COURT_EVENTS_AGY_LOC_FK references AGENCY_LOCATIONS,
  OUTCOME_REASON_CODE           VARCHAR2(12 char),
  COMMENT_TEXT                  VARCHAR2(240 char),
  CREATE_DATETIME               TIMESTAMP(9) default systimestamp not null,
  CREATE_USER_ID                VARCHAR2(32 char) default USER not null,
  MODIFY_DATETIME               TIMESTAMP(9),
  MODIFY_USER_ID                VARCHAR2(32 char),
  EVENT_OUTCOME                 VARCHAR2(12 char),
  NEXT_EVENT_REQUEST_FLAG       VARCHAR2(1 char) default 'N',
  ORDER_REQUESTED_FLAG          VARCHAR2(1 char) default 'N',
  RESULT_CODE                   VARCHAR2(12 char),
  NEXT_EVENT_DATE               DATE,
  NEXT_EVENT_START_TIME         DATE,
  OUTCOME_DATE                  DATE,
  AUDIT_TIMESTAMP               TIMESTAMP(9),
  AUDIT_USER_ID                 VARCHAR2(32 char),
  AUDIT_MODULE_NAME             VARCHAR2(65 char),
  AUDIT_CLIENT_USER_ID          VARCHAR2(64 char),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39 char),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
  AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char),
  OFFENDER_PROCEEDING_ID        NUMBER(10),
  DIRECTION_CODE                VARCHAR2(12 char),
  HOLD_FLAG                     VARCHAR2(1 char) default 'N'
);

comment on table COURT_EVENTS is 'A court event either scheduled to occur or which has occurred in relation to an offender during a period of supervison by NOMS e.g. an appearance, court directions, a trial. The event is on a specific date and at a specific time.';
comment on column COURT_EVENTS.EVENT_ID is 'The PK of the court event';
comment on column COURT_EVENTS.CASE_ID is 'FK to the offender cases';
comment on column COURT_EVENTS.OFFENDER_BOOK_ID is 'FK to the Offender Booking ID';
comment on column COURT_EVENTS.EVENT_DATE is 'The event date';
comment on column COURT_EVENTS.START_TIME is 'The start time of the court event';
comment on column COURT_EVENTS.END_TIME is 'The end time of the court event if there is any';
comment on column COURT_EVENTS.COURT_EVENT_TYPE is 'Reference Code (EVENT_SUBTYP) Where parent code = ''CRT''';
comment on column COURT_EVENTS.JUDGE_NAME is 'The name of the judge';
comment on column COURT_EVENTS.EVENT_STATUS is 'Reference Code (EVENT_STS)';
comment on column COURT_EVENTS.PARENT_EVENT_ID is 'FK to the previous court event';
comment on column COURT_EVENTS.AGY_LOC_ID is 'FK to the agency location';
comment on column COURT_EVENTS.OUTCOME_REASON_CODE is 'Reference Code (CANC_RSN)';
comment on column COURT_EVENTS.COMMENT_TEXT is 'The general comment text';
comment on column COURT_EVENTS.CREATE_DATETIME is 'The timestamp when the record is created';
comment on column COURT_EVENTS.CREATE_USER_ID is 'The user who creates the record';
comment on column COURT_EVENTS.MODIFY_DATETIME is 'The timestamp when the record is modified';
comment on column COURT_EVENTS.MODIFY_USER_ID is 'The user who modifies the record';
comment on column COURT_EVENTS.EVENT_OUTCOME is 'The outcome of the court event:Reference COde(OUTCOMES)';
comment on column COURT_EVENTS.NEXT_EVENT_REQUEST_FLAG is 'Next event is requested';
comment on column COURT_EVENTS.ORDER_REQUESTED_FLAG is 'An Orderis requested';
comment on column COURT_EVENTS.RESULT_CODE is 'The result code';
comment on column COURT_EVENTS.NEXT_EVENT_DATE is 'The next event date';
comment on column COURT_EVENTS.NEXT_EVENT_START_TIME is 'The next event start time';
comment on column COURT_EVENTS.OUTCOME_DATE is 'The date of the outcome deteremined';
comment on column COURT_EVENTS.HOLD_FLAG is 'Indicates if hold ordered by court at this hearing';

create index CRT_EVENTS_OFF_BKG_FK on COURT_EVENTS (OFFENDER_BOOK_ID);
create index COURT_EVENTS_NI5 on COURT_EVENTS (CASE_ID);
create index COURT_EVENTS_NI3 on COURT_EVENTS (AGY_LOC_ID);
create index COURT_EVENTS_NI1 on COURT_EVENTS (EVENT_DATE);
create index COURT_EVENTS_NI6_TEST on COURT_EVENTS (OFFENDER_BOOK_ID, EVENT_DATE, COURT_EVENT_TYPE);
