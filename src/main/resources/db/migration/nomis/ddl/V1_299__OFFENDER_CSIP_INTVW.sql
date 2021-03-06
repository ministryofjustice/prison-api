CREATE TABLE OFFENDER_CSIP_INTVW (
	CSIP_ID NUMBER(10,0) NOT NULL,
	CSIP_INTVW_ID NUMBER(10,0) NOT NULL,
	CSIP_INTERVIEWEE VARCHAR2(100) NOT NULL,
	INTVW_DATE DATE,
	INTVW_ROLE VARCHAR2(12) NOT NULL,
	COMMENTS VARCHAR2(4000),
	CREATE_DATETIME TIMESTAMP DEFAULT systimestamp,
	CREATE_USER_ID VARCHAR2(32) DEFAULT USER ,
	MODIFY_DATETIME TIMESTAMP,
	MODIFY_USER_ID VARCHAR2(32),
	AUDIT_TIMESTAMP TIMESTAMP,
	AUDIT_USER_ID VARCHAR2(32),
	AUDIT_MODULE_NAME VARCHAR2(65),
	AUDIT_CLIENT_USER_ID VARCHAR2(64),
	AUDIT_CLIENT_IP_ADDRESS VARCHAR2(39),
	AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
	AUDIT_ADDITIONAL_INFO VARCHAR2(256),
	CONSTRAINT OFF_CSIP_INTVW_PK PRIMARY KEY (CSIP_INTVW_ID),
	CONSTRAINT OFF_CSIP_INTVW_OFF_CSIP_REP FOREIGN KEY (CSIP_ID) REFERENCES OFFENDER_CSIP_REPORTS(CSIP_ID)
);
CREATE INDEX OFFENDER_CSIP_INTVW_X01 ON OFFENDER_CSIP_INTVW (CSIP_ID);
CREATE UNIQUE INDEX OFF_CSIP_INTVW_PK ON OFFENDER_CSIP_INTVW (CSIP_INTVW_ID);
