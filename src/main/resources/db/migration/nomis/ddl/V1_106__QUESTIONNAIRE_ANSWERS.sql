create table QUESTIONNAIRE_ANSWERS
(
  QUESTIONNAIRE_ANS_ID          NUMBER(10)                             not null
    constraint QUESTIONNAIRE_ANSWERS_PK
      primary key,
  QUESTIONNAIRE_QUE_ID          NUMBER(10)                             not null,
  NEXT_QUESTIONNAIRE_QUE_ID     NUMBER(10),
  ANS_SEQ                       NUMBER(6)                              not null,
  DESCRIPTION                   VARCHAR2(80 char)                      not null,
  LIST_SEQ                      NUMBER(6)         default 1            not null,
  ACTIVE_FLAG                   VARCHAR2(1 char)  default 'N'          not null,
  EXPIRY_DATE                   DATE,
  DATE_REQUIRED_FLAG            VARCHAR2(1 char)  default 'N',
  COMMENT_REQUIRED_FLAG         VARCHAR2(1 char)  default 'N',
  CREATE_DATETIME               TIMESTAMP(9)      default systimestamp not null,
  CREATE_USER_ID                VARCHAR2(32 char) default USER         not null,
  MODIFY_DATETIME               TIMESTAMP(9),
  MODIFY_USER_ID                VARCHAR2(32 char),
  SCORE                         NUMBER(6),
  AUDIT_TIMESTAMP               TIMESTAMP(9),
  AUDIT_USER_ID                 VARCHAR2(32 char),
  AUDIT_MODULE_NAME             VARCHAR2(65 char),
  AUDIT_CLIENT_USER_ID          VARCHAR2(64 char),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39 char),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
  AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char)
);

create index QUE_ANS_QUE_QUE_FK2 on QUESTIONNAIRE_ANSWERS (NEXT_QUESTIONNAIRE_QUE_ID);

create index QUESTIONNAIRE_ANSWERS_NI1 on QUESTIONNAIRE_ANSWERS (QUESTIONNAIRE_QUE_ID);