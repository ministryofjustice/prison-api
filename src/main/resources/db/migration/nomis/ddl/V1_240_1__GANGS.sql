CREATE TABLE GANGS
(
    GANG_CODE                     VARCHAR2(12)                      NOT NULL,
    GANG_NAME                     VARCHAR2(40)                      NULL,
    LIST_SEQ                      NUMBER(6, 0)                      NULL,
    ACTIVE_FLAG                   VARCHAR2(1)  DEFAULT 'Y'          NOT NULL,
    EXPIRY_DATE                   DATE                              NULL,
    UPDATE_ALLOWED_FLAG           VARCHAR2(1)  DEFAULT 'Y'          NOT NULL,
    MODIFY_USER_ID                VARCHAR2(32)                      NULL,
    PARENT_GANG_CODE              VARCHAR2(12)                      NULL,
    CREATE_DATETIME               TIMESTAMP    DEFAULT systimestamp NOT NULL,
    CREATE_USER_ID                VARCHAR2(32) DEFAULT USER         NOT NULL,
    MODIFY_DATETIME               TIMESTAMP                         NULL,
    AUDIT_TIMESTAMP               TIMESTAMP                         NULL,
    AUDIT_USER_ID                 VARCHAR2(32)                      NULL,
    AUDIT_MODULE_NAME             VARCHAR2(65)                      NULL,
    AUDIT_CLIENT_USER_ID          VARCHAR2(64)                      NULL,
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39)                      NULL,
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64)                      NULL,
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256)                     NULL,
    CONSTRAINT GANGS_PK PRIMARY KEY (GANG_CODE)
);
CREATE INDEX GANGS_NI1 ON GANGS (PARENT_GANG_CODE);
CREATE UNIQUE INDEX GANGS_PK ON GANGS (GANG_CODE);