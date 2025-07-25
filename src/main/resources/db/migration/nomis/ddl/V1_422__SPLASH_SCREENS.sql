CREATE TABLE SPLASH_SCREENS
(
    SPLASH_ID                     NUMBER(38, 0),
    MODULE_NAME                   VARCHAR2(20),
    FUNCTION_NAME                 VARCHAR2(100),
    WARNING_TEXT                  VARCHAR2(1000),
    BLOCKED_TEXT                  VARCHAR2(1000),
    BLOCK_ACCESS_CODE             VARCHAR2(12) DEFAULT 'NO',
    CREATE_DATETIME               TIMESTAMP    DEFAULT systimestamp,
    CREATE_USER_ID                VARCHAR2(32) DEFAULT USER,
    MODIFY_DATETIME               TIMESTAMP,
    MODIFY_USER_ID                VARCHAR2(32),
    AUDIT_TIMESTAMP               TIMESTAMP,
    AUDIT_USER_ID                 VARCHAR2(32),
    AUDIT_MODULE_NAME             VARCHAR2(65),
    AUDIT_CLIENT_USER_ID          VARCHAR2(64),
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256),
    CONSTRAINT SPLASH_SCREENS_PK PRIMARY KEY (SPLASH_ID),
    CONSTRAINT SPLASH_SCREENS_UK1 UNIQUE (MODULE_NAME),
    CONSTRAINT SYS_C00551440 CHECK ("SPLASH_ID" IS NOT NULL),
    CONSTRAINT SYS_C00551441 CHECK ("MODULE_NAME" IS NOT NULL),
    CONSTRAINT SYS_C00551463 CHECK ("CREATE_DATETIME" IS NOT NULL),
    CONSTRAINT SYS_C00551464 CHECK ("CREATE_USER_ID" IS NOT NULL),
    CONSTRAINT SYS_C00587260 CHECK ("BLOCK_ACCESS_CODE" IS NOT NULL),
    CONSTRAINT SPLASH_SCREENS_FK1 FOREIGN KEY (FUNCTION_NAME) REFERENCES SPLASH_SCREEN_FUNCS (FUNCTION_NAME)
);
CREATE UNIQUE INDEX SPLASH_SCREENS_PK ON SPLASH_SCREENS (SPLASH_ID);
CREATE UNIQUE INDEX SPLASH_SCREENS_UK1 ON SPLASH_SCREENS (MODULE_NAME);

CREATE SEQUENCE SPLASH_ID START WITH 1;
