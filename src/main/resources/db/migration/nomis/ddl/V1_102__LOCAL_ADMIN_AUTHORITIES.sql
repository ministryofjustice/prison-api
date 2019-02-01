create table LOCAL_ADMIN_AUTHORITIES
(
  LOCAL_AUTHORITY_CODE          VARCHAR2(6 char)                       not null
    constraint LOCAL_ADMIN_AUTHORITIES_PK primary key,
  DESCRIPTION                   VARCHAR2(40 char)                      not null,
  LOCAL_AUTHORITY_CATEGORY      VARCHAR2(12 char)                      not null,
  ACTIVE_FLAG                   VARCHAR2(1 char)  default 'Y'          not null,
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
  AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char)
);


