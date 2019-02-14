
create table INCIDENT_STATUSES
(
  CODE                          VARCHAR2(12 char)                      not null,
  DESCRIPTION                   VARCHAR2(40 char)                      not null,
  LIST_SEQ                      NUMBER(6)         default 1,
  ACTIVE_FLAG                   VARCHAR2(1 char)  default 'Y'          not null,
  EXPIRY_DATE                   DATE,
  STANDARD_USER_FLAG            VARCHAR2(1 char)  default 'N'          not null,
  ENHANCED_USER_FLAG            VARCHAR2(1 char)  default 'N'          not null,
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
  constraint INCIDENT_STATUSES_PK primary key (CODE)
);


comment on table INCIDENT_STATUSES is 'The status code for incident cases.';
comment on column INCIDENT_STATUSES.CODE is 'Reference code';
comment on column INCIDENT_STATUSES.DESCRIPTION is 'Description of the code';
comment on column INCIDENT_STATUSES.LIST_SEQ is 'Listing order of the code (It for controlling the order of listing in LOV).  If the value is "0", then it is taken as the default for this domain';
comment on column INCIDENT_STATUSES.ACTIVE_FLAG is 'Is the code active ?';
comment on column INCIDENT_STATUSES.EXPIRY_DATE is 'If the code allowed to changed ( It is for controlling the code)';
comment on column INCIDENT_STATUSES.STANDARD_USER_FLAG is 'Accessible by All user ?';
comment on column INCIDENT_STATUSES.ENHANCED_USER_FLAG is 'Accessible by Enhanced User ?';
comment on column INCIDENT_STATUSES.CREATE_DATETIME is 'The timestamp when the record is created';
comment on column INCIDENT_STATUSES.CREATE_USER_ID is 'The user who creates the record';
comment on column INCIDENT_STATUSES.MODIFY_DATETIME is 'The timestamp when the record is modified';
comment on column INCIDENT_STATUSES.MODIFY_USER_ID is 'The user who modifies the record';

