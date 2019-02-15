create table QUESTIONNAIRE_QUESTIONS
(
  QUESTIONNAIRE_QUE_ID          NUMBER(10)                             not null
    constraint QUESTIONNAIRE_QUESTIONS_PK
      primary key,
  QUESTIONNAIRE_ID              NUMBER(10)                             not null,
  QUE_SEQ                       NUMBER(6)                              not null,
  DESCRIPTION                   VARCHAR2(120 char)                     not null,
  LIST_SEQ                      NUMBER(6)                              not null,
  ACTIVE_FLAG                   VARCHAR2(1 char)  default 'N'          not null,
  EXPIRY_DATE                   DATE,
  MULTIPLE_ANSWER_FLAG          VARCHAR2(1 char)  default 'N',
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
  AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char)
);
