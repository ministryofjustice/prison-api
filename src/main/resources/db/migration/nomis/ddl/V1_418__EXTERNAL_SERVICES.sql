CREATE TABLE EXTERNAL_SERVICES (
        SERVICE_NAME VARCHAR2(40) NOT NULL,
        DESCRIPTION VARCHAR2(100) NOT NULL,
        CREATE_DATETIME TIMESTAMP(9) DEFAULT systimestamp NOT NULL,
        CREATE_USER_ID VARCHAR2(32) DEFAULT USER NOT NULL,
        MODIFY_DATETIME TIMESTAMP(9),
        MODIFY_USER_ID VARCHAR2(32),
        CONSTRAINT EXTERNAL_SERVICES_PK PRIMARY KEY (SERVICE_NAME)
);