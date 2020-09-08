CREATE TABLE NOMS_ID_SEQUENCE
(
    NOMS_ID                       NUMBER(5),
    PREFIX_ALPHA_SEQ              NUMBER(5)                         not null,
    SUFFIX_ALPHA_SEQ              NUMBER(5)                         not null,
    CURRENT_PREFIX                VARCHAR2(5)                       not null,
    CURRENT_SUFFIX                VARCHAR2(5)                       not null,
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

