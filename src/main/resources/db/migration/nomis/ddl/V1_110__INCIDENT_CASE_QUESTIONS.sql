create table INCIDENT_CASE_QUESTIONS
(
  INCIDENT_CASE_ID              NUMBER(10)                             not null
    constraint INC_CASE_REQ_INC_CASE_FK
      references INCIDENT_CASES,
  QUESTION_SEQ                  NUMBER(6)                              not null,
  QUESTIONNAIRE_QUE_ID          NUMBER(10)                             not null,
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
  constraint INCIDENT_CASE_QUESTION_PK
    primary key (INCIDENT_CASE_ID, QUESTION_SEQ)
);

create index INC_CASE_QUE_QUE_QUE_FK on INCIDENT_CASE_QUESTIONS (QUESTIONNAIRE_QUE_ID);

