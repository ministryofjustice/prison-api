create table QUESTIONNAIRES
(
  QUESTIONNAIRE_ID              NUMBER(10)                             not null
    constraint QUESTIONNAIRES_PK
      primary key,
  QUESTIONNAIRE_CATEGORY        VARCHAR2(12 char)                      not null,
  CODE                          VARCHAR2(12 char)                      not null,
  CREATE_DATETIME               TIMESTAMP(9)      default systimestamp not null,
  CREATE_USER_ID                VARCHAR2(32 char) default USER         not null,
  MODIFY_DATETIME               TIMESTAMP(9),
  MODIFY_USER_ID                VARCHAR2(32 char),
  ACTIVE_FLAG                   VARCHAR2(1 char)  default 'Y'          not null,
  DESCRIPTION                   VARCHAR2(120 char),
  LIST_SEQ                      NUMBER(6)                              not null,
  EXPIRY_DATE                   DATE,
  AUDIT_TIMESTAMP               TIMESTAMP(9),
  AUDIT_USER_ID                 VARCHAR2(32 char),
  AUDIT_MODULE_NAME             VARCHAR2(65 char),
  AUDIT_CLIENT_USER_ID          VARCHAR2(64 char),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39 char),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
  AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char),
  constraint QUESTIONNAIRES_UK
    unique (QUESTIONNAIRE_CATEGORY, CODE)
);

comment on table QUESTIONNAIRES is 'The questionniare for such as incident cases.';
comment on column QUESTIONNAIRES.QUESTIONNAIRE_ID is 'System generated identifier for an questionnaire.';
comment on column QUESTIONNAIRES.QUESTIONNAIRE_CATEGORY is 'Reference Code [ QUE_CLS ] : QUESTIONNAIRE Class Such as (Category, type, Question, Answer/response)';
comment on column QUESTIONNAIRES.CODE is 'questionnaire Code for different parts of questionnaire ie.  Type, Section, Category, Indicator.';
comment on column QUESTIONNAIRES.CREATE_DATETIME is 'The timestamp when the record is created';
comment on column QUESTIONNAIRES.CREATE_USER_ID is 'The user who creates the record';
comment on column QUESTIONNAIRES.MODIFY_DATETIME is 'The timestamp when the record is modified';
comment on column QUESTIONNAIRES.MODIFY_USER_ID is 'The user who modifies the record';
comment on column QUESTIONNAIRES.ACTIVE_FLAG is '? if the questionnaire active';
comment on column QUESTIONNAIRES.DESCRIPTION is 'Description of the questionnaire';
comment on column QUESTIONNAIRES.LIST_SEQ is 'The listing order';
comment on column QUESTIONNAIRES.EXPIRY_DATE is 'expiry date of the questionnaire';

