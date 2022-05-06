create table TEAMS
(
    TEAM_CODE                     VARCHAR2(20 char)                      not null
        constraint TEAMS_UK1
        unique,
    DESCRIPTION                   VARCHAR2(40 char)                      not null,
    CATEGORY                      VARCHAR2(12 char)                      not null,
    LIST_SEQ                      NUMBER(6),
    ACTIVE_FLAG                   VARCHAR2(1 char)  default 'Y'          not null,
    EXPIRY_DATE                   DATE,
    CREATE_USER_ID                VARCHAR2(32 char) default USER         not null,
    MODIFY_USER_ID                VARCHAR2(32 char),
    MODIFY_DATETIME               TIMESTAMP(9),
    CREATE_DATETIME               TIMESTAMP(9)      default systimestamp not null,
    TEAM_ID                       NUMBER(10)                             not null
        constraint TEAMS_PK
        primary key,
    AREA_CODE                     VARCHAR2(12 char),
    AGY_LOC_ID                    VARCHAR2(6 char),
    QUEUE_CLUSTER_ID              NUMBER(6),
    AUDIT_TIMESTAMP               TIMESTAMP(9),
    AUDIT_USER_ID                 VARCHAR2(32 char),
    AUDIT_MODULE_NAME             VARCHAR2(65 char),
    AUDIT_CLIENT_USER_ID          VARCHAR2(64 char),
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39 char),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char)
);
comment on table TEAMS is 'A set of offender management functions assigned to a group of NOMS Staff Members, each holding an Agency Location specific role & position.';
comment on column TEAMS.TEAM_CODE is 'Name of the team';
comment on column TEAMS.DESCRIPTION is 'The Description of the data';
comment on column TEAMS.CATEGORY is 'Category of the team';
comment on column TEAMS.LIST_SEQ is 'The sequence in which the data should be shown';
comment on column TEAMS.ACTIVE_FLAG is 'Active data indicator';
comment on column TEAMS.EXPIRY_DATE is 'Expiry date for the data';
comment on column TEAMS.CREATE_USER_ID is 'The user who creates the record';
comment on column TEAMS.MODIFY_USER_ID is 'The user who modifies the record';
comment on column TEAMS.MODIFY_DATETIME is 'The timestamp when the record is modified ';
comment on column TEAMS.CREATE_DATETIME is 'The timestamp when the record is created';
comment on column TEAMS.TEAM_ID is 'PK of the team';
comment on column TEAMS.AGY_LOC_ID is 'The Related Agency Location Identifier';
