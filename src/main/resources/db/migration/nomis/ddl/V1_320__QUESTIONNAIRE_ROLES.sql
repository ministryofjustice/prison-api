create table QUESTIONNAIRE_ROLES
(
  QUESTIONNAIRE_ID              NUMBER(10)                             not null,
  PARTICIPATION_ROLE            VARCHAR2(12 char)                      not null,
  SINGLE_ROLE_FLAG              VARCHAR2(1 char)  default 'N'          not null,
  LIST_SEQ                      NUMBER(6),
  ACTIVE_FLAG                   VARCHAR2(1 char)  default 'N'          not null,
  EXPIRY_DATE                   DATE,
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
  constraint QUESTIONNAIRE_ROLES_PK
      primary key (QUESTIONNAIRE_ID, PARTICIPATION_ROLE)
);
