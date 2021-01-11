create table BOOKING_NUMBER_BLOCKS
(
    USE_SEQ                       NUMBER(6)                         not null PRIMARY KEY,
    BOOKING_NUMBER_CHAR           VARCHAR2(1)                       not null,
    BOOKING_NUMBER_START          NUMBER(6)                         not null,
    BOOKING_NUMBER_END            NUMBER(6)                         not null,
    LAST_USED                     NUMBER(6),
    ACTIVE_FLAG                   VARCHAR2(1),
    USED_FLAG                     VARCHAR2(1)                       not null,
    PREFIX_OR_SUFFIX              VARCHAR2(1)  default 'P'          not null,
    CREATE_DATETIME               TIMESTAMP(9) default systimestamp not null,
    CREATE_USER_ID                VARCHAR2(32) default USER         not null,
    MODIFY_DATETIME               TIMESTAMP(9),
    MODIFY_USER_ID                VARCHAR2(32),
    AUDIT_TIMESTAMP               TIMESTAMP(9),
    AUDIT_USER_ID                 VARCHAR2(32),
    AUDIT_MODULE_NAME             VARCHAR2(65),
    AUDIT_CLIENT_USER_ID          VARCHAR2(64),
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256)
);
