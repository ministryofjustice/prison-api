CREATE TABLE GANG_NON_ASSOCIATIONS
(
    GANG_CODE                     VARCHAR2(12)                      NOT NULL,
    NS_GANG_CODE                  VARCHAR2(12)                      NOT NULL,
    NS_REASON_CODE                VARCHAR2(12)                      NOT NULL,
    NS_LEVEL_CODE                 VARCHAR2(12)                      NULL,
    INTERNAL_LOCATION_FLAG        VARCHAR2(1)  DEFAULT 'Y'          NOT NULL,
    TRANSPORT_FLAG                VARCHAR2(1)                       NOT NULL,
    CREATE_DATETIME               TIMESTAMP    DEFAULT systimestamp NOT NULL,
    CREATE_USER_ID                VARCHAR2(32) DEFAULT USER         NOT NULL,
    MODIFY_DATETIME               TIMESTAMP                         NULL,
    MODIFY_USER_ID                VARCHAR2(32)                      NULL,
    AUDIT_TIMESTAMP               TIMESTAMP                         NULL,
    AUDIT_USER_ID                 VARCHAR2(32)                      NULL,
    AUDIT_MODULE_NAME             VARCHAR2(65)                      NULL,
    AUDIT_CLIENT_USER_ID          VARCHAR2(64)                      NULL,
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39)                      NULL,
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64)                      NULL,
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256)                     NULL,
    CONSTRAINT GANG_NON_ASSOCIATIONS_PK PRIMARY KEY (GANG_CODE, NS_GANG_CODE),
    CONSTRAINT GANG_NA_GANG_F1 FOREIGN KEY (GANG_CODE) REFERENCES GANGS (GANG_CODE),
    CONSTRAINT GANG_NA_GANG_F2 FOREIGN KEY (NS_GANG_CODE) REFERENCES GANGS (GANG_CODE)
);
CREATE INDEX GANG_NA_NI1 ON GANG_NON_ASSOCIATIONS (NS_GANG_CODE);
CREATE UNIQUE INDEX GANG_NON_ASSOCIATIONS_PK ON GANG_NON_ASSOCIATIONS (GANG_CODE, NS_GANG_CODE);