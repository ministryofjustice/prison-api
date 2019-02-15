create table INCIDENT_CASE_RESPONSES
(
  INCIDENT_CASE_ID              NUMBER(10)                             not null,
  QUESTION_SEQ                  NUMBER(6)                              not null,
  RESPONSE_SEQ                  NUMBER(6)                              not null,
  RESPONSE_DATE                 DATE,
  RESPONSE_COMMENT_TEXT         VARCHAR2(240 char),
  RECORD_STAFF_ID               NUMBER(10)                             not null,
  QUESTIONNAIRE_ANS_ID          NUMBER(10),
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
  constraint INCIDENT_CASE_RESPONSE_PK
    primary key (INCIDENT_CASE_ID, QUESTION_SEQ, RESPONSE_SEQ),
  constraint INC_CASE_RESP_INC_CASE_QUE_FK
    foreign key (INCIDENT_CASE_ID, QUESTION_SEQ) references INCIDENT_CASE_QUESTIONS
);

create index INC_CASE_RESP_QUE_ANS_FK on INCIDENT_CASE_RESPONSES (QUESTIONNAIRE_ANS_ID);
