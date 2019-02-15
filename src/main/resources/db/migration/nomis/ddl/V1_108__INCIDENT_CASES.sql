create table INCIDENT_CASES
(
  INCIDENT_CASE_ID              NUMBER(10)                             not null
    constraint INCIDENT_CASES_PK
      primary key,
  REPORTED_STAFF_ID             NUMBER(10)                             not null,
  INCIDENT_DATE                 DATE                                   not null,
  INCIDENT_TIME                 DATE                                   not null,
  INCIDENT_STATUS               VARCHAR2(12 char)                      not null,
  RESPONSE_LOCKED_FLAG          VARCHAR2(1 char)  default 'N'          not null,
  INCIDENT_DETAILS              VARCHAR2(4000 char),
  REPORT_DATE                   DATE                                   not null,
  REPORT_TIME                   DATE                                   not null,
  AGY_LOC_ID                    VARCHAR2(6 char),
  INCIDENT_TITLE                VARCHAR2(240 char),
  INCIDENT_TYPE                 VARCHAR2(12 char)                      not null,
  QUESTIONNAIRE_ID              NUMBER(10),
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
  FOLLOW_UP_DATE                DATE
);

ALTER TABLE INCIDENT_CASES
  ADD CONSTRAINT INC_CASE_STAFF_FK FOREIGN KEY (REPORTED_STAFF_ID) REFERENCES STAFF_MEMBERS (STAFF_ID);

ALTER TABLE INCIDENT_CASES
  ADD CONSTRAINT INC_CASE_INC_STS_FK FOREIGN KEY (INCIDENT_STATUS) REFERENCES INCIDENT_STATUSES (CODE);

ALTER TABLE INCIDENT_CASES
  ADD CONSTRAINT INC_CASE_AGY_LOC_FK FOREIGN KEY (AGY_LOC_ID) REFERENCES AGENCY_LOCATIONS (AGY_LOC_ID);

ALTER TABLE INCIDENT_CASES
  ADD CONSTRAINT INC_CASE_QUE_FK FOREIGN KEY (QUESTIONNAIRE_ID) REFERENCES QUESTIONNAIRES (QUESTIONNAIRE_ID);


comment on table INCIDENT_CASES is 'Agency location incident cases (confidential reports)';
comment on column INCIDENT_CASES.INCIDENT_CASE_ID is 'Agency incident ID NOMIS Log number';
comment on column INCIDENT_CASES.REPORTED_STAFF_ID is 'The staff who report the incident';
comment on column INCIDENT_CASES.INCIDENT_DATE is 'Incident Date';
comment on column INCIDENT_CASES.INCIDENT_TIME is 'Incident Time';
comment on column INCIDENT_CASES.INCIDENT_STATUS is 'The status of the incident.  Reference Code ( IR_STS )';
comment on column INCIDENT_CASES.RESPONSE_LOCKED_FLAG is 'If the response completed ?';
comment on column INCIDENT_CASES.INCIDENT_DETAILS is 'The Occurence Details';
comment on column INCIDENT_CASES.REPORT_DATE is 'The report date of the incident';
comment on column INCIDENT_CASES.REPORT_TIME is 'The report time of the incident';
comment on column INCIDENT_CASES.AGY_LOC_ID is 'FK to agency locations';
comment on column INCIDENT_CASES.INCIDENT_TITLE is 'The title of the incident case';
comment on column INCIDENT_CASES.INCIDENT_TYPE is 'The type of the incident.  Reference Code (IR_TYPE)';
comment on column INCIDENT_CASES.QUESTIONNAIRE_ID is 'The questionnaire taken for the incident report.  FK to questionnaires';
comment on column INCIDENT_CASES.CREATE_DATETIME is 'The timestamp when the record is created';
comment on column INCIDENT_CASES.CREATE_USER_ID is 'The user who creates the record';
comment on column INCIDENT_CASES.MODIFY_DATETIME is 'The timestamp when the record is modified';
comment on column INCIDENT_CASES.MODIFY_USER_ID is 'The user who modifies the record';
comment on column INCIDENT_CASES.FOLLOW_UP_DATE is 'The followup date of the case';

create index INC_CASE_QUE_IDX_FK on INCIDENT_CASES (QUESTIONNAIRE_ID);
create index INC_CASE_INC_STS_IDX_FK on INCIDENT_CASES (INCIDENT_STATUS);
create index INCIDENT_CASES_NI3 on INCIDENT_CASES (INCIDENT_DATE);
create index INCIDENT_CASES_NI2 on INCIDENT_CASES (AGY_LOC_ID);
create index INCIDENT_CASES_NI1 on INCIDENT_CASES (REPORTED_STAFF_ID);
