
CREATE TABLE OFFENDER_SUB_ACCOUNTS
(	CASELOAD_ID VARCHAR2(6) NOT NULL,
	OFFENDER_ID NUMBER(10) NOT NULL,
	TRUST_ACCOUNT_CODE NUMBER(6) NOT NULL,
	BALANCE NUMBER(11,2),
	MINIMUM_BALANCE NUMBER(11,2),
	HOLD_BALANCE NUMBER(11,2),
	LAST_TXN_ID NUMBER(10),
	MODIFY_DATE DATE NOT NULL,
	MODIFY_USER_ID VARCHAR2(32),
	LIST_SEQ NUMBER(6) DEFAULT 99,
	IND_DATE DATE,
	IND_DAYS NUMBER(9),
	CREATE_DATETIME TIMESTAMP (9) DEFAULT SYSTIMESTAMP NOT NULL,
	CREATE_USER_ID VARCHAR2(32) DEFAULT USER NOT NULL,
	MODIFY_DATETIME TIMESTAMP (9),
	AUDIT_TIMESTAMP TIMESTAMP (9),
	AUDIT_USER_ID VARCHAR2(32),
	AUDIT_MODULE_NAME VARCHAR2(65),
	AUDIT_CLIENT_USER_ID VARCHAR2(64),
	AUDIT_CLIENT_IP_ADDRESS VARCHAR2(39),
	AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
	AUDIT_ADDITIONAL_INFO VARCHAR2(256),
	CONSTRAINT OFFENDER_SUB_ACCOUNTS_PK PRIMARY KEY (CASELOAD_ID, OFFENDER_ID, TRUST_ACCOUNT_CODE)
);

CREATE INDEX OFFENDER_SUB_ACCOUNTS_IX1 ON OFFENDER_SUB_ACCOUNTS (OFFENDER_ID, TRUST_ACCOUNT_CODE);

-- Note there are also triggers for this table
